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
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalTime
import java.time.Year
import java.time.YearMonth
import java.time.temporal.Temporal
import java.util.UUID
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.RequestAttributes

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
  fun metricsProblemInspector(meterRegistry: MeterRegistry) =
      ProblemInspector { _, problem, ex, _, _, request ->
        val code = problem.extensions["code"]?.toString()

        request.setAttribute(
            RequestLoggingFilter.EXCEPTION_ATTRIBUTE,
            ex,
            RequestAttributes.SCOPE_REQUEST,
        )
        if (code != null) {
          request.setAttribute(
              RequestLoggingFilter.CODE_ATTRIBUTE,
              code,
              RequestAttributes.SCOPE_REQUEST,
          )
        }

        incrementProblemsCounter(meterRegistry, problem, ex, code)
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
}

private val ErrorType.status: HttpStatus
  get() =
      when (category) {
        ErrorCategory.BAD_REQUEST -> HttpStatus.BAD_REQUEST
        ErrorCategory.NOT_FOUND -> HttpStatus.NOT_FOUND
        ErrorCategory.CONFLICT -> HttpStatus.CONFLICT
        ErrorCategory.INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
      }

private fun incrementProblemsCounter(
    meterRegistry: MeterRegistry,
    problem: Problem,
    ex: Exception,
    code: String?,
) =
    meterRegistry
        .counter(
            "sample-coupon-service.rest.controller.problems",
            listOf(
                Tag.of("status", problem.status.toString()),
                Tag.of("exception", ex.javaClass.simpleName),
                Tag.of("code", code ?: "UNKNOWN"),
            ),
        )
        .increment()
