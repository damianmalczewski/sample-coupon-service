package com.example.samplecoupon.adapter.rest.problem

import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.webmvc.AdviceWebMvcInspector
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import java.lang.Exception
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.context.request.WebRequest

@Component
class DiagnosticsInspector(private val meterRegistry: MeterRegistry) : AdviceWebMvcInspector {

  override fun inspect(
      context: ProblemContext,
      problem: Problem,
      ex: Exception,
      headers: HttpHeaders,
      status: HttpStatusCode,
      request: WebRequest,
  ) {
    val tags = mutableListOf<Tag>()

    var builder = if (status.is5xxServerError) log.atError() else log.atWarn()
    builder =
        builder.addKeyValue("status", status.value()).addKeyValue("exception", ex.javaClass.name)
    tags.add(Tag.of("status", status.value().toString()))
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

  private companion object {
    private val log = LoggerFactory.getLogger(DiagnosticsInspector::class.java)
  }
}
