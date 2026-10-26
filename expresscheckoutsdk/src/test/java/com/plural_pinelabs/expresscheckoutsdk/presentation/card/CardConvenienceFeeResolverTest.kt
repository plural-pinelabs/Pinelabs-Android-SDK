package com.plural_pinelabs.expresscheckoutsdk.presentation.card

import com.plural_pinelabs.expresscheckoutsdk.data.model.Amount
import com.plural_pinelabs.expresscheckoutsdk.data.model.ConvenienceFeesInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CardConvenienceFeeResolverTest {
    @Test
    fun `visa debit card selects debit card convenience fee`() {
        val creditFee = fee(paymentMode = "CREDIT_DEBIT", network = "VISA", finalAmount = 5065)
        val debitFee = fee(paymentMode = "DEBIT_CARD", network = "VISA", finalAmount = 5059)

        val selected = resolveCardConvenienceFee(
            convenienceFees = listOf(creditFee, debitFee),
            cardNetwork = "visa",
            cardType = "debit",
        )

        assertEquals(debitFee, selected)
    }

    @Test
    fun `credit card continues to use credit debit convenience fee`() {
        val creditFee = fee(paymentMode = "CREDIT_DEBIT", network = "VISA", finalAmount = 5065)
        val debitFee = fee(paymentMode = "DEBIT_CARD", network = "VISA", finalAmount = 5059)

        val selected = resolveCardConvenienceFee(
            convenienceFees = listOf(debitFee, creditFee),
            cardNetwork = "VISA",
            cardType = "CREDIT",
        )

        assertEquals(creditFee, selected)
    }

    @Test
    fun `default network fee is used when exact card network is unavailable`() {
        val defaultFee = fee(paymentMode = "DEBIT_CARD", network = "DEFAULT", finalAmount = 5059)

        val selected = resolveCardConvenienceFee(
            convenienceFees = listOf(defaultFee),
            cardNetwork = "VISA",
            cardType = "DEBIT",
        )

        assertEquals(defaultFee, selected)
    }

    @Test
    fun `unrelated payment mode is not selected for card`() {
        val upiFee = fee(paymentMode = "UPI", network = "DEFAULT", finalAmount = 5059)

        val selected = resolveCardConvenienceFee(
            convenienceFees = listOf(upiFee),
            cardNetwork = "VISA",
            cardType = "DEBIT",
        )

        assertNull(selected)
    }

    @Test
    fun `process payment amount includes selected convenience fee`() {
        val debitFee = fee(paymentMode = "DEBIT_CARD", network = "VISA", finalAmount = 5059)

        val paymentAmount = resolveCardPaymentAmount(
            baseAmount = 5000,
            selectedConvenienceFee = debitFee,
            isPayByPoints = false,
            redeemableAmount = 0,
        )

        assertEquals(5059, paymentAmount)
    }

    @Test
    fun `process payment amount remains base amount without a fee`() {
        val paymentAmount = resolveCardPaymentAmount(
            baseAmount = 5000,
            selectedConvenienceFee = null,
            isPayByPoints = false,
            redeemableAmount = 0,
        )

        assertEquals(5000, paymentAmount)
    }

    @Test
    fun `saved card selects matching credit debit network fee before default`() {
        val defaultFee = fee(paymentMode = "CREDIT_DEBIT", network = "DEFAULT", finalAmount = 5050)
        val visaFee = fee(paymentMode = "CREDIT_DEBIT", network = "VISA", finalAmount = 5059)

        val selected = resolveSavedCardConvenienceFee(
            convenienceFees = listOf(defaultFee, visaFee),
            cardNetwork = "VISA",
        )

        assertEquals(visaFee, selected)
    }

    private fun fee(
        paymentMode: String,
        network: String,
        finalAmount: Int,
    ): ConvenienceFeesInfo {
        return ConvenienceFeesInfo(
            paymentAmount = amount(finalAmount),
            convenienceFeesAmount = amount(50),
            convenienceFeesGSTAmount = amount(9),
            convenienceFeesAdditionalAmount = amount(0),
            convenienceFeesApplicableFeeAmount = amount(59),
            originalTxnAmount = amount(5000),
            paymentModeType = paymentMode,
            networkType = network,
            cardType = if (paymentMode == "DEBIT_CARD") "DEBIT" else "CREDIT",
        )
    }

    private fun amount(value: Int): Amount {
        return Amount(currency = "INR", value = value, amount = value)
    }
}
