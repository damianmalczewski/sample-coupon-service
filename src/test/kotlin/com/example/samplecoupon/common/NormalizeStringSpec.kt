package com.example.samplecoupon.common

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withGivens
import io.kotest.matchers.shouldBe
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

class NormalizeStringSpec :
    BehaviorSpec(
        {
          val mapper = JsonMapper.builder().build()

          fun read(json: String): Fixture = mapper.readValue<Fixture>(json)

          Given("a field with default normalization") {
            When("deserializing a padded mixed-case value") {
              val result = read("""{"plain":"  AbC  "}""")

              Then("it keeps the original value") { result.plain shouldBe "  AbC  " }
            }
          }

          Given("a field with trim enabled") {
            When("deserializing a padded value") {
              val result = read("""{"trimmed":"  AbC  "}""")

              Then("it trims the value") { result.trimmed shouldBe "AbC" }
            }
          }

          Given("a field with lowercase format") {
            When("deserializing a mixed-case value") {
              val result = read("""{"lower":"AbC123"}""")

              Then("it converts the value to lowercase") { result.lower shouldBe "abc123" }
            }
          }

          Given("a field with uppercase format") {
            When("deserializing a mixed-case value") {
              val result = read("""{"upper":"AbC123"}""")

              Then("it converts the value to uppercase") { result.upper shouldBe "ABC123" }
            }
          }

          Given("a field with both trim and uppercase format") {
            When("deserializing a padded mixed-case value") {
              val result = read("""{"trimUpper":"  AbC  "}""")

              Then("it trims before applying the format") { result.trimUpper shouldBe "ABC" }
            }
          }

          withGivens(
              nameFn = { "json without values: $it" },
              """{"plain":null,"trimmed":null,"lower":null,"upper":null,"trimUpper":null}""",
              "{}",
          ) { json ->
            When("deserializing it") {
              val result = read(json)

              Then("all values stay null") { result shouldBe Fixture() }
            }
          }
        },
    )

private data class Fixture(
    @field:NormalizeString val plain: String? = null,
    @field:NormalizeString(trim = true) val trimmed: String? = null,
    @field:NormalizeString(normalizeFormat = NormalizeFormat.LOWERCASE) val lower: String? = null,
    @field:NormalizeString(normalizeFormat = NormalizeFormat.UPPERCASE) val upper: String? = null,
    @field:NormalizeString(trim = true, normalizeFormat = NormalizeFormat.UPPERCASE)
    val trimUpper: String? = null,
)
