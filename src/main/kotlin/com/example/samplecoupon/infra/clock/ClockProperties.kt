package com.example.samplecoupon.infra.clock

import org.springframework.boot.context.properties.ConfigurationProperties

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
