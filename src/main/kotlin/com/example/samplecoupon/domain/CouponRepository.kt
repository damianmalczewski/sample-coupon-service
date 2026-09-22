package com.example.samplecoupon.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CouponRepository {

  /** Returns coupons ordered by creation date ascending. */
  fun findAll(pageable: Pageable): Page<Coupon>

  /**
   * Fetches coupon by code. Lookup is case-insensitive; code matched uppercase.
   *
   * @throws CouponException.CouponByCodeNotExists if no coupon with given code exists
   */
  fun fetchByCode(code: String): Coupon

  /**
   * Persists new coupon.
   *
   * @throws CouponException.CouponAlreadyExists if coupon with same code already exists
   */
  fun insert(coupon: Coupon): Coupon

  /**
   * Marks single redemption of coupon. Atomically increments usage count only if usage limit not
   * yet reached, resolving race condition on concurrent redemptions. Does not check coupon
   * existence - caller must fetch (e.g. via [fetchByCode]) beforehand, else a missing code throws
   * same exception as limit reached.
   *
   * @throws CouponException.CouponUsageLimitReached if coupon's usage limit was reached or code
   *   does not exist
   */
  fun lockRedemption(code: String)
}
