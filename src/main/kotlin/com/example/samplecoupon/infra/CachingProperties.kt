package com.example.samplecoupon.infra

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sample-coupon-service.caching")
data class CachingProperties(val ipAddrGeoLocationTtl: Duration)
