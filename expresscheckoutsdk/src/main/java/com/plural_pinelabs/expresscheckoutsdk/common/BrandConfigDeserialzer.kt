package com.plural_pinelabs.expresscheckoutsdk.common

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.plural_pinelabs.expresscheckoutsdk.data.model.BrandConfig
import java.lang.reflect.Type

class BrandConfigDeserialzer : JsonDeserializer<BrandConfig?> {


    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): BrandConfig? {
        if (json != null) {
            return when {
                json.isJsonNull -> null
                json.isJsonObject -> context?.deserialize(json, BrandConfig::class.java)
                json.isJsonPrimitive && json.asJsonPrimitive.isString -> null // handles ""
                else -> null
            }
        } else return null
    }
}