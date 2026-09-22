package com.example.samplecoupon.domain

import com.example.samplecoupon.common.DomainException

abstract class CouponException(error: CouponError) : DomainException(error) {

  class CouponByCodeNotExists : DomainException(CouponError.COUPON_BY_CODE_NOT_EXISTS)

  class CouponAlreadyExists : DomainException(CouponError.COUPON_ALREADY_EXISTS)

  class CouponUnavailableInCountry : DomainException(CouponError.COUPON_UNAVAILABLE_IN_COUNTRY)

  class CouponUsageLimitReached : DomainException(CouponError.COUPON_USAGE_LIMIT_REACHED)
}
