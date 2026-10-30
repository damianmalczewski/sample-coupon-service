package com.example.samplecoupon.infra

import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.Resource
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver

/**
 * Serves the web UI built from `webapp/` into static resources. Replaces Spring Boot's default
 * static resource mapping, which is skipped once the catch-all pattern is registered - hence the
 * highest order.
 *
 * Resource handlers rank below controllers, actuator and Swagger UI, so only requests none of them
 * handle get here.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
class WebappConfiguration : WebMvcConfigurer {

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry
        .addResourceHandler("/**")
        .addResourceLocations("classpath:/static/")
        .resourceChain(true)
        .addResolver(WebappRouteResolver())
  }
}

/**
 * Resolves client-side routes of the web UI (e.g. `/redeem`) to `index.html`, so they survive a
 * page reload. Missing files (path with an extension) and unknown API paths stay 404.
 */
private class WebappRouteResolver : PathResourceResolver() {

  override fun getResource(resourcePath: String, location: Resource): Resource? {
    val resource = super.getResource(resourcePath, location)
    if (resource != null || !isClientRoute(resourcePath)) {
      return resource
    }
    return super.getResource(WEBAPP_INDEX, location)
  }

  private fun isClientRoute(resourcePath: String): Boolean =
      !resourcePath.startsWith("api/") && !resourcePath.substringAfterLast('/').contains('.')
}

private const val WEBAPP_INDEX = "index.html"
