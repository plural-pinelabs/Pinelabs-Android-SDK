package com.plural_pinelabs.expresscheckoutsdk.common

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentModeDataType
import com.plural_pinelabs.expresscheckoutsdk.data.model.Wallet
import java.lang.reflect.Type

class PaymentModeDeserialiser : JsonDeserializer<Any> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Any {
        return when {

            json.isJsonObject -> {
                val data = context?.deserialize<PaymentModeData>(
                    json,
                    object : TypeToken<PaymentModeData>() {}.type
                )
                    ?: throw JsonParseException("Context is null")
                PaymentModeDataType.Data(data)
            }

            json.isJsonArray -> {
                val wallets = context?.deserialize<List<Wallet>>(
                    json,
                    object : TypeToken<List<Wallet>>() {}.type
                )
                    ?: throw JsonParseException("Context is null")
                PaymentModeDataType.Wallets(wallets)
            }

            else -> throw JsonParseException("Expected JSON object or array for paymentModeData")
        }
    }
}