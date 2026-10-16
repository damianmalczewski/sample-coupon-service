package com.example.samplecoupon.adapter.rest

import com.example.samplecoupon.ApplicationTest
import com.example.samplecoupon.beforeEachGiven
import com.example.samplecoupon.domain.Coupon
import com.example.samplecoupon.domain.CouponError
import com.example.samplecoupon.infra.repository.CouponJpaRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withGivens
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody

@ApplicationTest
class CouponControllerSpec(restClient: RestTestClient, couponRepository: CouponJpaRepository) :
    BehaviorSpec({
      beforeEachGiven {
        couponRepository.deleteAll()
        couponRepository.saveAll(
            listOf(
                Coupon(code = "WINTER", usageLimit = 10, countryCode = "PL"),
                Coupon(code = "SPRING", usageLimit = 20, countryCode = "GB"),
                Coupon(code = "SUMMER", usageLimit = 30, countryCode = "US"),
            )
        )
      }

      fun createCoupon(body: Map<String, *>) =
          restClient
              .post()
              .uri("/api/v1/coupons")
              .contentType(MediaType.APPLICATION_JSON)
              .body(body)
              .exchange()

      fun validationProblem(field: String, error: String) =
          mapOf(
              "title" to "Bad Request",
              "status" to 400,
              "detail" to "validation failed",
              "errors" to listOf(mapOf("field" to field, "error" to error)),
          )

      Given("existing coupons") {
        When("GET coupons with default pagination") {
          val response = restClient.get().uri("/api/v1/coupons").exchange()

          Then("it returns 200") { response.expectStatus().isOk() }
        }

        When("GET coupons with custom pagination") {
          val response = restClient.get().uri("/api/v1/coupons?page=0&size=2").exchange()

          Then("it returns 200") { response.expectStatus().isOk() }
        }

        When("POST create coupon with valid request") {
          val response =
              createCoupon(mapOf("code" to "HOLIDAY", "usageLimit" to 50, "countryCode" to "PL"))

          Then("it returns 201 with only the id in body") {
            response.expectStatus().isCreated().expectBody<Map<String, *>>().value { body ->
              body!!.keys shouldBe setOf("id")
              body["id"].shouldNotBeNull()
            }
          }
        }

        When("POST create coupon with duplicate code") {
          val response =
              createCoupon(mapOf("code" to "WINTER", "usageLimit" to 50, "countryCode" to "PL"))

          Then("it returns 409") {
            response
                .expectStatus()
                .isEqualTo(409)
                .expectBody<Map<String, *>>()
                .isEqualTo(
                    mapOf(
                        "title" to "Conflict",
                        "status" to 409,
                        "detail" to CouponError.COUPON_ALREADY_EXISTS.detail,
                        "code" to CouponError.COUPON_ALREADY_EXISTS.name,
                    )
                )
          }
        }
      }

      withGivens(
          nameFn = { (param, value, _) -> "an invalid query parameter $param=$value" },
          Triple("page", "-1", "must be greater than or equal to 0"),
          Triple("size", "-1", "must be greater than or equal to 1"),
          Triple("size", "0", "must be greater than or equal to 1"),
          Triple("size", "501", "must be less than or equal to 500"),
      ) { (param, value, error) ->
        When("GET coupons") {
          val response = restClient.get().uri("/api/v1/coupons?$param=$value").exchange()

          Then("it returns 400") {
            response
                .expectStatus()
                .isBadRequest()
                .expectBody<Map<String, *>>()
                .isEqualTo(validationProblem(param, error))
          }
        }
      }

      withGivens(
          nameFn = { (field, value, _) -> "an invalid request body field $field='$value'" },
          Triple("code", "", "size must be between 1 and 255"),
          Triple("code", " ", "size must be between 1 and 255"),
          Triple("code", "a".repeat(256), "size must be between 1 and 255"),
          Triple("code", "-12", "must be alphanumeric"),
          Triple("usageLimit", -1, "must be greater than or equal to 1"),
          Triple("usageLimit", 0, "must be greater than or equal to 1"),
          Triple("countryCode", "", "size must be between 1 and 8"),
          Triple("countryCode", " ", "size must be between 1 and 8"),
          Triple("countryCode", "a".repeat(9), "size must be between 1 and 8"),
          Triple("countryCode", "-12", "must be alphabetic"),
      ) { (field, value, error) ->
        When("POST create coupon") {
          val response =
              createCoupon(
                  mapOf("code" to "HOLIDAY", "usageLimit" to 50, "countryCode" to "PL") +
                      (field to value)
              )

          Then("it returns 400") {
            response
                .expectStatus()
                .isBadRequest()
                .expectBody<Map<String, *>>()
                .isEqualTo(validationProblem(field, error))
          }
        }
      }
    })
