package com.example.samplecoupon.infra

import java.time.Clock
import java.time.ZoneId
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClockConfiguration {

  @Bean fun clock(properties: ClockProperties): Clock = Clock.system(ZoneId.of(properties.zone))
}
