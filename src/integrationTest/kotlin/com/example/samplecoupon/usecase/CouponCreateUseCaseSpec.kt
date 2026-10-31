package com.example.samplecoupon.usecase

import com.example.samplecoupon.ApplicationTest
import com.example.samplecoupon.beforeEachGiven
import com.example.samplecoupon.domain.Coupon
import com.example.samplecoupon.domain.CouponException
import com.example.samplecoupon.infrastructure.repository.CouponJpaRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.UUID

@ApplicationTest
class CouponCreateUseCaseSpec(
    couponRepository: CouponJpaRepository,
    useCase: CouponCreateUseCase,
) :
    BehaviorSpec(
        {
          beforeEachGiven {
            couponRepository.deleteAll()
            couponRepository.saveAll(
                listOf(
                    Coupon(code = "WINTER", usageLimit = 10, countryCode = "PL"),
                    Coupon(code = "SPRING", usageLimit = 20, countryCode = "GB"),
                    Coupon(code = "SUMMER", usageLimit = 30, countryCode = "US"),
                    Coupon(code = "AUTUMN", usageLimit = 40, countryCode = "DE"),
                ),
            )
          }

          Given("a command with a new coupon code") {
            val command = CreateCouponCommand(code = "HOLIDAY", usageLimit = 50, countryCode = "PL")

            When("creating the coupon") {
              val identity = useCase.createCoupon(command)

              Then("the coupon is stored under the returned identity") {
                val entity = couponRepository.findById(UUID.fromString(identity.id)).orElseThrow()
                entity.code shouldBe "HOLIDAY"
                entity.usageLimit shouldBe 50
                entity.usageCount shouldBe 0
                entity.createdAt.shouldNotBeNull()
              }
            }
          }

          Given("a command with an already existing coupon code") {
            val command = CreateCouponCommand(code = "WINTER", usageLimit = 99, countryCode = "PL")

            When("creating the coupon") {
              Then("it throws CouponAlreadyExists") {
                shouldThrow<CouponException.CouponAlreadyExists> {
                  useCase.createCoupon(command)
                }
              }
            }
          }
        },
    )
