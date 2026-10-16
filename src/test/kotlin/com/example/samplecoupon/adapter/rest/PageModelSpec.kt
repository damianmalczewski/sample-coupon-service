package com.example.samplecoupon.adapter.rest

import com.example.samplecoupon.common.PageResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class PageModelSpec :
    BehaviorSpec(
        {
          Given("a page result") {
            val source =
                PageResult(content = listOf(1, 2, 3), page = 4, size = 3, totalElements = 15L)

            When("mapping it to a page model") {
              val result = source.toPageModel { "item-$it" }

              Then("it maps content and keeps paging metadata") {
                result shouldBe PageModel(listOf("item-1", "item-2", "item-3"), 4, 3, 15L)
              }
            }
          }

          Given("an empty page result") {
            val source =
                PageResult(
                    content = emptyList<Int>(),
                    page = 7,
                    size = 20,
                    totalElements = 140L,
                )

            When("mapping it to a page model") {
              val result = source.toPageModel { "item-$it" }

              Then("it keeps paging metadata with empty content") {
                result shouldBe PageModel(emptyList(), 7, 20, 140L)
              }
            }
          }

          Given("a collection") {
            val source = setOf(1, 2, 3)

            When("mapping it to a page model") {
              val result = source.toPageModel { it * 10 }

              Then("it builds a single first page spanning the whole collection") {
                result shouldBe PageModel(listOf(10, 20, 30), 0, 3, 3L)
              }
            }
          }

          Given("an empty collection") {
            val source = emptyList<Int>()

            When("mapping it to a page model") {
              val result = source.toPageModel { it * 10 }

              Then("it builds an empty first page") {
                result shouldBe PageModel(emptyList(), 0, 0, 0L)
              }
            }
          }
        },
    )
