package com.example.samplecoupon.infra

import java.time.Duration
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "sample-coupon-service.caching")
data class CachingProperties(@field:DurationMin(nanos = 0) val ipAddrGeoLocationTtl: Duration)

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
