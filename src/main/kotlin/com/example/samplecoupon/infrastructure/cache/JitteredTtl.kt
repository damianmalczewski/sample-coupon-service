package com.example.samplecoupon.infrastructure.cache

import java.time.Duration
import java.util.concurrent.ThreadLocalRandom
import org.springframework.data.redis.cache.RedisCacheWriter.TtlFunction

/**
 * [TtlFunction] adding random extra in `[0, jitter)` to [ttl], so entries written together (e.g.
 * after cold start) do not expire together and trigger another wave of loads.
 */
class JitteredTtl(private val ttl: Duration, private val jitter: Duration) : TtlFunction {

  override fun getTimeToLive(key: Any, value: Any?): Duration =
      when {
        jitter.isZero -> ttl
        else -> ttl.plusNanos(ThreadLocalRandom.current().nextLong(jitter.toNanos()))
      }
}
