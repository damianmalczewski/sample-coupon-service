package com.example.samplecoupon.common

data class Identity(val id: String)

data class PageResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
)
