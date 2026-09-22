package com.example.samplecoupon.adapter.rest

import com.example.samplecoupon.domain.GeoLocationClient
import com.example.samplecoupon.usecase.CouponRedeemUseCase
import com.example.samplecoupon.usecase.RedeemCouponCommand
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
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
  ) {
    val geoLocation = geoLocationClient.findLocation(request.clientIpAddr)
    couponRedeemUseCase.redeemCoupon(
        RedeemCouponCommand(
            code = requestBody.code,
            userId = requestBody.userId,
            geoLocation = geoLocation,
        )
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
