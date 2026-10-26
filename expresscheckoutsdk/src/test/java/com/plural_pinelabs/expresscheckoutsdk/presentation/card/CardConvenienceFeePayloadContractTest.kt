package com.plural_pinelabs.expresscheckoutsdk.presentation.card

import com.google.gson.Gson
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.Amount
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardMetaData
import com.plural_pinelabs.expresscheckoutsdk.data.model.ConvenienceFeesInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.MaximumFeeAmount
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class CardConvenienceFeePayloadContractTest {
    @Test
    fun `visa debit process payment payload matches web checkout CCF amounts`() {
        val selectedFee = ConvenienceFeesInfo(
            paymentAmount = amount(5059),
            convenienceFeesAmount = amount(50),
            convenienceFeesGSTAmount = amount(9),
            convenienceFeesAdditionalAmount = amount(0),
            convenienceFeesMaximumFeeAmount = MaximumFeeAmount(
                currency = "INR",
                value = 9_999_999_900L,
                amount = 9_999_999_900L,
            ),
            convenienceFeesApplicableFeeAmount = amount(59),
            originalTxnAmount = amount(5000),
            paymentModeType = "DEBIT_CARD",
            networkType = "VISA",
            cardType = "DEBIT",
        )
        val request = ProcessPaymentRequest(
            extras = Extra(
                payment_mode = listOf("CREDIT_DEBIT"),
                payment_amount = resolveCardPaymentAmount(
                    baseAmount = 5000,
                    selectedConvenienceFee = selectedFee,
                    isPayByPoints = false,
                    redeemableAmount = 0,
                ),
                payment_currency = "INR",
                card_last4 = "2131",
                redeemable_amount = null,
                registered_mobile_number = null,
                txn_mode = null,
                device_info = null,
                risk_validation_details = null,
            ),
            convenience_fee_data = Utils.getConvenienceFeesRequest(selectedFee),
            card_meta_data = CardMetaData(
                scheme_name = selectedFee.networkType,
                card_type = selectedFee.cardType,
            ),
        )

        val json = Gson().toJsonTree(request).asJsonObject
        val extras = json.getAsJsonObject("extras")
        val fee = json.getAsJsonObject("convenience_fee_data")
        val cardMeta = json.getAsJsonObject("card_meta_data")

        assertEquals(5059, extras.get("payment_amount").asInt)
        assertEquals(50, fee.get("convenience_fees_amt_in_paise").asInt)
        assertEquals(9, fee.get("convenience_fees_fees_gst_amt_in_paise").asInt)
        assertEquals(0, fee.get("convenience_fees_fees_addition_amt_in_paise").asInt)
        assertEquals(5059, fee.get("final_amt_in_paise").asInt)
        assertEquals(5000, fee.get("transaction_amount").asInt)
        assertEquals(9_999_999_900L, fee.get("convenience_fees_maximum_fee_amount").asLong)
        assertEquals(59, fee.get("convenience_fees_applicable_fee_amount").asInt)
        assertEquals("INR", fee.get("currency").asString)
        assertEquals("VISA", cardMeta.get("scheme_name").asString)
        assertEquals("DEBIT", cardMeta.get("card_type").asString)
    }

    private fun amount(value: Int): Amount {
        return Amount(currency = "INR", value = value, amount = value)
    }
}
