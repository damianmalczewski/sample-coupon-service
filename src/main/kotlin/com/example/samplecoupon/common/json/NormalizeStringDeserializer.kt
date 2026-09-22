package com.example.samplecoupon.common.json

import tools.jackson.core.JsonParser
import tools.jackson.databind.BeanProperty
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.deser.std.StdDeserializer

class NormalizeStringDeserializer(
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
    val normalize = property.getAnnotation(NormalizeString::class.java)
    return NormalizeStringDeserializer(
        trim = normalize?.trim ?: false,
        normalizeFormat = normalize?.normalizeFormat ?: NormalizeFormat.DEFAULT,
    )
  }
}
