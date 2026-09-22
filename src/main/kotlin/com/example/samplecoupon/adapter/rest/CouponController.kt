package com.example.samplecoupon.adapter.rest

import com.example.samplecoupon.common.Identity
import com.example.samplecoupon.common.PageResult
import com.example.samplecoupon.usecase.CouponCreateUseCase
import com.example.samplecoupon.usecase.CouponDto
import com.example.samplecoupon.usecase.CouponQueryUseCase
import com.example.samplecoupon.usecase.CreateCouponCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/coupons")
@Validated
class CouponController(
    private val couponQueryUseCase: CouponQueryUseCase,
    private val couponCreateUseCase: CouponCreateUseCase,
) {

  @GetMapping(produces = [APPLICATION_JSON_VALUE])
  fun getCoupons(
      @RequestParam(defaultValue = "0")
      @Min(0, message = "must be greater than or equal to 0")
      page: Int,
      @RequestParam(defaultValue = "20")
      @Min(1, message = "must be greater than or equal to 1")
      @Max(500, message = "must be less than or equal to 500")
      size: Int,
  ): PageResult<CouponDto> {
    return couponQueryUseCase.queryCoupons(PageRequest.of(page, size))
  }

  @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
  @ResponseStatus(CREATED)
  fun createCoupon(@RequestBody @Valid requestBody: CreateCouponRequest): Identity {
    return couponCreateUseCase.createCoupon(
        CreateCouponCommand(
            code = requestBody.code,
            usageLimit = requestBody.usageLimit,
            countryCode = requestBody.countryCode,
        )
    )
  }
}
