package com.example.samplecoupon.infra

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sample-coupon-service.geolocation")
data class GeoLocationProperties(
    val endpoint: String,
)
