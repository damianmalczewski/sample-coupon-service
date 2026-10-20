package com.example.samplecoupon.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.UuidGenerator
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "coupons")
@EntityListeners(AuditingEntityListener::class)
class Coupon(
    //
    @field:Id
    @field:UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @field:Column(name = "coupon_id", nullable = false) //
    var id: UUID? = null,
    //
    @field:Column(name = "coupon_code", nullable = false, length = 255, unique = true) //
    var code: String,
    //
    @field:Column(name = "coupon_usage_limit", nullable = false) //
    var usageLimit: Int,
    //
    @field:Column(name = "coupon_usage_count", nullable = false) //
    var usageCount: Int = 0,
    //
    @field:Column(name = "coupon_country_code", nullable = false, length = 16) //
    var countryCode: String,
    //
    @field:CreatedDate
    @field:Column(name = "coupon_created_at", nullable = false) //
    var createdAt: Instant? = null,
) {
  fun validateGeoLocation(geoLocation: GeoLocation) {
    if (countryCode != geoLocation.countryCode.uppercase()) {
      throw CouponException.CouponUnavailableInCountry()
    }
  }

  fun redeem(userId: UUID, requestId: UUID): CouponRedemption =
      CouponRedemption(coupon = this, userId = userId, requestId = requestId)
}

@Entity
@Table(name = "coupon_redemptions")
@EntityListeners(AuditingEntityListener::class)
class CouponRedemption(
    //
    @field:Id
    @field:UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @field:Column(name = "coupon_redemption_id", nullable = false) //
    var id: UUID? = null,
    //
    @field:ManyToOne(fetch = FetchType.LAZY)
    @field:JoinColumn(name = "coupon_id", nullable = false) //
    var coupon: Coupon,
    //
    @field:Column(name = "user_id", nullable = false) //
    var userId: UUID,
    //
    @field:Column(name = "coupon_redemption_request_id", nullable = false, unique = true) //
    var requestId: UUID,
    //
    @field:CreatedDate
    @field:Column(name = "coupon_redemption_created_at", nullable = false) //
    var createdAt: Instant? = null,
) {
  /** Whether this redemption was made for given coupon and user, i.e. it answers same request. */
  fun isFor(coupon: Coupon, userId: UUID): Boolean =
      this.coupon.id == coupon.id && this.userId == userId
}
