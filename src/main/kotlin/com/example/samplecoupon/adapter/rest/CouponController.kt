package com.example.samplecoupon.adapter.rest

import com.example.samplecoupon.common.json.NormalizeFormat
import com.example.samplecoupon.common.json.NormalizeString
import com.example.samplecoupon.common.model.PageModel
import com.example.samplecoupon.usecase.CouponCreateUseCase
import com.example.samplecoupon.usecase.CouponDto
import com.example.samplecoupon.usecase.CouponQueryUseCase
import com.example.samplecoupon.usecase.CreateCouponCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant
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
  ): PageModel<CouponResponse> {
    val result = couponQueryUseCase.queryCoupons(PageRequest.of(page, size))
    return PageModel.of(result) { it.toResponse() }
  }

  @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
  @ResponseStatus(CREATED)
  fun createCoupon(@RequestBody @Valid requestBody: CreateCouponRequest): CreateCouponResponse {
    val identity =
        couponCreateUseCase.createCoupon(
            CreateCouponCommand(
                code = requestBody.code,
                usageLimit = requestBody.usageLimit,
                countryCode = requestBody.countryCode,
            )
        )
    return CreateCouponResponse(id = identity.id)
  }

  private fun CouponDto.toResponse(): CouponResponse =
      CouponResponse(
          id = id,
          code = code,
          usageLimit = usageLimit,
          usageCount = usageCount,
          createdAt = createdAt,
      )
}

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

data class CreateCouponResponse(val id: String)

data class CouponResponse(
    val id: String,
    val code: String,
    val usageLimit: Int,
    val usageCount: Int,
    val createdAt: Instant,
)
