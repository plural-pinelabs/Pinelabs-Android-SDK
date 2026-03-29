package com.plural_pinelabs.expresscheckoutsdk.common

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class NullableIntAdapter : JsonDeserializer<Int?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Int? {
        if (json == null || json.isJsonNull) {
            return null
        }

        if (json.isJsonPrimitive) {
            val primitive = json.asJsonPrimitive
            return when {
                primitive.isNumber -> primitive.asInt
                primitive.isString -> primitive.asString.trim().toIntOrNull()
                else -> null
            }
        }

        if (json.isJsonObject) {
            val candidateKeys = listOf("amount", "value", "balance", "brandWalletBalance")
            candidateKeys.forEach { key ->
                val value = json.asJsonObject.get(key) ?: return@forEach
                if (!value.isJsonPrimitive) return@forEach
                val primitive = value.asJsonPrimitive
                return when {
                    primitive.isNumber -> primitive.asInt
                    primitive.isString -> primitive.asString.trim().toIntOrNull()
                    else -> null
                }
            }
        }

        return null
    }
}