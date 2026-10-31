package com.example.samplecoupon.infrastructure

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration {

  @Bean
  fun openApi(): OpenAPI =
      OpenAPI()
          .info(
              Info()
                  .title("Sample Coupon API")
                  .description(
                      """
                      Sandbox service for managing and redeeming coupons.

                      - **Coupons** - create coupons with a unique code, usage limit and the
                        country they can be redeemed in, and browse them page by page.
                      - **Coupon Redemptions** - redeem a coupon for a user. The client's country
                        is resolved from its IP address and must match the coupon's country. Each
                        user can redeem a coupon once, and only until its usage limit is reached.

                      Errors are returned as `application/problem+json` (RFC 9457).
                      """
                          .trimIndent(),
                  )
                  .version(javaClass.`package`.implementationVersion ?: "0.0.0-SNAPSHOT"),
          )

  @Bean
  fun openApiPathSorter(): OpenApiCustomizer = { api ->
    api.paths = api.paths.smartSort()
    api.tags = api.tags.sortedBy { it.name }
  }
}

private fun Paths.smartSort(): Paths = orEmpty().toSortedMap().let { Paths().withPathItems(it) }

private fun Paths.withPathItems(map: Map<String, PathItem>): Paths = apply {
  map.forEach { (string, item) -> addPathItem(string, item) }
}
