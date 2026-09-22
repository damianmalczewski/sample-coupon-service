package com.example.samplecoupon.common

abstract class CommonException(error: CommonError) : DomainException(error) {

  class FailedToLocalizeClient : CommonException(CommonError.FAILED_TO_LOCALIZE_CLIENT)
}
