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
import io.kotest.matchers.shouldBe
import java.util.UUID

@ApplicationTest
class CouponRedeemUseCaseSpec(
    couponRepository: CouponJpaRepository,
    couponRedemptionRepository: CouponRedemptionJpaRepository,
    useCase: CouponRedeemUseCase,
) :
    BehaviorSpec({
      val redeemedUserId = UUID.randomUUID()
      val plGeoLocation = GeoLocation(ipAddr = "127.0.0.1", countryCode = "PL")

      fun redeem(code: String, userId: UUID = UUID.randomUUID()) =
          useCase.redeemCoupon(
              RedeemCouponCommand(code = code, userId = userId, geoLocation = plGeoLocation)
          )

      beforeEachGiven {
        couponRedemptionRepository.deleteAll()
        couponRepository.deleteAll()

        couponRepository.saveAll(
            listOf(
                Coupon(code = "WINTER", usageLimit = 10, countryCode = "PL"),
                Coupon(code = "SPRING", usageLimit = 20, countryCode = "GB"),
                Coupon(code = "SUMMER", usageLimit = 30, countryCode = "US"),
                Coupon(code = "AUTUMN", usageLimit = 40, countryCode = "DE"),
                Coupon(code = "EXHAUSTED", usageLimit = 5, usageCount = 5, countryCode = "PL"),
            )
        )
        val redeemedCoupon =
            couponRepository.save(Coupon(code = "REDEEMED", usageLimit = 10, countryCode = "PL"))
        couponRedemptionRepository.save(
            CouponRedemption(coupon = redeemedCoupon, userId = redeemedUserId)
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
    })
