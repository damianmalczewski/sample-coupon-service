package com.example.samplecoupon.common

interface ErrorType {
  val category: ErrorCategory
  val name: String
  val detail: String

  val message: String
    get() = "$category.$name: $detail"
}

enum class ErrorCategory {
  BAD_REQUEST,
  NOT_FOUND,
  CONFLICT,
  INTERNAL_ERROR,
}

abstract class DomainException(
    val error: ErrorType,
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message ?: error.message, cause)
