package com.example.samplecoupon.infrastructure

import jakarta.validation.constraints.Min
import java.time.Duration
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "sample-coupon-service.caching")
data class CachingProperties(
    @field:DurationMin(nanos = 0) val ipAddrGeoLocationTtl: Duration,

    /**
     * Upper bound of random extra added to each entry's TTL, so entries written together do not
     * expire together.
     */
    @field:DurationMin(nanos = 0) val ipAddrGeoLocationTtlJitter: Duration,

    /**
     * TTL of in-memory cache in front of Redis. Not invalidated across instances, so keep it short.
     */
    @field:DurationMin(nanos = 1) val localTtl: Duration,

    /** Maximum number of entries per in-memory cache. */
    @field:Min(1) val localMaxSize: Long,
)

@ConfigurationProperties(prefix = "sample-coupon-service.clock")
data class ClockProperties(

    /**
     * Time zone used for the application clock. Accepted formats:
     * - IANA region ID: `"Europe/Warsaw"`, `"America/New_York"`, `"UTC"`,
     * - Zone offset: `"Z"`, `"+01:00"`, `"-05:30"`,
     * - Prefixed offset: `"UTC+01:00"`, `"GMT-05:00"`.
     */
    val zone: String,
)

@ConfigurationProperties(prefix = "sample-coupon-service.geolocation")
data class GeoLocationProperties(
    val endpoint: String,
)

@Validated
@ConfigurationProperties(prefix = "sample-coupon-service.rest-client")
data class RestClientProperties(
    @field:DurationMin(nanos = 1) val connectTimeout: Duration,
    @field:DurationMin(nanos = 1) val readTimeout: Duration,
)
