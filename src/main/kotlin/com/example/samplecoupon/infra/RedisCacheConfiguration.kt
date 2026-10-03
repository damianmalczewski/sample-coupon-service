package com.example.samplecoupon.infra

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
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
  fun cacheManager(
      connectionFactory: RedisConnectionFactory,
      properties: CachingProperties,
  ): CacheManager {
    val rootConfig =
        RedisCacheSettings.defaultCacheConfig()
            .serializeKeysWith(SerializationPair.fromSerializer(RedisSerializer.string()))
            .serializeValuesWith(SerializationPair.fromSerializer(RedisSerializer.json()))

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(rootConfig)
        .withInitialCacheConfigurations(
            mapOf("ipAddrGeoLocation" to rootConfig.entryTtl(properties.ipAddrGeoLocationTtl))
        )
        .build()
  }
}
