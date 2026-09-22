package com.example.samplecoupon.adapter.rest.problem

import com.example.samplecoupon.common.DomainException
import com.example.samplecoupon.common.ErrorCategory
import com.example.samplecoupon.common.ErrorType
import io.github.problem4j.core.Problem
import io.github.problem4j.core.ProblemContext
import io.github.problem4j.spring.web.resolver.ProblemResolver
import java.lang.Exception
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component

@Component
class DomainExceptionResolver : ProblemResolver {

  override fun getExceptionClass(): Class<out Exception> = DomainException::class.java

  override fun resolve(
      context: ProblemContext,
      ex: Exception,
      headers: HttpHeaders,
      status: HttpStatusCode,
  ): Problem =
      Problem.builder()
          .status((ex as DomainException).error.status.value())
          .detail(ex.error.detail)
          .extension("code", ex.error.name)
          .build()

  private val ErrorType.status: HttpStatus
    get() =
        when (category) {
          ErrorCategory.BAD_REQUEST -> HttpStatus.BAD_REQUEST
          ErrorCategory.NOT_FOUND -> HttpStatus.NOT_FOUND
          ErrorCategory.CONFLICT -> HttpStatus.CONFLICT
          ErrorCategory.INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
        }
}
