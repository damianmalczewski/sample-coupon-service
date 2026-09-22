package com.example.samplecoupon.usecase

import com.example.samplecoupon.common.PageResult
import com.example.samplecoupon.domain.CouponRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class CouponQueryUseCase(private val couponRepository: CouponRepository) {

  @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
  fun queryCoupons(pageable: Pageable): PageResult<CouponDto> {
    val page = couponRepository.findAll(pageable)
    return PageResult(
        content =
            page.content.map {
              CouponDto(
                  id = it.id!!.toString(),
                  code = it.code,
                  usageLimit = it.usageLimit,
                  usageCount = it.usageCount,
                  createdAt = it.createdAt!!,
              )
            },
        page = page.number,
        size = page.size,
        totalElements = page.totalElements,
    )
  }
}
