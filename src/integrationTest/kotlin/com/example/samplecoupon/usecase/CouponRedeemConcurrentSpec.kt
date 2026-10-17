package com.example.samplecoupon.usecase

import com.example.samplecoupon.ApplicationTest
import com.example.samplecoupon.beforeEachGiven
import com.example.samplecoupon.domain.Coupon
import com.example.samplecoupon.domain.CouponException
import com.example.samplecoupon.domain.GeoLocation
import com.example.samplecoupon.infra.repository.CouponJpaRepository
import com.example.samplecoupon.infra.repository.CouponRedemptionJpaRepository
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * Concurrency tests for coupon redemption. Not `@Transactional` on purpose - every redemption runs
 * in its own committed transaction, so concurrent callers really contend for the same coupon row.
 */
@ApplicationTest
class CouponRedeemConcurrentSpec(
    useCase: CouponRedeemUseCase,
    couponRepository: CouponJpaRepository,
    couponRedemptionRepository: CouponRedemptionJpaRepository,
    transactions: PlatformTransactionManager,
    jdbc: JdbcOperations,
) :
    BehaviorSpec(
        {
          val plGeoLocation = GeoLocation(ipAddr = "127.0.0.1", countryCode = "PL")
          val transactionTemplate = TransactionTemplate(transactions)

          val executor = Executors.newCachedThreadPool()
          afterSpec { executor.shutdownNow() }

          fun redeem(code: String, userId: UUID = UUID.randomUUID()) =
              useCase.redeemCoupon(
                  RedeemCouponCommand(code = code, userId = userId, geoLocation = plGeoLocation),
              )

          fun usageCountOf(code: String) = couponRepository.fetchByCode(code).usageCount

          /** Number of backends in current database blocked on a row/tuple lock. */
          fun lockWaitersCount(): Int =
              jdbc.queryForObject(
                  """
                  SELECT count(*) FROM pg_stat_activity
                  WHERE datname = current_database() AND wait_event_type = 'Lock'
                  """,
                  Int::class.java,
              )!!

          /**
           * Opens transaction that reserves single redemption of [code] and holds row lock until
           * returned [CompletableFuture] is completed with `true` (commit) or `false` (rollback).
           */
          fun holdRedemptionLock(code: String): Pair<CompletableFuture<Boolean>, Future<*>> {
            val decision = CompletableFuture<Boolean>()
            val lockAcquired = CountDownLatch(1)
            val holder = executor.submit {
              transactionTemplate.executeWithoutResult { status ->
                couponRepository.lockRedemption(code)
                lockAcquired.countDown()
                if (!decision.get(30, TimeUnit.SECONDS)) {
                  status.setRollbackOnly()
                }
              }
            }
            lockAcquired.await(10, TimeUnit.SECONDS) shouldBe true
            return decision to holder
          }

          beforeEachGiven {
            couponRedemptionRepository.deleteAll()
            couponRepository.deleteAll()

            couponRepository.save(Coupon(code = "LAST", usageLimit = 1, countryCode = "PL"))
          }

          Given("a coupon with single usage left, reserved by an open transaction") {
            When("another user redeems it and reserving transaction commits") {
              val (decision, holder) = holdRedemptionLock("LAST")
              val contender =
                  CompletableFuture.supplyAsync({ runCatching { redeem("LAST") } }, executor)

              Then("contender blocks on row lock instead of reading stale usage count") {
                eventually(5.seconds) { lockWaitersCount() shouldBe 1 }
                contender.isDone shouldBe false
              }

              Then("contender fails with CouponUsageLimitReached after commit") {
                decision.complete(true)
                holder.get(10, TimeUnit.SECONDS)
                contender
                    .get(10, TimeUnit.SECONDS)
                    .exceptionOrNull()
                    .shouldBeInstanceOf<CouponException.CouponUsageLimitReached>()
                usageCountOf("LAST") shouldBe 1
              }
            }
          }

          Given("a coupon with single usage left, reserved by a transaction that rolls back") {
            When("another user redeems it meanwhile") {
              val (decision, holder) = holdRedemptionLock("LAST")
              val contender =
                  CompletableFuture.supplyAsync({ runCatching { redeem("LAST") } }, executor)

              Then("contender takes the released usage") {
                eventually(5.seconds) { lockWaitersCount() shouldBe 1 }
                decision.complete(false)
                holder.get(10, TimeUnit.SECONDS)
                contender.get(10, TimeUnit.SECONDS).isSuccess shouldBe true
                usageCountOf("LAST") shouldBe 1
                couponRedemptionRepository.count() shouldBe 1
              }
            }
          }
        },
    )
