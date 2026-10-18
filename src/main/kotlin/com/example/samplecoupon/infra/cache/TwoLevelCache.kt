package com.example.samplecoupon.infra.cache

import com.github.benmanes.caffeine.cache.Cache as CaffeineCache
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ConcurrentHashMap
import org.springframework.beans.factory.InitializingBean
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.support.SimpleValueWrapper

/**
 * Spring [Cache] putting in-memory [local] Caffeine cache in front of shared [remote] one (e.g.
 * Redis).
 *
 * Value-loader [get] is single-flight per key within JVM - first caller to miss [local] registers
 * in-flight load, concurrent callers for same key wait on it instead of hitting [remote] and loader
 * themselves. Across N instances, at most N loads per key. Requires `@Cacheable(sync = true)`, as
 * without `sync` Spring bypasses value-loader path and every caller loads on its own.
 *
 * In-flight loads live outside Caffeine on purpose - Caffeine logs a warning with stack trace for
 * every future it holds that completes exceptionally, which would duplicate each loader failure.
 *
 * Local entries are not invalidated across instances, so keep local TTL short.
 */
class TwoLevelCache(
    private val local: CaffeineCache<Any, Any>,
    internal val remote: Cache,
) : Cache {

  private val inFlightLoads = ConcurrentHashMap<Any, CompletableFuture<Any?>>()

  override fun getName(): String = remote.name

  override fun getNativeCache(): Any = remote.nativeCache

  override fun get(key: Any): Cache.ValueWrapper? {
    local.getIfPresent(key)?.let {
      return SimpleValueWrapper(it)
    }
    val wrapper = remote.get(key)
    wrapper?.get()?.let { local.put(key, it) }
    return wrapper
  }

  override fun <T : Any> get(key: Any, type: Class<T>?): T? {
    val value = get(key)?.get()
    check(value == null || type == null || type.isInstance(value)) {
      "Cached value is not of required type [${type?.name}]: $value"
    }
    @Suppress("UNCHECKED_CAST")
    return value as T?
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(key: Any, valueLoader: Callable<T>): T? {
    local.getIfPresent(key)?.let {
      return it as T
    }

    val leader = CompletableFuture<Any?>()
    val inFlight = inFlightLoads.putIfAbsent(key, leader)
    if (inFlight != null) {
      return await(inFlight) as T?
    }

    try {
      // Previous leader may have finished between local miss and registering this load.
      val value =
          local.getIfPresent(key) ?: remote.get(key, valueLoader)?.also { local.put(key, it) }
      leader.complete(value)
      return value as T?
    } catch (e: Throwable) {
      leader.completeExceptionally(e)
      throw e
    } finally {
      // Removed only after value landed in local, so late callers hit it instead of loading again.
      inFlightLoads.remove(key, leader)
    }
  }

  override fun put(key: Any, value: Any?) {
    remote.put(key, value)
    if (value == null) {
      local.invalidate(key)
    } else {
      local.put(key, value)
    }
  }

  override fun evict(key: Any) {
    remote.evict(key)
    local.invalidate(key)
  }

  override fun clear() {
    remote.clear()
    local.invalidateAll()
  }

  private fun await(future: CompletableFuture<Any?>): Any? =
      try {
        future.join()
      } catch (e: CompletionException) {
        throw e.cause ?: e
      }
}

/**
 * Wraps every cache of [remote] manager in [TwoLevelCache], with local cache from [localCache].
 *
 * Forwards [afterPropertiesSet] to [remote], so it can be built inline instead of registered as
 * separate bean - e.g. `RedisCacheManager` loads its initial cache configurations there.
 */
class TwoLevelCacheManager(
    private val remote: CacheManager,
    private val localCache: (name: String) -> CaffeineCache<Any, Any>,
) : CacheManager, InitializingBean {

  private val caches = ConcurrentHashMap<String, Cache>()

  override fun afterPropertiesSet() {
    (remote as? InitializingBean)?.afterPropertiesSet()
  }

  override fun getCache(name: String): Cache? =
      remote.getCache(name)?.let { remoteCache ->
        caches.computeIfAbsent(name) { TwoLevelCache(localCache(name), remoteCache) }
      }

  override fun getCacheNames(): Collection<String> = remote.cacheNames
}
