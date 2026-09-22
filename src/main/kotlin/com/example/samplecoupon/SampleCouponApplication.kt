package com.example.samplecoupon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication //
class SampleCouponApplication

fun main(args: Array<String>) {
  runApplication<SampleCouponApplication>(*args)
}
