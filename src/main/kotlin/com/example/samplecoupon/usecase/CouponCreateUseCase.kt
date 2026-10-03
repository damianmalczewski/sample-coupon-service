package com.example.samplecoupon.usecase

import com.example.samplecoupon.common.Identity
import com.example.samplecoupon.domain.Coupon
import com.example.samplecoupon.domain.CouponRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class CouponCreateUseCase(private val couponRepository: CouponRepository) {

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun createCoupon(request: CreateCouponCommand): Identity {
    var coupon =
        Coupon(
            code = request.code.uppercase(),
            usageLimit = request.usageLimit,
            countryCode = request.countryCode.uppercase(),
        )

    coupon = couponRepository.insert(coupon)

    return Identity(coupon.id!!.toString())
  }
}

data class CreateCouponCommand(val code: String, val usageLimit: Int, val countryCode: String)
