package com.example.samplecoupon.infra

import com.example.samplecoupon.infra.cache.JitteredTtl
import com.example.samplecoupon.infra.cache.TwoLevelCache
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Duration
import org.springframework.data.redis.cache.RedisCache
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory

class RedisCacheConfigurationSpec :
    BehaviorSpec(
        {
          Given("caching properties") {
            val properties =
                CachingProperties(
                    ipAddrGeoLocationTtl = Duration.ofHours(8),
                    ipAddrGeoLocationTtlJitter = Duration.ofHours(1),
                    localTtl = Duration.ofMinutes(1),
                    localMaxSize = 100,
                )

            When("building cache manager") {
              // Connection factory is never started - building caches does not touch Redis.
              val cacheManager =
                  RedisCacheConfiguration()
                      .twoLevelCacheManager(LettuceConnectionFactory(), properties)
                      .apply { afterPropertiesSet() } // as Spring does for bean

              Then("geolocation cache is pre-configured") {
                cacheManager.cacheNames shouldContain "ipAddrGeoLocation"
              }

              Then("geolocation cache is two-level over Redis with jittered TTL") {
                val cache =
                    cacheManager.getCache("ipAddrGeoLocation").shouldBeInstanceOf<TwoLevelCache>()
                cache.remote
                    .shouldBeInstanceOf<RedisCache>()
                    .cacheConfiguration
                    .ttlFunction
                    .shouldBeInstanceOf<JitteredTtl>()
              }
            }
          }
        },
    )
