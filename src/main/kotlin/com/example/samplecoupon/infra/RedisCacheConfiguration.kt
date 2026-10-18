package com.example.samplecoupon.infra

import com.example.samplecoupon.infra.cache.JitteredTtl
import com.example.samplecoupon.infra.cache.TwoLevelCacheManager
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration as RedisCacheSettings
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
import org.springframework.data.redis.serializer.RedisSerializer

@ConditionalOnProperty("spring.cache.type", havingValue = "redis", matchIfMissing = true)
@Configuration
@EnableCaching
class RedisCacheConfiguration {

  @Bean
  fun twoLevelCacheManager(
      connectionFactory: RedisConnectionFactory,
      properties: CachingProperties,
  ): TwoLevelCacheManager {
    val rootConfig =
        RedisCacheSettings.defaultCacheConfig()
            .serializeKeysWith(SerializationPair.fromSerializer(RedisSerializer.string()))
            .serializeValuesWith(SerializationPair.fromSerializer(RedisSerializer.json()))

    val redisCacheManager =
        RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(rootConfig)
            .withInitialCacheConfigurations(
                mapOf(
                    "ipAddrGeoLocation" to
                        rootConfig.entryTtl(
                            JitteredTtl(
                                properties.ipAddrGeoLocationTtl,
                                properties.ipAddrGeoLocationTtlJitter,
                            ),
                        ),
                ),
            )
            .build()

    return TwoLevelCacheManager(redisCacheManager) {
      Caffeine.newBuilder()
          .expireAfterWrite(properties.localTtl)
          .maximumSize(properties.localMaxSize)
          .build()
    }
  }
}
