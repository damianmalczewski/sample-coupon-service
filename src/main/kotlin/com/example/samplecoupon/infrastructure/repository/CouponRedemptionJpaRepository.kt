package com.example.samplecoupon.infrastructure.repository

import com.example.samplecoupon.domain.CouponException
import com.example.samplecoupon.domain.CouponRedemption
import com.example.samplecoupon.domain.CouponRedemptionRepository
import java.util.UUID
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CouponRedemptionJpaRepository :
    CouponRedemptionRepository, JpaRepository<CouponRedemption, UUID> {

  @Query("SELECT r FROM CouponRedemption r WHERE r.requestId = :requestId")
  override fun findByRequestId(requestId: UUID): CouponRedemption?

  override fun insert(couponRedemption: CouponRedemption): CouponRedemption {
    try {
      return saveAndFlush(couponRedemption)
    } catch (e: DataIntegrityViolationException) {
      if (e.cause is ConstraintViolationException) {
        val cause = e.cause as ConstraintViolationException
        if (cause.constraintName in UK_COUPON_REDEMPTIONS) {
          throw CouponException.CouponUsageLimitReached()
        }
      }
      throw e
    }
  }

  private companion object {
    private val UK_COUPON_REDEMPTIONS =
        setOf("uk_coupon_redemptions_coupon_user", "uk_coupon_redemptions_request_id")
  }
}
