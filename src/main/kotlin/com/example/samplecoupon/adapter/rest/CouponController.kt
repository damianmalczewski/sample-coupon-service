package com.example.samplecoupon.adapter.rest

import com.example.samplecoupon.adapter.ValidCountryCode
import com.example.samplecoupon.adapter.ValidCouponCode
import com.example.samplecoupon.adapter.ValidCouponId
import com.example.samplecoupon.adapter.ValidCouponUsageLimit
import com.example.samplecoupon.adapter.ValidQueryPage
import com.example.samplecoupon.adapter.ValidQuerySize
import com.example.samplecoupon.common.NormalizeFormat
import com.example.samplecoupon.common.NormalizeString
import com.example.samplecoupon.usecase.CouponCreateUseCase
import com.example.samplecoupon.usecase.CouponDto
import com.example.samplecoupon.usecase.CouponFetchUseCase
import com.example.samplecoupon.usecase.CouponListItemDto
import com.example.samplecoupon.usecase.CouponQueryUseCase
import com.example.samplecoupon.usecase.CouponsQuery
import com.example.samplecoupon.usecase.CreateCouponCommand
import com.example.samplecoupon.usecase.FetchCouponQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.time.Instant
import java.util.UUID
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Coupon API", description = "Create and browse coupons")
@RestController
@RequestMapping("/api/v1/coupons")
@Validated
class CouponController(
    private val couponQueryUseCase: CouponQueryUseCase,
    private val couponFetchUseCase: CouponFetchUseCase,
    private val couponCreateUseCase: CouponCreateUseCase,
) {

  @Operation(
      operationId = "listCoupons",
      summary = "List coupons",
      description =
          "Returns a page of coupons with their usage statistics. Optionally filtered by coupon code (case-insensitive).",
  )
  @GetMapping(produces = [APPLICATION_JSON_VALUE])
  fun listCoupons(
      @RequestParam(defaultValue = "0") @ValidQueryPage page: Int,
      @RequestParam(defaultValue = "20") @ValidQuerySize size: Int,
      @RequestParam(required = false)
      @Schema(example = "SUMMER2026")
      @ValidCouponCode
      code: String?,
  ): PageModel<CouponListItem> {
    val result =
        couponQueryUseCase.queryCoupons(
            CouponsQuery(pageable = PageRequest.of(page, size), code = code),
        )
    return result.toPageModel { it.toListItem() }
  }

  @Operation(
      operationId = "getCoupon",
      summary = "Get coupon",
      description = "Returns a single coupon with its usage statistics.",
  )
  @GetMapping("/{id}", produces = [APPLICATION_JSON_VALUE])
  fun getCoupon(@PathVariable @ValidCouponId id: String): CouponResponse =
      couponFetchUseCase.fetchCoupon(FetchCouponQuery(id = UUID.fromString(id))).toResponse()

  @Operation(
      operationId = "createCoupon",
      summary = "Create coupon",
      description =
          "Creates a coupon with a unique code, usage limit and the country it can be redeemed in.",
  )
  @PostMapping(consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
  @ResponseStatus(CREATED)
  fun createCoupon(@RequestBody @Valid requestBody: CreateCouponRequest): CreateCouponResponse {
    val identity =
        couponCreateUseCase.createCoupon(
            CreateCouponCommand(
                code = requestBody.code,
                usageLimit = requestBody.usageLimit,
                countryCode = requestBody.countryCode,
            ),
        )
    return CreateCouponResponse(id = identity.id)
  }

  private fun CouponListItemDto.toListItem(): CouponListItem =
      CouponListItem(
          id = id,
          code = code,
          usageLimit = usageLimit,
          usageCount = usageCount,
          countryCode = countryCode,
          createdAt = createdAt,
      )

  private fun CouponDto.toResponse(): CouponResponse =
      CouponResponse(
          id = id,
          code = code,
          usageLimit = usageLimit,
          usageCount = usageCount,
          countryCode = countryCode,
          createdAt = createdAt,
      )
}

data class CreateCouponRequest(
    //
    @Schema(example = "SUMMER2026")
    @NormalizeString(trim = true, normalizeFormat = NormalizeFormat.UPPERCASE)
    @ValidCouponCode
    val code: String,
    //
    @Schema(example = "5")
    @ValidCouponUsageLimit //
    val usageLimit: Int,
    //
    @Schema(example = "PL")
    @NormalizeString(trim = true, normalizeFormat = NormalizeFormat.UPPERCASE)
    @ValidCountryCode
    val countryCode: String,
)

data class CreateCouponResponse(
    @Schema(example = "00000000-0000-0000-0000-000000000000") val id: String,
)

data class CouponListItem(
    @Schema(example = "00000000-0000-0000-0000-000000000000") val id: String,
    @Schema(example = "SUMMER2026") val code: String,
    @Schema(example = "5") val usageLimit: Int,
    @Schema(example = "2") val usageCount: Int,
    @Schema(example = "PL") val countryCode: String,
    @Schema(example = "2026-06-26T12:33:42Z") val createdAt: Instant,
)

data class CouponResponse(
    @Schema(example = "00000000-0000-0000-0000-000000000000") val id: String,
    @Schema(example = "SUMMER2026") val code: String,
    @Schema(example = "5") val usageLimit: Int,
    @Schema(example = "2") val usageCount: Int,
    @Schema(example = "PL") val countryCode: String,
    @Schema(example = "2026-06-26T12:33:42Z") val createdAt: Instant,
)
