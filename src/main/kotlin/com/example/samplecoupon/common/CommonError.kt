package com.example.samplecoupon.common

enum class CommonError(override val category: ErrorCategory, override val detail: String) :
    ErrorType {
  FAILED_TO_LOCALIZE_CLIENT(
      ErrorCategory.INTERNAL_ERROR,
      "failed to establish client localization",
  ),
}
