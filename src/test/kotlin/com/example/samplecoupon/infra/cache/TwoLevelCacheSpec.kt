package com.example.samplecoupon.infra.cache

import com.github.benmanes.caffeine.cache.Caffeine
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.lang.Thread.State.TIMED_WAITING
import java.lang.Thread.State.WAITING
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.seconds
import org.springframework.cache.Cache
import org.springframework.cache.concurrent.ConcurrentMapCache

class TwoLevelCacheSpec :
    BehaviorSpec(
        {
          fun twoLevelCache(remote: Cache = NonAtomicRemoteCache()) =
              TwoLevelCache(Caffeine.newBuilder().build(), remote)

          /**
           * Calls [load] from [count] threads at once and returns their outcomes. Every thread is
           * parked - leader inside loader on [release], followers on in-flight load - before
           * [release] opens, so all of them overlap with single in-flight load.
           */
          suspend fun <T> loadConcurrently(
              count: Int,
              release: CountDownLatch,
              load: () -> T,
          ): List<Result<T>> {
            val results = arrayOfNulls<Result<T>>(count)
            val callers = (0 until count).map { i -> thread { results[i] = runCatching(load) } }
            eventually(5.seconds) {
              callers.all { it.state in setOf(WAITING, TIMED_WAITING) } shouldBe true
            }
            release.countDown()
            callers.forEach { it.join(10_000) }
            return results.map { it!! }
          }

          Given("many concurrent callers missing the same key") {
            val cache = twoLevelCache()
            val loads = AtomicInteger()
            val release = CountDownLatch(1)

            When("all of them load through value loader") {
              val results =
                  loadConcurrently(20, release) {
                    cache.get("1.2.3.4") {
                      loads.incrementAndGet()
                      release.await(10, TimeUnit.SECONDS)
                      "PL"
                    }
                  }

              Then("loader runs once") { loads.get() shouldBe 1 }

              Then("every caller gets loaded value") {
                results.map { it.getOrThrow() } shouldContainOnly listOf("PL")
              }
            }
          }

          Given("many concurrent callers missing the same key with failing loader") {
            val cache = twoLevelCache()
            val loads = AtomicInteger()
            val release = CountDownLatch(1)

            When("all of them load through value loader") {
              val results =
                  loadConcurrently(20, release) {
                    cache.get<String>("1.2.3.4") {
                      loads.incrementAndGet()
                      release.await(10, TimeUnit.SECONDS)
                      throw IllegalStateException("ip-api down")
                    }
                  }

              Then("loader runs once") { loads.get() shouldBe 1 }

              Then("every caller gets leader's failure") {
                results.forEach {
                  it.exceptionOrNull()
                      .shouldBeInstanceOf<Cache.ValueRetrievalException>()
                      .cause
                      .shouldBeInstanceOf<IllegalStateException>()
                }
              }
            }
          }

          Given("a loader that fails") {
            val cache = twoLevelCache()

            When("loading fails and then succeeds") {
              val failure =
                  shouldThrow<Cache.ValueRetrievalException> {
                    cache.get<String>("1.2.3.4") { throw IllegalStateException("ip-api down") }
                  }
              val loaded = cache.get("1.2.3.4") { "PL" }

              Then("failure is propagated with original cause") {
                (failure.cause is IllegalStateException) shouldBe true
              }

              Then("failure is not cached") { loaded shouldBe "PL" }
            }
          }

          Given("a value loaded into both levels") {
            val remote = ConcurrentMapCache("remote")
            val cache = twoLevelCache(remote)
            cache.get("1.2.3.4") { "PL" }

            When("remote entry disappears") {
              remote.evict("1.2.3.4")

              Then("local level still serves it") {
                cache.get("1.2.3.4")?.get() shouldBe "PL"
                cache.get("1.2.3.4") { "DE" } shouldBe "PL"
              }
            }

            When("entry is evicted through two-level cache") {
              cache.evict("1.2.3.4")

              Then("both levels drop it") {
                cache.get("1.2.3.4") shouldBe null
                remote.get("1.2.3.4") shouldBe null
              }
            }
          }

          Given("a value present only in remote level") {
            val remote = ConcurrentMapCache("remote")
            val cache = twoLevelCache(remote)
            remote.put("1.2.3.4", "PL")

            When("it is read through two-level cache") {
              val value = cache.get("1.2.3.4", String::class.java)
              remote.clear()

              Then("it is returned and copied to local level") {
                value shouldBe "PL"
                cache.get("1.2.3.4", String::class.java) shouldBe "PL"
              }
            }
          }
        },
    )

/**
 * Mimics non-locking `RedisCache` - check-then-load with no per-key coordination, so concurrent
 * misses all run loader. [ConcurrentMapCache] alone loads via `computeIfAbsent`, which is already
 * single-flight and would hide missing coordination in [TwoLevelCache].
 */
private class NonAtomicRemoteCache : ConcurrentMapCache("remote") {

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(key: Any, valueLoader: Callable<T>): T? {
    get(key)?.let {
      return it.get() as T?
    }
    val value =
        try {
          valueLoader.call()
        } catch (e: Exception) {
          throw Cache.ValueRetrievalException(key, valueLoader, e)
        }
    put(key, value)
    return value
  }
}
