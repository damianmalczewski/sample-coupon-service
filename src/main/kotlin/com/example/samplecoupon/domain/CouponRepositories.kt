package com.example.samplecoupon.domain

import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CouponRepository {

  /** Returns coupons with usage left first, then exhausted ones, each alphabetically by code. */
  fun findAll(pageable: Pageable): Page<Coupon>

  /**
   * Returns coupons matching given code, ordered as [findAll]. Lookup is case-insensitive; code
   * matched uppercase. As codes are unique, page holds at most one coupon.
   */
  fun findAllByCode(code: String, pageable: Pageable): Page<Coupon>

  /**
   * Fetches coupon by id.
   *
   * @throws CouponException.CouponByIdNotExists if no coupon with given id exists
   */
  fun fetchById(id: UUID): Coupon

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

interface CouponRedemptionRepository {

  /** Finds redemption made by request with given id. */
  fun findByRequestId(requestId: UUID): CouponRedemption?

  /**
   * Persists coupon redemption record. Enforces one redemption per user per coupon and one
   * redemption per request id via unique constraints.
   *
   * @throws CouponException.CouponUsageLimitReached if user already redeemed this coupon or
   *   redemption with same request id already exists
   */
  fun insert(couponRedemption: CouponRedemption): CouponRedemption
}
