package com.plural_pinelabs.expresscheckoutsdk.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvenienceFeesInfoParsingTest {
    @Test
    fun parsesMaximumFeeAmountLargerThanIntRange() {
        val response = Gson().fromJson(
            """
            {
              "convenienceFeesMaximumFeeAmount": {
                "currency": "INR",
                "value": 9999999900,
                "amount": 9999999900
              }
            }
            """.trimIndent(),
            ConvenienceFeesInfo::class.java
        )

        assertEquals(9_999_999_900L, response.convenienceFeesMaximumFeeAmount?.value)
        assertEquals(9_999_999_900L, response.convenienceFeesMaximumFeeAmount?.amount)
    }

    @Test
    fun preservesMaximumFeeAmountWhenSerializingPaymentRequest() {
        val request = ConvenienceFeesData(
            convenience_fees_amt_in_paise = 100,
            convenience_fees_fees_gst_amt_in_paise = 18,
            convenience_fees_fees_addition_amt_in_paise = 0,
            final_amt_in_paise = 10_118,
            transaction_amount = 10_000,
            convenience_fees_maximum_fee_amount = 9_999_999_900L,
            convenience_fees_applicable_fee_amount = 118,
            currency = "INR"
        )

        val json = Gson().toJson(request)

        org.junit.Assert.assertTrue(
            json.contains("\"convenience_fees_maximum_fee_amount\":9999999900")
        )
    }
}
