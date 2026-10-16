package com.example.samplecoupon.infra.geolocation

import com.example.samplecoupon.common.CommonException
import com.example.samplecoupon.domain.GeoLocation
import com.example.samplecoupon.domain.GeoLocationClient
import com.example.samplecoupon.infra.GeoLocationProperties
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class GeoLocationRestClient(
    private val restClient: RestClient,
    private val properties: GeoLocationProperties,
) : GeoLocationClient {

  @Cacheable("ipAddrGeoLocation", key = "#ipAddr")
  override fun findLocation(ipAddr: String): GeoLocation {
    val responseBody =
        restClient.get().uri(properties.endpoint, ipAddr).retrieve().body<Map<String, *>>()
            ?: throw CommonException.FailedToLocalizeClient()

    val countryCode =
        responseBody["countryCode"] as? String ?: throw CommonException.FailedToLocalizeClient()

    return GeoLocation(ipAddr = ipAddr, countryCode = countryCode)
  }
}
