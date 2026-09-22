package com.example.samplecoupon.common.json

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import tools.jackson.databind.annotation.JsonDeserialize

/**
 * Custom annotation to normalize string values during JSON deserialization. It provides options to
 * trim whitespace and to convert the string to either lowercase or uppercase.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = NormalizeStringDeserializer::class)
annotation class NormalizeString(
    val trim: Boolean = false,
    val normalizeFormat: NormalizeFormat = NormalizeFormat.DEFAULT,
)
