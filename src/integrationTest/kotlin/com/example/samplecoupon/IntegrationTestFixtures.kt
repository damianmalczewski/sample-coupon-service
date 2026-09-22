package com.example.samplecoupon

import io.github.malczuuu.testclock.TestClock
import io.kotest.core.extensions.ApplyExtension
import io.kotest.extensions.spring.SpringExtension
import java.time.Duration
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock

@Target(CLASS)
@Retention(RUNTIME)
@ApplyExtension(SpringExtension::class)
@EnableWireMock(ConfigureWireMock(registerSpringBean = true, resetWireMockServer = false))
@ActiveProfiles("integrationTest")
@Import(ApplicationTestConfiguration::class)
@SpringBootTest(
    classes = [SampleCouponApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureRestTestClient
annotation class ApplicationTest(
    @get:AliasFor(annotation = SpringBootTest::class, attribute = "properties")
    val properties: Array<String> = []
)

@TestConfiguration
private class ApplicationTestConfiguration {

  @Bean
  @ServiceConnection
  fun postgresContainer(): PostgreSQLContainer =
      PostgreSQLContainer(DockerImageName.parse("postgres:18.6"))

  @Bean
  @Primary
  fun testClock(): TestClock =
      TestClock.from("2025-03-23T12:42:18Z").withTickDuration(Duration.ofSeconds(1))
}
