package com.example.samplecoupon.adapter.rest

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * Serves the web UI built from `webapp/` on the root path, or redirects to Swagger UI when the web
 * UI was not built. Other client-side routes are resolved in `WebappConfiguration`.
 */
@Controller
class IndexController(
    @param:Value("classpath:static/index.html") private val webappIndex: Resource,
) {

  @GetMapping("/")
  fun index(): String {
    return if (webappIndex.exists()) "forward:/index.html" else "redirect:${SWAGGER_UI_PATH}"
  }
}

private const val SWAGGER_UI_PATH = "/swagger-ui/index.html"
