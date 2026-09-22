package com.example.samplecoupon.usecase

import java.time.Instant

data class CouponDto(
    val id: String,
    val code: String,
    val usageLimit: Int,
    val usageCount: Int,
    val createdAt: Instant,
)
