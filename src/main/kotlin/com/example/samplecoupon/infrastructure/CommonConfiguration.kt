package com.example.samplecoupon.infrastructure

import java.net.http.HttpClient
import java.time.Clock
import java.time.ZoneId
import java.util.Optional
import java.util.concurrent.Executors
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = DATE_TIME_PROVIDER)
class CommonConfiguration {

  @Bean
  fun restClient(properties: RestClientProperties): RestClient {
    val nativeClient = HttpClient.newBuilder().connectTimeout(properties.connectTimeout).build()
    val executor = Executors.newVirtualThreadPerTaskExecutor()

    val requestFactory = JdkClientHttpRequestFactory(nativeClient, executor)
    requestFactory.setReadTimeout(properties.readTimeout)

    return RestClient.builder().requestFactory(requestFactory).build()
  }

  @Bean fun clock(properties: ClockProperties): Clock = Clock.system(ZoneId.of(properties.zone))

  @Bean(DATE_TIME_PROVIDER)
  fun dateTimeProvider(clock: Clock): DateTimeProvider {
    return DateTimeProvider { Optional.of(clock.instant()) }
  }
}

private const val DATE_TIME_PROVIDER = "dateTimeProvider"
