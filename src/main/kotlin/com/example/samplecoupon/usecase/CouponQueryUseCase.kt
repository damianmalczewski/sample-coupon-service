package com.example.samplecoupon.usecase

import com.example.samplecoupon.common.PageResult
import com.example.samplecoupon.domain.CouponRepository
import java.time.Instant
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class CouponQueryUseCase(private val couponRepository: CouponRepository) {

  @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
  fun queryCoupons(query: CouponsQuery): PageResult<CouponListItemDto> {
    val page =
        if (query.code == null) {
          couponRepository.findAll(query.pageable)
        } else {
          couponRepository.findAllByCode(query.code, query.pageable)
        }
    return PageResult(
        content =
            page.content.map {
              CouponListItemDto(
                  id = it.id!!.toString(),
                  code = it.code,
                  usageLimit = it.usageLimit,
                  usageCount = it.usageCount,
                  countryCode = it.countryCode,
                  createdAt = it.createdAt!!,
              )
            },
        page = page.number,
        size = page.size,
        totalElements = page.totalElements,
    )
  }
}

data class CouponsQuery(
    val pageable: Pageable,
    val code: String? = null,
)

data class CouponListItemDto(
    val id: String,
    val code: String,
    val usageLimit: Int,
    val usageCount: Int,
    val countryCode: String,
    val createdAt: Instant,
)
