@file:Suppress("unused")

package com.example.samplecoupon.adapter

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

@Min(0, message = "must be greater than or equal to 0")
@Constraint(validatedBy = [])
@Target(FIELD, VALUE_PARAMETER, PROPERTY_GETTER, ANNOTATION_CLASS)
@Retention(RUNTIME)
annotation class ValidQueryPage(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

@Min(1, message = "must be greater than or equal to 1")
@Max(500, message = "must be less than or equal to 500")
@Constraint(validatedBy = [])
@Target(FIELD, VALUE_PARAMETER, PROPERTY_GETTER, ANNOTATION_CLASS)
@Retention(RUNTIME)
annotation class ValidQuerySize(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

@Size(min = 1, max = 255, message = "size must be between 1 and 255")
@Pattern(regexp = "^[a-zA-Z0-9]*$", message = "must be alphanumeric")
@Constraint(validatedBy = [])
@Target(FIELD, VALUE_PARAMETER, PROPERTY_GETTER, ANNOTATION_CLASS)
@Retention(RUNTIME)
annotation class ValidCouponCode(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

@Size(min = 1, max = 8, message = "size must be between 1 and 8")
@Pattern(regexp = "^[a-zA-Z]*$", message = "must be alphabetic")
@Constraint(validatedBy = [])
@Target(FIELD, VALUE_PARAMETER, PROPERTY_GETTER, ANNOTATION_CLASS)
@Retention(RUNTIME)
annotation class ValidCountryCode(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

@Pattern(
    regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
    message = "must be a UUID",
)
@Constraint(validatedBy = [])
@Target(FIELD, VALUE_PARAMETER, PROPERTY_GETTER, ANNOTATION_CLASS)
@Retention(RUNTIME)
annotation class ValidUserId(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

@Pattern(
    regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
    message = "must be a UUID",
)
@Constraint(validatedBy = [])
@Target(FIELD, VALUE_PARAMETER, PROPERTY_GETTER, ANNOTATION_CLASS)
@Retention(RUNTIME)
annotation class ValidCouponId(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

@Min(1, message = "must be greater than or equal to 1")
@Constraint(validatedBy = [])
@Target(FIELD, VALUE_PARAMETER, PROPERTY_GETTER, ANNOTATION_CLASS)
@Retention(RUNTIME)
annotation class ValidCouponUsageLimit(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
