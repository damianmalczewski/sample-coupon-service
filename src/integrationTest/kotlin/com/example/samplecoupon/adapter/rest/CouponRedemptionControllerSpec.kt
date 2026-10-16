package com.example.samplecoupon.adapter.rest

import com.example.samplecoupon.ApplicationTest
import com.example.samplecoupon.beforeEachGiven
import com.example.samplecoupon.domain.Coupon
import com.example.samplecoupon.domain.CouponError
import com.example.samplecoupon.domain.CouponRedemption
import com.example.samplecoupon.infra.repository.CouponJpaRepository
import com.example.samplecoupon.infra.repository.CouponRedemptionJpaRepository
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withGivens
import java.util.UUID
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import tools.jackson.databind.json.JsonMapper

@ApplicationTest
class CouponRedemptionControllerSpec(
    restClient: RestTestClient,
    couponRepository: CouponJpaRepository,
    couponRedemptionRepository: CouponRedemptionJpaRepository,
    jsonMapper: JsonMapper,
    wireMock: WireMockServer,
) :
    BehaviorSpec({
      val redeemedUserId = UUID.randomUUID()

      fun stubGeoLocation(ipAddr: String) {
        wireMock.stubFor(
            get(urlPathTemplate("/wiremock/geolocation/$ipAddr"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonMapper.writeValueAsString(mapOf("countryCode" to "PL")))
                )
        )
      }

      beforeEachGiven {
        wireMock.resetAll()
        stubGeoLocation("127.0.0.1")

        couponRepository.deleteAll()
        couponRepository.saveAll(
            listOf(
                Coupon(code = "WINTER", usageLimit = 10, countryCode = "PL"),
                Coupon(code = "EXHAUSTED", usageLimit = 1, usageCount = 1, countryCode = "PL"),
            )
        )
        val redeemedCoupon =
            couponRepository.save(Coupon(code = "REDEEMED", usageLimit = 10, countryCode = "PL"))
        couponRedemptionRepository.save(
            CouponRedemption(coupon = redeemedCoupon, userId = redeemedUserId)
        )
      }

      fun redeemCoupon(body: Map<String, *>, forwardedFor: String? = null) =
          restClient
              .post()
              .uri("/api/v1/coupon-redemptions")
              .contentType(MediaType.APPLICATION_JSON)
              .headers { headers -> forwardedFor?.let { headers.set("X-Forwarded-For", it) } }
              .body(body)
              .exchange()

      fun RestTestClient.ResponseSpec.expectBadRequest(problem: Map<String, *>) {
        expectStatus().isBadRequest().expectBody<Map<String, *>>().isEqualTo(problem)
      }

      fun couponProblem(error: CouponError) =
          mapOf(
              "title" to "Bad Request",
              "status" to 400,
              "detail" to error.detail,
              "code" to error.name,
          )

      Given("an available coupon") {
        When("POST redeem coupon without X-Forwarded-For header") {
          val response = redeemCoupon(mapOf("code" to "WINTER", "userId" to UUID.randomUUID()))

          Then("it resolves client IP from remoteAddr and returns 201") {
            response.expectStatus().isCreated()
          }
        }
      }

      Given("an available coupon and a client behind a proxy") {
        wireMock.resetAll()
        stubGeoLocation("192.168.1.1")

        When("POST redeem coupon with X-Forwarded-For header") {
          val response =
              redeemCoupon(
                  mapOf("code" to "WINTER", "userId" to UUID.randomUUID()),
                  forwardedFor = "192.168.1.1, 10.0.0.1",
              )

          Then("it resolves client IP from X-Forwarded-For and returns 201") {
            response.expectStatus().isCreated()
          }
        }
      }

      Given("a non-existent coupon code") {
        When("POST redeem coupon") {
          val response = redeemCoupon(mapOf("code" to "UNKNOWN", "userId" to UUID.randomUUID()))

          Then("it returns 400") {
            response.expectBadRequest(couponProblem(CouponError.COUPON_BY_CODE_NOT_EXISTS))
          }
        }
      }

      Given("a coupon with exhausted usage limit") {
        When("POST redeem coupon") {
          val response = redeemCoupon(mapOf("code" to "EXHAUSTED", "userId" to UUID.randomUUID()))

          Then("it returns 400") {
            response.expectBadRequest(couponProblem(CouponError.COUPON_USAGE_LIMIT_REACHED))
          }
        }
      }

      Given("a coupon already redeemed by the user") {
        When("POST redeem coupon again") {
          val response = redeemCoupon(mapOf("code" to "REDEEMED", "userId" to redeemedUserId))

          Then("it returns 400") {
            response.expectBadRequest(couponProblem(CouponError.COUPON_USAGE_LIMIT_REACHED))
          }
        }
      }

      withGivens(
          nameFn = { (field, value, _) -> "an invalid request body field $field='$value'" },
          Triple("code", "", "size must be between 1 and 255"),
          Triple("code", " ", "size must be between 1 and 255"),
          Triple("code", "a".repeat(256), "size must be between 1 and 255"),
          Triple("code", "-12", "must be alphanumeric"),
      ) { (field, value, error) ->
        When("POST redeem coupon") {
          val response =
              redeemCoupon(
                  mapOf("code" to "WINTER", "userId" to UUID.randomUUID()) + (field to value)
              )

          Then("it returns 400") {
            response.expectBadRequest(
                mapOf(
                    "title" to "Bad Request",
                    "status" to 400,
                    "detail" to "validation failed",
                    "errors" to listOf(mapOf("field" to field, "error" to error)),
                )
            )
          }
        }
      }

      withGivens(
          nameFn = { (missing, _) -> "a request body without $missing" },
          "code" to mapOf<String, Any>("userId" to UUID.randomUUID()),
          "userId" to mapOf<String, Any>("code" to "WINTER"),
      ) { (missing, body) ->
        When("POST redeem coupon") {
          val response = redeemCoupon(body)

          Then("it returns 400") {
            response.expectBadRequest(
                mapOf(
                    "title" to "Bad Request",
                    "status" to 400,
                    "detail" to "type mismatch",
                    "property" to missing,
                )
            )
          }
        }
      }
    })
