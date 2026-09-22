package com.example.samplecoupon.infra.geolocation

import com.example.samplecoupon.beforeEachGiven
import com.example.samplecoupon.common.CommonException
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withGivens
import io.kotest.matchers.shouldBe
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class GeoLocationRestClientSpec :
    BehaviorSpec({
      val wireMock = WireMockServer(wireMockConfig().dynamicPort())

      beforeSpec { wireMock.start() }
      afterSpec { wireMock.stop() }
      beforeEachGiven { wireMock.resetAll() }

      fun stubLocation(body: String) {
        wireMock.stubFor(
            get(urlPathEqualTo("/location/1.2.3.4"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(body)
                )
        )
      }

      fun createClient(): GeoLocationRestClient {
        val properties = GeoLocationProperties(endpoint = "${wireMock.baseUrl()}/location/{ipAddr}")
        return GeoLocationRestClient(RestClient.builder().build(), properties)
      }

      Given("a geolocation service responding with a countryCode") {
        stubLocation("""{"countryCode":"PL"}""")

        When("finding location") {
          val result = createClient().findLocation("1.2.3.4")

          Then("it returns GeoLocation with ipAddr and countryCode from response") {
            result.ipAddr shouldBe "1.2.3.4"
            result.countryCode shouldBe "PL"
          }
        }
      }

      withGivens(
          nameFn = { (description, _) -> "a geolocation service responding with $description" },
          "a null body" to "null",
          "no countryCode" to """{"city":"Warsaw"}""",
          "a non-string countryCode" to """{"countryCode":123}""",
      ) { (_, body) ->
        stubLocation(body)

        When("finding location") {
          Then("it throws FailedToLocalizeClient") {
            shouldThrow<CommonException.FailedToLocalizeClient> {
              createClient().findLocation("1.2.3.4")
            }
          }
        }
      }
    })
