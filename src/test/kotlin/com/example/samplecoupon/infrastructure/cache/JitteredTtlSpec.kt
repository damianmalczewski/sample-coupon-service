package com.example.samplecoupon.infrastructure.cache

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.time.Duration

class JitteredTtlSpec :
    BehaviorSpec(
        {
          val ttl = Duration.ofHours(8)

          Given("a non-zero jitter") {
            val jitteredTtl = JitteredTtl(ttl, Duration.ofHours(1))

            When("computing TTL for many entries") {
              val ttls = (1..1000).map { jitteredTtl.getTimeToLive("key-$it", null) }

              Then("each TTL falls within [ttl, ttl + jitter)") {
                ttls.forEach {
                  it shouldBeGreaterThanOrEqualTo ttl
                  it shouldBeLessThan ttl.plusHours(1)
                }
              }

              Then("TTLs are spread out") { ttls.toSet() shouldHaveAtLeastSize 900 }
            }
          }

          Given("a zero jitter") {
            val jitteredTtl = JitteredTtl(ttl, Duration.ZERO)

            When("computing TTL") {
              val result = jitteredTtl.getTimeToLive("key", null)

              Then("it is exactly base TTL") { result shouldBe ttl }
            }
          }
        },
    )
