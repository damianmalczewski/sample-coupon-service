package com.example.samplecoupon.usecase

import com.example.samplecoupon.domain.GeoLocation
import java.util.UUID

data class CreateCouponCommand(val code: String, val usageLimit: Int, val countryCode: String)

data class RedeemCouponCommand(
    val code: String,
    val userId: UUID,
    val geoLocation: GeoLocation,
)
