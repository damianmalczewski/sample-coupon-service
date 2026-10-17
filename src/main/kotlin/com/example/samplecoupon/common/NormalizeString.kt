package com.example.samplecoupon.common

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import tools.jackson.core.JsonParser
import tools.jackson.databind.BeanProperty
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.deser.std.StdDeserializer

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

enum class NormalizeFormat {
  DEFAULT,
  LOWERCASE,
  UPPERCASE,
}

private class NormalizeStringDeserializer(
    private val trim: Boolean = false,
    private val normalizeFormat: NormalizeFormat = NormalizeFormat.DEFAULT,
) : StdDeserializer<String?>(String::class.java) {

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String? {
    return p.string?.let {
      var result = it
      if (trim) {
        result = result.trim()
      }

      result =
          when (normalizeFormat) {
            NormalizeFormat.DEFAULT -> result
            NormalizeFormat.LOWERCASE -> result.lowercase()
            NormalizeFormat.UPPERCASE -> result.uppercase()
          }

      result
    }
  }

  override fun createContextual(
      ctxt: DeserializationContext,
      property: BeanProperty,
  ): ValueDeserializer<*> {
    val normalize = property.getAnnotation<NormalizeString>()
    return NormalizeStringDeserializer(
        trim = normalize?.trim ?: false,
        normalizeFormat = normalize?.normalizeFormat ?: NormalizeFormat.DEFAULT,
    )
  }
}

private inline fun <reified T : Annotation> BeanProperty.getAnnotation(): T? =
    getAnnotation(T::class.java)
