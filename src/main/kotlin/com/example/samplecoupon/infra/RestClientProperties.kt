package com.example.samplecoupon.infra

import jakarta.validation.constraints.Positive
import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sample-coupon-service.rest-client")
data class RestClientProperties(
    @field:Positive val connectTimeout: Duration,
    @field:Positive val readTimeout: Duration,
)
