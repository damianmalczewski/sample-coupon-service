package com.example.samplecoupon.usecase

import com.example.samplecoupon.domain.CouponRepository
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class CouponFetchUseCase(private val couponRepository: CouponRepository) {

  @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
  fun fetchCoupon(query: FetchCouponQuery): CouponDto {
    val coupon = couponRepository.fetchById(query.id)
    return CouponDto(
        id = coupon.id!!.toString(),
        code = coupon.code,
        usageLimit = coupon.usageLimit,
        usageCount = coupon.usageCount,
        countryCode = coupon.countryCode,
        createdAt = coupon.createdAt!!,
    )
  }
}

data class FetchCouponQuery(val id: UUID)

data class CouponDto(
    val id: String,
    val code: String,
    val usageLimit: Int,
    val usageCount: Int,
    val countryCode: String,
    val createdAt: Instant,
)
