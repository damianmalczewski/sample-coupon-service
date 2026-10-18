package com.example.samplecoupon.infra

import com.example.samplecoupon.common.DomainException
import com.example.samplecoupon.common.ErrorCategory
import com.example.samplecoupon.common.ErrorType
import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.web.AttributeSupport
import io.github.problem4j.spring.web.ProblemContextSettings
import io.github.problem4j.spring.web.SimpleTypeNameMapper
import io.github.problem4j.spring.web.TypeNameMapper
import io.github.problem4j.spring.web.resolver.problemResolver
import io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
import io.github.problem4j.spring.webmvc.adviceWebMvcInspector
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.Year
import java.time.YearMonth
import java.time.temporal.Temporal
import java.util.Optional
import java.util.UUID
import kotlin.reflect.KClass
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
  fun problemContextWebMvcFilter(settings: ProblemContextSettings) =
      problemContextWebMvcFilter(settings) {
        onContext { _, _, context -> MDC.put("traceId", context.get("traceId")) }
        onComplete { _, _, _ -> MDC.remove("traceId") }
      }

  @Bean
  fun problemTypeNameMapper(): TypeNameMapper =
      simpleTypeNameMapper(
          UUID::class to "uuid",
          Temporal::class to "date-time",
          LocalDate::class to "date",
          LocalTime::class to "time",
          Year::class to "year",
          YearMonth::class to "year-month",
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

// TODO: Simplified backport of `io.github.problem4j.spring.webmvc.problemContextWebMvcFilter` and
//  `io.github.problem4j.spring.webmvc.problemContext`, which are not available in problem4j-spring
//  3.1.0-M1. Delete declarations below and import the library ones instead after upgrading
//  problem4j-spring to 3.1.0 (call sites stay the same).

private class ProblemContextWebMvcFilterSpec {

  val contextCallbacks =
      mutableListOf<(HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit>()
  val completeCallbacks =
      mutableListOf<(HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit>()

  fun onContext(callback: (HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit) {
    contextCallbacks.add(callback)
  }

  fun onComplete(callback: (HttpServletRequest, HttpServletResponse, ProblemContext) -> Unit) {
    completeCallbacks.add(callback)
  }
}

private fun problemContextWebMvcFilter(
    settings: ProblemContextSettings,
    configure: ProblemContextWebMvcFilterSpec.() -> Unit,
): ProblemContextWebMvcFilter {
  val spec = ProblemContextWebMvcFilterSpec().apply(configure)
  return object : ProblemContextWebMvcFilter(settings) {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
      try {
        super.doFilterInternal(request, response, filterChain)
      } finally {
        val context = request.problemContext
        if (context != null) {
          spec.completeCallbacks.forEach { it(request, response, context) }
        }
      }
    }

    override fun assignContextAttributes(
        request: HttpServletRequest,
        response: HttpServletResponse,
        context: ProblemContext,
    ) {
      super.assignContextAttributes(request, response, context)
      spec.contextCallbacks.forEach { it(request, response, context) }
    }
  }
}

private val HttpServletRequest.problemContext: ProblemContext?
  get() = getAttribute(AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE) as? ProblemContext

// TODO: Simplified backport of `io.github.problem4j.spring.web.simpleTypeNameMapper`, which is not
//  available in problem4j-spring 3.1.0-M1. Delete declaration below and import the library one
//  instead after upgrading problem4j-spring to 3.1.0 (call sites stay the same).

private fun simpleTypeNameMapper(vararg mappings: Pair<KClass<*>, String>): TypeNameMapper {
  val customMappings = mappings.associate { (type, name) -> type.javaObjectType to name }
  val defaults = SimpleTypeNameMapper()
  return TypeNameMapper { type ->
    if (type == null) {
      return@TypeNameMapper Optional.empty()
    }
    val boxed = type.kotlin.javaObjectType
    customMappings.entries
        .firstOrNull { (mappedType, _) -> mappedType == type || mappedType.isAssignableFrom(boxed) }
        ?.let { Optional.of(it.value) } ?: defaults.map(type)
  }
}
