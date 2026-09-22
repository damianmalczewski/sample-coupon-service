package com.example.samplecoupon.infra.data

import com.example.samplecoupon.infra.data.DataJpaConfiguration.Companion.CLOCK_DATE_TIME_PROVIDER
import java.time.Clock
import java.util.Optional
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = CLOCK_DATE_TIME_PROVIDER)
class DataJpaConfiguration {

  @Bean(CLOCK_DATE_TIME_PROVIDER)
  fun clockDateTimeProvider(clock: Clock): DateTimeProvider {
    return DateTimeProvider { Optional.of(clock.instant()) }
  }

  companion object {
    const val CLOCK_DATE_TIME_PROVIDER = "clockDateTimeProvider"
  }
}
