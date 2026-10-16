package com.example.samplecoupon.adapter.rest

import com.example.samplecoupon.common.PageResult
import io.swagger.v3.oas.annotations.media.Schema

data class PageModel<T>(
    val content: List<T>,
    @Schema(example = "0") val page: Int,
    @Schema(example = "20") val size: Int,
    @Schema(example = "37") val totalElements: Long,
)

fun <S, T> PageResult<S>.toPageModel(mapper: (S) -> T): PageModel<T> =
    PageModel(
        content = content.map(mapper),
        page = page,
        size = size,
        totalElements = totalElements,
    )

fun <S, T> Collection<S>.toPageModel(mapper: (S) -> T): PageModel<T> =
    PageModel(
        content = map(mapper),
        page = 0,
        size = size,
        totalElements = size.toLong(),
    )
