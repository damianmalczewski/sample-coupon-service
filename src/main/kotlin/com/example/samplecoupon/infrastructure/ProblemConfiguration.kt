package com.example.samplecoupon.infrastructure

import com.example.samplecoupon.common.DomainException
import com.example.samplecoupon.common.ErrorCategory
import com.example.samplecoupon.common.ErrorType
import io.github.problem4j.core.Problem
import io.github.problem4j.spring.web.SimpleTypeNameMapper
import io.github.problem4j.spring.web.TypeNameEntry
import io.github.problem4j.spring.web.TypeNameMapper
import io.github.problem4j.spring.web.resolver.problemResolver
import io.github.problem4j.spring.webmvc.ProblemInspector
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import java.time.LocalDate
import java.time.LocalTime
import java.time.Year
import java.time.YearMonth
import java.time.temporal.Temporal
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

@Configuration
class ProblemConfiguration {

  @Bean
  fun domainExceptionResolver() =
      problemResolver<DomainException> { ex ->
        status(ex.error.status.value())
        detail(ex.error.detail)
        extension("code", ex.error.name)
      }

  @Bean
  fun loggingProblemInspector(meterRegistry: MeterRegistry) =
      ProblemInspector { _, problem, ex, _, _, _ ->
        val tags = mutableListOf<Tag>()

        var builder =
            if (problem.httpStatus.is5xxServerError) log.atError().setCause(ex) else log.atWarn()
        builder =
            builder
                .addKeyValue("status", problem.httpStatus.value())
                .addKeyValue("exception", ex.javaClass.simpleName)
        tags.add(Tag.of("status", problem.httpStatus.value().toString()))
        tags.add(Tag.of("exception", ex.javaClass.simpleName))

        if (problem.extensions["code"] != null) {
          builder = builder.addKeyValue("code", problem.extensions["code"])
          tags.add(Tag.of("code", problem.extensions["code"].toString()))
        } else {
          tags.add(Tag.of("code", "UNKNOWN"))
        }

        if (log.isDebugEnabled) {
          builder = builder.setCause(ex)
        }

        meterRegistry.counter("sample-coupon-service.rest.controller.exceptions", tags).increment()
        builder.log("An exception occurred within @RestController stack")
      }

  @Bean
  fun typeNameMapper(): TypeNameMapper =
      SimpleTypeNameMapper(
          TypeNameEntry(UUID::class.java, "uuid"),
          TypeNameEntry(Temporal::class.java, "date-time"),
          TypeNameEntry(LocalDate::class.java, "date"),
          TypeNameEntry(LocalTime::class.java, "time"),
          TypeNameEntry(Year::class.java, "year"),
          TypeNameEntry(YearMonth::class.java, "year-month"),
      )

  private companion object {
    private val log = LoggerFactory.getLogger(ProblemConfiguration::class.java)
  }
}

private val ErrorType.status: HttpStatus
  get() =
      when (category) {
        ErrorCategory.BAD_REQUEST -> HttpStatus.BAD_REQUEST
        ErrorCategory.NOT_FOUND -> HttpStatus.NOT_FOUND
        ErrorCategory.CONFLICT -> HttpStatus.CONFLICT
        ErrorCategory.INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
      }

private val Problem.httpStatus: HttpStatusCode
  get() = HttpStatusCode.valueOf(status)
