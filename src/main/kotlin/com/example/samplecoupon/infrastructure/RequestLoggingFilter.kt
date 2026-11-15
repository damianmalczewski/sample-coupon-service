package com.example.samplecoupon.infrastructure

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Logs every HTTP request exactly once, after it is handled: method, path, status and duration.
 *
 * A filter (not an interceptor) is used on purpose - it also sees requests that never reach a
 * controller (static resources, unmapped paths, failures in other filters) and measures the final
 * status after error handling. Runs right after the observation filter, so the trace id is already
 * in the logging MDC.
 *
 * If the request failed with an exception handled by Problem4J, it is stored under
 * [EXCEPTION_ATTRIBUTE] (see [ProblemConfiguration]) and added to the entry.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class RequestLoggingFilter : OncePerRequestFilter() {

  override fun shouldNotFilter(request: HttpServletRequest): Boolean =
      request.requestURI.startsWith("/actuator")

  override fun doFilterInternal(
      request: HttpServletRequest,
      response: HttpServletResponse,
      filterChain: FilterChain,
  ) {
    val startNanos = System.nanoTime()
    try {
      filterChain.doFilter(request, response)
    } finally {
      logRequest(request, response.status, (System.nanoTime() - startNanos) / 1_000_000)
    }
  }

  private fun logRequest(request: HttpServletRequest, status: Int, durationMs: Long) {
    val level =
        when {
          status >= 500 -> Level.ERROR
          status >= 400 -> Level.WARN
          else -> Level.INFO
        }
    if (!log.isEnabledForLevel(level)) {
      return
    }

    val exception = request.getAttribute(EXCEPTION_ATTRIBUTE) as? Exception
    var builder =
        log.atLevel(level)
            .addKeyValue("http.method", request.method)
            .addKeyValue("http.path", request.requestURI)
            .addKeyValue("http.status", status)
            .addKeyValue("http.durationMs", durationMs)
    request.getAttribute(CODE_ATTRIBUTE)?.let { builder = builder.addKeyValue("problem.code", it) }
    if (exception != null) {
      builder = builder.addKeyValue("http.exception", exception.javaClass.simpleName)
      if (status >= 500 || log.isDebugEnabled) {
        builder = builder.setCause(exception)
      }
      builder.log(
          "HTTP request {} {} responded in {}ms with {}: {} {}",
          request.method,
          request.requestURI,
          durationMs,
          status,
          exception.javaClass.simpleName,
          exception.message,
      )
    } else {
      builder.log(
          "HTTP request {} {} responded in {}ms with {}",
          request.method,
          request.requestURI,
          durationMs,
          status,
      )
    }
  }

  companion object {
    const val EXCEPTION_ATTRIBUTE = "sample-coupon-service.request.exception"
    const val CODE_ATTRIBUTE = "sample-coupon-service.request.code"

    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)
  }
}
