package com.example.samplecoupon.usecase

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

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun redeemCoupon(request: RedeemCouponCommand) {
    val coupon = couponRepository.fetchByCode(request.code)
    coupon.validateGeoLocation(request.geoLocation)
    couponRepository.lockRedemption(request.code)
    val redemption = coupon.redeem(request.userId)
    couponRedemptionRepository.insert(redemption)
  }
}

data class RedeemCouponCommand(
    val code: String,
    val userId: UUID,
    val geoLocation: GeoLocation,
)
