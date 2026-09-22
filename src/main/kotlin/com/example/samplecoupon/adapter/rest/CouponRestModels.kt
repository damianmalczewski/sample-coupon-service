package com.example.samplecoupon.adapter.rest

import com.example.samplecoupon.common.json.NormalizeFormat
import com.example.samplecoupon.common.json.NormalizeString
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateCouponRequest(
    //
    @Size(min = 1, max = 255, message = "size must be between 1 and 255")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "must be alphanumeric")
    @NormalizeString(trim = true, normalizeFormat = NormalizeFormat.UPPERCASE) //
    val code: String,
    //
    @Min(1, message = "must be greater than or equal to 1") //
    val usageLimit: Int,
    //
    @Size(min = 1, max = 8, message = "size must be between 1 and 8")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "must be alphabetic")
    @NormalizeString(trim = true, normalizeFormat = NormalizeFormat.UPPERCASE) //
    val countryCode: String,
)

data class RedeemCouponRequest(
    //
    @Size(min = 1, max = 255, message = "size must be between 1 and 255")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "must be alphanumeric")
    @NormalizeString(trim = true, normalizeFormat = NormalizeFormat.UPPERCASE) //
    val code: String,
    //
    val userId: UUID,
)
