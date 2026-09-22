package com.example.samplecoupon.infra.data

import com.example.samplecoupon.domain.Coupon
import com.example.samplecoupon.domain.CouponException
import com.example.samplecoupon.domain.CouponRepository
import java.util.UUID
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CouponJpaRepository : CouponRepository, JpaRepository<Coupon, UUID> {

  @Query("SELECT c FROM Coupon c ORDER BY c.createdAt ASC")
  override fun findAll(pageable: Pageable): Page<Coupon>

  override fun fetchByCode(code: String): Coupon {
    return findByCodeInternal(code.uppercase()) ?: throw CouponException.CouponByCodeNotExists()
  }

  @Query("SELECT c FROM Coupon c WHERE c.code = :code")
  fun findByCodeInternal(code: String): Coupon?

  override fun insert(coupon: Coupon): Coupon {
    try {
      return saveAndFlush(coupon)
    } catch (e: DataIntegrityViolationException) {
      if (e.cause is ConstraintViolationException) {
        val cause = e.cause as ConstraintViolationException
        if (cause.constraintName == UK_COUPON_CODE) {
          throw CouponException.CouponAlreadyExists()
        }
      }
      throw e
    }
  }

  override fun lockRedemption(code: String) {
    if (lockRedemptionInternal(code) == 0) {
      throw CouponException.CouponUsageLimitReached()
    }
  }

  @Modifying
  @Query(
      """
      UPDATE Coupon c
      SET c.usageCount = c.usageCount + 1
      WHERE c.code = :code
        AND c.usageCount < c.usageLimit
      """
  )
  fun lockRedemptionInternal(code: String): Int

  private companion object {
    private const val UK_COUPON_CODE = "uk_coupons_coupon_code"
  }
}
