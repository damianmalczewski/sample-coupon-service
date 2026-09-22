package com.example.samplecoupon.adapter.rest.problem

import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.web.ProblemContextSettings
import io.github.problem4j.spring.webmvc.ProblemContextWebMvcFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component

/**
 * Populates `traceId`, provided by simple tracing feature from `problem4j-spring-webmvc` library
 * (not `micrometer-tracing`) to logs.
 */
@Component
class ExtendedProblemContextWebMvcFilter(settings: ProblemContextSettings) :
    ProblemContextWebMvcFilter(settings) {

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
