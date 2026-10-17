package com.example.samplecoupon.usecase

import com.example.samplecoupon.ApplicationTest
import com.example.samplecoupon.beforeEachGiven
import com.example.samplecoupon.domain.Coupon
import com.example.samplecoupon.infra.repository.CouponJpaRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withWhens
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.temporal.ChronoUnit
import org.springframework.data.domain.PageRequest

@ApplicationTest
class CouponQueryUseCaseSpec(couponRepository: CouponJpaRepository, useCase: CouponQueryUseCase) :
    BehaviorSpec(
        {
          beforeEachGiven { couponRepository.deleteAll() }

          Given("five coupons") {
            couponRepository.saveAll(
                listOf(
                    Coupon(code = "WINTER", usageLimit = 10, countryCode = "PL"),
                    Coupon(code = "SPRING", usageLimit = 20, countryCode = "GB"),
                    Coupon(code = "SUMMER", usageLimit = 30, countryCode = "US"),
                    Coupon(code = "AUTUMN", usageLimit = 40, countryCode = "DE"),
                    Coupon(code = "HOLIDAY", usageLimit = 50, countryCode = "FR"),
                ),
            )

            When("querying the first page of size 2") {
              val result = useCase.queryCoupons(CouponsQuery(PageRequest.of(0, 2)))

              Then("it returns the page metadata") {
                result.content shouldHaveSize 2
                result.page shouldBe 0
                result.size shouldBe 2
                result.totalElements shouldBe 5L
              }

              Then("it maps the first coupon to DTO") {
                val first = result.content[0]
                first.id.shouldNotBeNull()
                first.code shouldBe "WINTER"
                first.usageLimit shouldBe 10
                first.usageCount shouldBe 0
                first.createdAt.shouldNotBeNull()
              }
            }

            When("querying all coupons in a single page") {
              val result = useCase.queryCoupons(CouponsQuery(PageRequest.of(0, 10)))

              Then("it returns all coupons in insertion order") {
                result.content.map { it.code } shouldBe
                    listOf("WINTER", "SPRING", "SUMMER", "AUTUMN", "HOLIDAY")
                result.page shouldBe 0
                result.size shouldBe 10
                result.totalElements shouldBe 5L
              }
            }

            When("querying coupons by code in different case") {
              val result = useCase.queryCoupons(CouponsQuery(PageRequest.of(0, 10), "summer"))

              Then("it returns only the matching coupon") {
                result.content.map { it.code } shouldBe listOf("SUMMER")
                result.totalElements shouldBe 1L
              }
            }

            When("querying coupons by unknown code") {
              val result = useCase.queryCoupons(CouponsQuery(PageRequest.of(0, 10), "UNKNOWN"))

              Then("it returns empty page") {
                result.content shouldHaveSize 0
                result.totalElements shouldBe 0L
              }
            }

            withWhens(
                nameFn = { (page, size, _) -> "querying page $page of size $size" },
                Triple(1, 2, 2),
                Triple(2, 2, 1),
                Triple(0, 3, 3),
                Triple(10, 2, 0),
            ) { (page, size, expectedCount) ->
              val result = useCase.queryCoupons(CouponsQuery(PageRequest.of(page, size)))

              Then("it returns $expectedCount coupons out of 5") {
                result.content shouldHaveSize expectedCount
                result.page shouldBe page
                result.size shouldBe size
                result.totalElements shouldBe 5L
              }
            }
          }

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

            When("querying coupons") {
              val result = useCase.queryCoupons(CouponsQuery(PageRequest.of(0, 10)))

              Then("it maps all entity fields to DTO") {
                val dto = result.content.single()
                dto.id shouldBe entity.id!!.toString()
                dto.code shouldBe "TEST"
                dto.usageLimit shouldBe 100
                dto.usageCount shouldBe 25
                dto.createdAt.truncatedTo(ChronoUnit.MILLIS) shouldBe
                    entity.createdAt!!.truncatedTo(ChronoUnit.MILLIS)
              }
            }
          }
        },
    )
