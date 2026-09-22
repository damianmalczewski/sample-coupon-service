package com.example.samplecoupon.domain

interface GeoLocationClient {

  fun findLocation(ipAddr: String): GeoLocation
}
