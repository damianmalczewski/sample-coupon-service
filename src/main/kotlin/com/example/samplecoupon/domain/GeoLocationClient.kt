package com.example.samplecoupon.domain

interface GeoLocationClient {

  fun findLocation(ipAddr: String): GeoLocation
}

data class GeoLocation(val ipAddr: String, val countryCode: String)
