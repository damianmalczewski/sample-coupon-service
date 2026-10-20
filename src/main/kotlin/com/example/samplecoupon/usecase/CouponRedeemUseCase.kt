package com.example.samplecoupon.usecase

import com.example.samplecoupon.common.Identity
import com.example.samplecoupon.domain.CouponException
import com.example.samplecoupon.domain.CouponRedemptionRepository
import com.example.samplecoupon.domain.CouponRepository
import com.example.samplecoupon.domain.GeoLocation
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class CouponRedeemUseCase(
    private val couponRepository: CouponRepository,
    private val couponRedemptionRepository: CouponRedemptionRepository,
) {

  /**
   * Redeems coupon, idempotent by request id. A repeated request with the id of an already made
   * redemption of the same coupon and user succeeds again without redeeming the coupon again. Only
   * committed redemptions are recognized - a repeat overlapping the original request is rejected
   * like a second redemption, and succeeds when sent again after the original ends.
   *
   * @return identity holding the request id of the redemption, generated if not given
   * @throws CouponException.IdempotencyFailed if request id was already used by a different user
   * @throws CouponException.CouponUsageLimitReached if request id was already used by the same user
   *   for a different coupon, as for any other repeated redemption
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun redeemCoupon(request: RedeemCouponCommand): Identity {
    val coupon = couponRepository.fetchByCode(request.code)

    val existing = request.requestId?.let { couponRedemptionRepository.findByRequestId(it) }
    if (existing != null) {
      if (!existing.isBy(request.userId)) {
        throw CouponException.IdempotencyFailed()
      }
      if (!existing.isOf(coupon)) {
        throw CouponException.CouponUsageLimitReached()
      }
      return Identity(existing.requestId.toString())
    }

    coupon.validateGeoLocation(request.geoLocation)
    couponRepository.lockRedemption(request.code)
    val redemption =
        couponRedemptionRepository.insert(
            coupon.redeem(request.userId, request.requestId ?: UUID.randomUUID()),
        )
    return Identity(redemption.requestId.toString())
  }
}

data class RedeemCouponCommand(
    val code: String,
    val userId: UUID,
    val geoLocation: GeoLocation,
    val requestId: UUID? = null,
)
