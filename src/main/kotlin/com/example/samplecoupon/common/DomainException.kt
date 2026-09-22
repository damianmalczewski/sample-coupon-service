package com.example.samplecoupon.common

abstract class DomainException(
    val error: ErrorType,
    message: String? = null,
    cause: Throwable? = null,
) :
    RuntimeException(
        if (message == null) message else error.message,
        cause,
    )
