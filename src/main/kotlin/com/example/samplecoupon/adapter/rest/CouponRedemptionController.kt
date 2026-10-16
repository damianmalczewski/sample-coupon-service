package com.example.samplecoupon.adapter.rest

import com.example.samplecoupon.common.NormalizeFormat
import com.example.samplecoupon.common.NormalizeString
import com.example.samplecoupon.domain.GeoLocationClient
import com.example.samplecoupon.usecase.CouponRedeemUseCase
import com.example.samplecoupon.usecase.RedeemCouponCommand
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.UUID
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/coupon-redemptions")
@Validated
class CouponRedemptionController(
    private val couponRedeemUseCase: CouponRedeemUseCase,
    private val geoLocationClient: GeoLocationClient,
) {

  @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
  @ResponseStatus(CREATED)
  fun redeemCoupon(
      @RequestBody @Valid requestBody: RedeemCouponRequest,
      request: HttpServletRequest,
  ) = couponRedeemUseCase.redeemCoupon(requestBody.toCommand(request))

  private fun RedeemCouponRequest.toCommand(request: HttpServletRequest): RedeemCouponCommand {
    val geoLocation = geoLocationClient.findLocation(request.clientIpAddr)
    return RedeemCouponCommand(
        code = code,
        userId = userId,
        geoLocation = geoLocation,
    )
  }

  private val HttpServletRequest.clientIpAddr: String
    get() {
      return if (getHeader("X-Forwarded-For") == null) {
        remoteAddr
      } else {
        getHeader("X-Forwarded-For").split(",").first().trim()
      }
    }
}

data class RedeemCouponRequest(
    //
    @Size(min = 1, max = 255, message = "size must be between 1 and 255")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "must be alphanumeric")
    @NormalizeString(trim = true, normalizeFormat = NormalizeFormat.UPPERCASE) //
    val code: String,
    //
    val userId: UUID,
)
