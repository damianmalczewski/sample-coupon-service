package com.example.samplecoupon.infra.repository

import com.example.samplecoupon.domain.CouponException
import com.example.samplecoupon.domain.CouponRedemption
import com.example.samplecoupon.domain.CouponRedemptionRepository
import java.util.UUID
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.JpaRepository

interface CouponRedemptionJpaRepository :
    CouponRedemptionRepository, JpaRepository<CouponRedemption, UUID> {

  override fun insert(couponRedemption: CouponRedemption): CouponRedemption {
    try {
      return saveAndFlush(couponRedemption)
    } catch (e: DataIntegrityViolationException) {
      if (e.cause is ConstraintViolationException) {
        val cause = e.cause as ConstraintViolationException
        if (cause.constraintName == UK_COUPON_REDEMPTIONS_UNIQUE) {
          throw CouponException.CouponUsageLimitReached()
        }
      }
      throw e
    }
  }

  private companion object {
    private const val UK_COUPON_REDEMPTIONS_UNIQUE = "uk_coupon_redemptions_coupon_user"
  }
}
