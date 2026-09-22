package com.example.samplecoupon.domain

interface CouponRedemptionRepository {

  /**
   * Persists coupon redemption record. Enforces one redemption per user per coupon via unique
   * constraint.
   *
   * @throws CouponException.CouponUsageLimitReached if user already redeemed this coupon
   */
  fun insert(couponRedemption: CouponRedemption): CouponRedemption
}
