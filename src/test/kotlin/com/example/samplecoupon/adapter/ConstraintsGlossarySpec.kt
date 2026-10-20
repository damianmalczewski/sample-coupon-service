package com.example.samplecoupon.adapter

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import jakarta.validation.Validation
import jakarta.validation.Validator

class ConstraintsGlossarySpec :
    BehaviorSpec(
        {
          val validator: Validator = Validation.buildDefaultValidatorFactory().validator

          fun violationsOf(bean: Any): List<String> = validator.validate(bean).map { it.message }

          Given("@ValidQueryPage") {
            listOf(0, 1, 1_000).forEach { page ->
              When("page is $page") {
                Then("it is valid") { violationsOf(QueryPageHolder(page)).shouldBeEmpty() }
              }
            }

            When("page is negative") {
              Then("it violates minimum") {
                violationsOf(QueryPageHolder(-1))
                    .shouldContainExactlyInAnyOrder("must be greater than or equal to 0")
              }
            }
          }

          Given("@ValidQuerySize") {
            listOf(1, 20, 500).forEach { size ->
              When("size is $size") {
                Then("it is valid") { violationsOf(QuerySizeHolder(size)).shouldBeEmpty() }
              }
            }

            When("size is below minimum") {
              Then("it violates minimum") {
                violationsOf(QuerySizeHolder(0))
                    .shouldContainExactlyInAnyOrder("must be greater than or equal to 1")
              }
            }

            When("size is above maximum") {
              Then("it violates maximum") {
                violationsOf(QuerySizeHolder(501))
                    .shouldContainExactlyInAnyOrder("must be less than or equal to 500")
              }
            }
          }

          Given("@ValidCountryCode") {
            listOf("P", "PL", "pl", "ABCDEFGH").forEach { countryCode ->
              When("country code is $countryCode") {
                Then("it is valid") { violationsOf(CountryCodeHolder(countryCode)).shouldBeEmpty() }
              }
            }

            When("country code is empty") {
              Then("it violates size only") {
                violationsOf(CountryCodeHolder(""))
                    .shouldContainExactlyInAnyOrder("size must be between 1 and 8")
              }
            }

            When("country code is too long") {
              Then("it violates size") {
                violationsOf(CountryCodeHolder("ABCDEFGHI"))
                    .shouldContainExactlyInAnyOrder("size must be between 1 and 8")
              }
            }

            listOf("P1", "P-L", "P L").forEach { countryCode ->
              When("country code '$countryCode' is not alphabetic") {
                Then("it violates pattern") {
                  violationsOf(CountryCodeHolder(countryCode))
                      .shouldContainExactlyInAnyOrder("must be alphabetic")
                }
              }
            }

            When("country code is too long and not alphabetic") {
              Then("it violates both size and pattern") {
                violationsOf(CountryCodeHolder("123456789"))
                    .shouldContainExactlyInAnyOrder(
                        "size must be between 1 and 8",
                        "must be alphabetic",
                    )
              }
            }
          }

          Given("@ValidCouponCode") {
            listOf("A", "SUMMER2026", "summer2026", "A".repeat(255)).forEach { code ->
              When("code is ${code.take(16)} (length ${code.length})") {
                Then("it is valid") { violationsOf(CouponCodeHolder(code)).shouldBeEmpty() }
              }
            }

            When("code is null") {
              Then("it is valid, as nullability is not part of the constraint") {
                violationsOf(NullableCouponCodeHolder(null)).shouldBeEmpty()
              }
            }

            When("code is empty") {
              Then("it violates size only") {
                violationsOf(CouponCodeHolder(""))
                    .shouldContainExactlyInAnyOrder("size must be between 1 and 255")
              }
            }

            When("code is too long") {
              Then("it violates size") {
                violationsOf(CouponCodeHolder("A".repeat(256)))
                    .shouldContainExactlyInAnyOrder("size must be between 1 and 255")
              }
            }

            listOf("SUMMER-26", "SUMMER 26", "ZŁOTO26", "-12").forEach { code ->
              When("code '$code' is not alphanumeric") {
                Then("it violates pattern") {
                  violationsOf(CouponCodeHolder(code))
                      .shouldContainExactlyInAnyOrder("must be alphanumeric")
                }
              }
            }

            When("code is too long and not alphanumeric") {
              Then("it violates both size and pattern") {
                violationsOf(CouponCodeHolder("-".repeat(256)))
                    .shouldContainExactlyInAnyOrder(
                        "size must be between 1 and 255",
                        "must be alphanumeric",
                    )
              }
            }
          }

          mapOf<String, (String) -> Any>(
                  "@ValidCouponId" to ::CouponIdHolder,
                  "@ValidUserId" to ::UserIdHolder,
                  "@ValidRedemptionRequestId" to ::RedemptionRequestIdHolder,
              )
              .forEach { (annotation, holder) ->
                Given(annotation) {
                  listOf(
                          "00000000-0000-0000-0000-000000000000",
                          "123e4567-e89b-12d3-a456-426614174000",
                          "123E4567-E89B-12D3-A456-426614174000",
                      )
                      .forEach { uuid ->
                        When("value is $uuid") {
                          Then("it is valid") { violationsOf(holder(uuid)).shouldBeEmpty() }
                        }
                      }

                  listOf(
                          "",
                          "not-a-uuid",
                          "123e4567e89b12d3a456426614174000",
                          "123e4567-e89b-12d3-a456-42661417400",
                          "123e4567-e89b-12d3-a456-4266141740000",
                          "123e4567-e89b-12d3-a456-42661417400g",
                          " 123e4567-e89b-12d3-a456-426614174000",
                      )
                      .forEach { value ->
                        When("value is '$value'") {
                          Then("it violates pattern") {
                            violationsOf(holder(value))
                                .shouldContainExactlyInAnyOrder("must be a UUID")
                          }
                        }
                      }
                }
              }

          Given("@ValidCouponUsageLimit") {
            listOf(1, 5, Int.MAX_VALUE).forEach { usageLimit ->
              When("usage limit is $usageLimit") {
                Then("it is valid") {
                  violationsOf(CouponUsageLimitHolder(usageLimit)).shouldBeEmpty()
                }
              }
            }

            listOf(0, -1).forEach { usageLimit ->
              When("usage limit is $usageLimit") {
                Then("it violates minimum") {
                  violationsOf(CouponUsageLimitHolder(usageLimit))
                      .shouldContainExactlyInAnyOrder("must be greater than or equal to 1")
                }
              }
            }
          }
        },
    )

private data class QueryPageHolder(@ValidQueryPage val value: Int)

private data class QuerySizeHolder(@ValidQuerySize val value: Int)

private data class CountryCodeHolder(@ValidCountryCode val value: String)

private data class CouponCodeHolder(@ValidCouponCode val value: String)

private data class NullableCouponCodeHolder(@ValidCouponCode val value: String?)

private data class CouponIdHolder(@ValidCouponId val value: String)

private data class UserIdHolder(@ValidUserId val value: String)

private data class RedemptionRequestIdHolder(@ValidRedemptionRequestId val value: String?)

private data class CouponUsageLimitHolder(@ValidCouponUsageLimit val value: Int)
