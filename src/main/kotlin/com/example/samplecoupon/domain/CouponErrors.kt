package com.example.samplecoupon.domain

import com.example.samplecoupon.common.DomainException
import com.example.samplecoupon.common.ErrorCategory
import com.example.samplecoupon.common.ErrorType

enum class CouponError(override val category: ErrorCategory, override val detail: String) :
    ErrorType {
  COUPON_ALREADY_EXISTS(ErrorCategory.CONFLICT, "coupon with the same code already exists"),
  COUPON_BY_ID_NOT_EXISTS(ErrorCategory.NOT_FOUND, "coupon with the given id does not exist"),
  COUPON_BY_CODE_NOT_EXISTS(
      ErrorCategory.BAD_REQUEST,
      "coupon with the given code does not exist",
  ),
  COUPON_UNAVAILABLE_IN_COUNTRY(
      ErrorCategory.BAD_REQUEST,
      "coupon unavailable in the country of the user",
  ),
  COUPON_USAGE_LIMIT_REACHED(ErrorCategory.BAD_REQUEST, "coupon usage limit has been reached"),
}

sealed class CouponException(error: CouponError) : DomainException(error) {

  class CouponByIdNotExists : CouponException(CouponError.COUPON_BY_ID_NOT_EXISTS)

  class CouponByCodeNotExists : CouponException(CouponError.COUPON_BY_CODE_NOT_EXISTS)

  class CouponAlreadyExists : CouponException(CouponError.COUPON_ALREADY_EXISTS)

  class CouponUnavailableInCountry : CouponException(CouponError.COUPON_UNAVAILABLE_IN_COUNTRY)

  class CouponUsageLimitReached : CouponException(CouponError.COUPON_USAGE_LIMIT_REACHED)
}
