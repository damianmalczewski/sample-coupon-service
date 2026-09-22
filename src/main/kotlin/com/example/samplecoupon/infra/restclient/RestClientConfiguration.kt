package com.example.samplecoupon.infra.restclient

import java.net.http.HttpClient
import java.util.concurrent.Executors
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfiguration {

  @Bean
  fun restClient(properties: RestClientProperties): RestClient {
    val nativeClient = HttpClient.newBuilder().connectTimeout(properties.connectTimeout).build()
    val executor = Executors.newVirtualThreadPerTaskExecutor()

    val requestFactory = JdkClientHttpRequestFactory(nativeClient, executor)
    requestFactory.setReadTimeout(properties.readTimeout)

    return RestClient.builder().requestFactory(requestFactory).build()
  }
}
