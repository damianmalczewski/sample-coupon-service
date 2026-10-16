package com.example.samplecoupon.infra

import com.example.samplecoupon.common.DomainException
import com.example.samplecoupon.common.ErrorCategory
import com.example.samplecoupon.common.ErrorType
import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.web.ProblemContextSettings
import io.github.problem4j.spring.web.resolver.problemResolver
import io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
import io.github.problem4j.spring.webmvc.adviceWebMvcInspector
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
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
  fun loggingProblemInspector(meterRegistry: MeterRegistry) = adviceWebMvcInspector { problem, ex ->
    val tags = mutableListOf<Tag>()

    var builder = if (problem.httpStatus.is5xxServerError) log.atError() else log.atWarn()
    builder =
        builder
            .addKeyValue("status", problem.httpStatus.value())
            .addKeyValue("exception", ex.javaClass.name)
    tags.add(Tag.of("status", problem.httpStatus.value().toString()))
    tags.add(Tag.of("exception", ex.javaClass.name))

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
  fun extendedProblemContextWebMvcFilter(settings: ProblemContextSettings) =
      object : ProblemContextWebMvcFilter(settings) {

        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain,
        ) {
          try {
            super.doFilterInternal(request, response, filterChain)
          } finally {
            MDC.remove("traceId")
          }
        }

        override fun assignContextAttributes(
            request: HttpServletRequest,
            response: HttpServletResponse,
            context: ProblemContext,
        ) {
          super.assignContextAttributes(request, response, context)
          MDC.put("traceId", context.get("traceId"))
        }
      }

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
