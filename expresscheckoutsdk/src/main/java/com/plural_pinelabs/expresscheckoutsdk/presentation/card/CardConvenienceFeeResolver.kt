package com.plural_pinelabs.expresscheckoutsdk.presentation.card

import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CREDIT_DEBIT_ID
import com.plural_pinelabs.expresscheckoutsdk.data.model.ConvenienceFeesInfo

internal const val DEBIT_CARD_CONVENIENCE_FEE_PAYMENT_MODE = "DEBIT_CARD"

/**
 * Mirrors web checkout's card convenience-fee selection:
 * debit cards use DEBIT_CARD, other cards use CREDIT_DEBIT, and an exact
 * network match is preferred over DEFAULT.
 */
internal fun resolveCardConvenienceFee(
    convenienceFees: List<ConvenienceFeesInfo>?,
    cardNetwork: String?,
    cardType: String?,
): ConvenienceFeesInfo? {
    val paymentMode = if (cardType.equals("DEBIT", ignoreCase = true)) {
        DEBIT_CARD_CONVENIENCE_FEE_PAYMENT_MODE
    } else {
        CREDIT_DEBIT_ID
    }

    val paymentModeFees = convenienceFees.orEmpty().filter { fee ->
        fee.paymentModeType.equals(paymentMode, ignoreCase = true)
    }

    return paymentModeFees.firstOrNull { fee ->
        fee.networkType.equals(cardNetwork, ignoreCase = true)
    } ?: paymentModeFees.firstOrNull { fee ->
        fee.networkType.equals("DEFAULT", ignoreCase = true)
    } ?: paymentModeFees.firstOrNull()
}

internal fun resolveCardPaymentAmount(
    baseAmount: Int,
    selectedConvenienceFee: ConvenienceFeesInfo?,
    isPayByPoints: Boolean,
    redeemableAmount: Int,
): Int {
    val amountWithConvenienceFee =
        selectedConvenienceFee?.paymentAmount?.amount ?: baseAmount

    return if (isPayByPoints) {
        amountWithConvenienceFee - redeemableAmount
    } else {
        amountWithConvenienceFee
    }
}

/**
 * Saved-card data exposes the network but not the card type. This mirrors web checkout's
 * saved-card fallback, which uses the CREDIT_DEBIT fee bucket and CREDIT metadata when its
 * card-type lookup is unavailable.
 */
internal fun resolveSavedCardConvenienceFee(
    convenienceFees: List<ConvenienceFeesInfo>?,
    cardNetwork: String?,
): ConvenienceFeesInfo? {
    val cardFees = convenienceFees.orEmpty().filter { fee ->
        fee.paymentModeType.equals(CREDIT_DEBIT_ID, ignoreCase = true)
    }

    return cardFees.firstOrNull { fee ->
        fee.networkType.equals(cardNetwork, ignoreCase = true)
    } ?: cardFees.firstOrNull { fee ->
        fee.networkType.equals("DEFAULT", ignoreCase = true)
    } ?: cardFees.firstOrNull()
}
