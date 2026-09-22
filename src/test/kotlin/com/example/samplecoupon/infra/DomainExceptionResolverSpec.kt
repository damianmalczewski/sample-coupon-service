package com.example.samplecoupon.infra

import com.example.samplecoupon.adapter.rest.problem.DomainExceptionResolver
import com.example.samplecoupon.common.DomainException
import com.example.samplecoupon.common.ErrorCategory
import com.example.samplecoupon.common.ErrorType
import io.github.problem4j.core.ProblemContext
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withGivens
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

class DomainExceptionResolverSpec :
    BehaviorSpec({
      val resolver = DomainExceptionResolver()

      fun resolve(error: DummyError) =
          resolver.resolve(
              ProblemContext.create(),
              DummyException(error),
              HttpHeaders(),
              HttpStatus.OK,
          )

      Given("a DomainExceptionResolver") {
        Then("its exception class is DomainException") {
          resolver.exceptionClass shouldBe DomainException::class.java
        }

        When("resolving a DomainException") {
          val problem = resolve(DummyError.BAD_REQUEST_ERROR)

          Then("the detail is taken from the error") {
            problem.detail shouldBe DummyError.BAD_REQUEST_ERROR.detail
          }

          Then("the code extension is taken from the error") {
            problem.extensions["code"] shouldBe DummyError.BAD_REQUEST_ERROR.name
          }
        }
      }

      withGivens(
          nameFn = { (error, status) -> "a ${error.category} error mapping to $status" },
          DummyError.BAD_REQUEST_ERROR to 400,
          DummyError.NOT_FOUND_ERROR to 404,
          DummyError.CONFLICT_ERROR to 409,
          DummyError.INTERNAL_ERROR to 500,
      ) { (error, status) ->
        When("resolving an exception of it") {
          val problem = resolve(error)

          Then("the problem status is $status") { problem.status shouldBe status }
        }
      }
    })

private enum class DummyError(
    override val category: ErrorCategory,
    override val detail: String,
) : ErrorType {
  BAD_REQUEST_ERROR(ErrorCategory.BAD_REQUEST, "bad request occurred"),
  NOT_FOUND_ERROR(ErrorCategory.NOT_FOUND, "resource not found"),
  CONFLICT_ERROR(ErrorCategory.CONFLICT, "conflict occurred"),
  INTERNAL_ERROR(ErrorCategory.INTERNAL_ERROR, "internal error occurred"),
}

private class DummyException(error: DummyError) : DomainException(error)
