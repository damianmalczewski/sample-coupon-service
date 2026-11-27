package com.example.samplecoupon.usecase

import com.example.samplecoupon.ApplicationTest
import com.example.samplecoupon.beforeEachGiven
import com.example.samplecoupon.domain.Coupon
import com.example.samplecoupon.domain.CouponException
import com.example.samplecoupon.infrastructure.repository.CouponJpaRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.temporal.ChronoUnit
import java.util.UUID

@ApplicationTest
class CouponFetchUseCaseSpec(couponRepository: CouponJpaRepository, useCase: CouponFetchUseCase) :
    BehaviorSpec(
        {
          beforeEachGiven { couponRepository.deleteAll() }

          Given("a single coupon with usage") {
            val entity =
                couponRepository.save(
                    Coupon(
                        code = "TEST",
                        usageLimit = 100,
                        usageCount = 25,
                        countryCode = "PL",
                    ),
                )

            When("fetching coupon by its id") {
              val dto = useCase.fetchCoupon(FetchCouponQuery(entity.id!!))

              Then("it maps all entity fields to DTO") {
                dto.id shouldBe entity.id!!.toString()
                dto.code shouldBe "TEST"
                dto.usageLimit shouldBe 100
                dto.usageCount shouldBe 25
                dto.countryCode shouldBe "PL"
                dto.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe
                    entity.createdAt!!.truncatedTo(ChronoUnit.MILLIS)
              }
            }

            When("fetching coupon by unknown id") {
              Then("it throws CouponByIdNotExists") {
                shouldThrow<CouponException.CouponByIdNotExists> {
                  useCase.fetchCoupon(FetchCouponQuery(UUID.randomUUID()))
                }
              }
            }
          }
        },
    )
