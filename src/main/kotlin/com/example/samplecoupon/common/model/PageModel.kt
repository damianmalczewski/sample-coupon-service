package com.example.samplecoupon.common.model

import com.example.samplecoupon.common.PageResult

data class PageModel<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
) {

  companion object {
    @JvmStatic
    fun <S, T> of(result: PageResult<S>, mapper: (S) -> T): PageModel<T> =
        PageModel(
            content = result.content.map(mapper),
            page = result.page,
            size = result.size,
            totalElements = result.totalElements,
        )
  }
}
