package com.example.samplecoupon.usecase

import com.example.samplecoupon.ApplicationTest
import com.example.samplecoupon.beforeEachGiven
import com.example.samplecoupon.domain.Coupon
import com.example.samplecoupon.domain.CouponException
import com.example.samplecoupon.domain.CouponRedemption
import com.example.samplecoupon.domain.GeoLocation
import com.example.samplecoupon.infra.repository.CouponJpaRepository
import com.example.samplecoupon.infra.repository.CouponRedemptionJpaRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.UUID

@ApplicationTest
class CouponRedeemUseCaseSpec(
    couponRepository: CouponJpaRepository,
    couponRedemptionRepository: CouponRedemptionJpaRepository,
    useCase: CouponRedeemUseCase,
) :
    BehaviorSpec(
        {
          val redeemedUserId = UUID.randomUUID()
          val redeemedRequestId = UUID.randomUUID()
          val plGeoLocation = GeoLocation(ipAddr = "127.0.0.1", countryCode = "PL")

          fun redeem(
              code: String,
              userId: UUID = UUID.randomUUID(),
              requestId: UUID? = null,
          ) =
              useCase.redeemCoupon(
                  RedeemCouponCommand(
                      code = code,
                      userId = userId,
                      geoLocation = plGeoLocation,
                      requestId = requestId,
                  ),
              )

          fun usageCountOf(code: String) = couponRepository.fetchByCode(code).usageCount

          beforeEachGiven {
            couponRedemptionRepository.deleteAll()
            couponRepository.deleteAll()

            couponRepository.saveAll(
                listOf(
                    Coupon(code = "WINTER", usageLimit = 10, countryCode = "PL"),
                    Coupon(code = "SPRING", usageLimit = 20, countryCode = "GB"),
                    Coupon(code = "SUMMER", usageLimit = 30, countryCode = "US"),
                    Coupon(code = "AUTUMN", usageLimit = 40, countryCode = "DE"),
                    Coupon(
                        code = "EXHAUSTED",
                        usageLimit = 5,
                        usageCount = 5,
                        countryCode = "PL",
                    ),
                ),
            )
            val redeemedCoupon =
                couponRepository.save(
                    Coupon(code = "REDEEMED", usageLimit = 10, countryCode = "PL"),
                )
            couponRedemptionRepository.save(
                CouponRedemption(
                    coupon = redeemedCoupon,
                    userId = redeemedUserId,
                    requestId = redeemedRequestId,
                ),
            )
          }

          Given("an available coupon") {
            When("redeeming it") {
              redeem("WINTER")

              Then("its usage count is incremented") {
                couponRepository.findAll().first { it.code == "WINTER" }.usageCount shouldBe 1
              }
            }
          }

          Given("an available coupon and a request id") {
            val requestId = UUID.randomUUID()

            When("redeeming it") {
              val identity = redeem("WINTER", requestId = requestId)

              Then("it returns the request id") { identity.id shouldBe requestId.toString() }

              Then("it stores the redemption under the request id") {
                couponRedemptionRepository.findByRequestId(requestId).shouldNotBeNull()
              }
            }
          }

          Given("an available coupon and no request id") {
            When("redeeming it") {
              val identity = redeem("WINTER")

              Then("it generates the request id and stores the redemption under it") {
                couponRedemptionRepository
                    .findByRequestId(UUID.fromString(identity.id))
                    .shouldNotBeNull()
              }
            }
          }

          Given("a coupon already redeemed by the request") {
            When("repeating the request") {
              val identity = redeem("REDEEMED", redeemedUserId, redeemedRequestId)

              Then("it returns the same request id") {
                identity.id shouldBe redeemedRequestId.toString()
              }

              Then("it does not redeem the coupon again") {
                usageCountOf("REDEEMED") shouldBe 0
                couponRedemptionRepository.count() shouldBe 1
              }
            }
          }

          Given("a coupon whose last usage was taken by the request") {
            val userId = UUID.randomUUID()
            val requestId = UUID.randomUUID()
            couponRepository.save(Coupon(code = "LAST", usageLimit = 1, countryCode = "PL"))
            redeem("LAST", userId, requestId)

            When("repeating the request") {
              val identity = redeem("LAST", userId, requestId)

              Then("it succeeds instead of reporting the usage limit") {
                identity.id shouldBe requestId.toString()
                usageCountOf("LAST") shouldBe 1
              }
            }
          }

          Given("a request id already used for a different user") {
            When("redeeming the same coupon with it") {
              Then("it throws CouponUsageLimitReached") {
                shouldThrow<CouponException.CouponUsageLimitReached> {
                  redeem("REDEEMED", UUID.randomUUID(), redeemedRequestId)
                }
              }
            }
          }

          Given("a request id already used for a different coupon") {
            When("redeeming another coupon with it") {
              Then("it throws CouponUsageLimitReached") {
                shouldThrow<CouponException.CouponUsageLimitReached> {
                  redeem("WINTER", redeemedUserId, redeemedRequestId)
                }
                usageCountOf("WINTER") shouldBe 0
              }
            }
          }

          Given("a coupon already redeemed by the user") {
            When("redeeming it again") {
              Then("it throws CouponUsageLimitReached") {
                shouldThrow<CouponException.CouponUsageLimitReached> {
                  redeem("REDEEMED", redeemedUserId)
                }
              }
            }
          }

          Given("a non-existent coupon code") {
            When("redeeming it") {
              Then("it throws CouponByCodeNotExists") {
                shouldThrow<CouponException.CouponByCodeNotExists> { redeem("UNKNOWN") }
              }
            }
          }

          Given("a coupon with exhausted usage limit") {
            When("redeeming it") {
              Then("it throws CouponUsageLimitReached") {
                shouldThrow<CouponException.CouponUsageLimitReached> { redeem("EXHAUSTED") }
              }
            }
          }
        },
    )
