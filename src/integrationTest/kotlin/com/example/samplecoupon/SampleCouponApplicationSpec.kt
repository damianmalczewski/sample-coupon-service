package com.example.samplecoupon

import io.kotest.core.spec.style.BehaviorSpec

@ApplicationTest
class SampleCouponApplicationSpec :
    BehaviorSpec({ Given("the application") { Then("the context loads") {} } })
