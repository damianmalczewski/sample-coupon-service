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
    @field:CreatedDate
    @field:Column(name = "coupon_redemption_created_at", nullable = false) //
    var createdAt: Instant? = null,
)
