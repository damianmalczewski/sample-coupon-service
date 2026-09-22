package com.example.samplecoupon.common

interface ErrorType {

  val category: ErrorCategory

  val name: String

  val detail: String

  val message: String
    get() = "$category.$name: $detail"
}
