package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import com.plural_pinelabs.expresscheckoutsdk.common.Constants.AUTHORIZED_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_ATTEMPTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_CANCELLED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_CREATED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_INITIATED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_PENDING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_STATUS
import java.util.Locale

internal enum class UpiInquiryOutcome {
    KEEP_POLLING,
    SUCCESS,
    ATTEMPT_FAILED,
    FAILURE,
}

internal data class UpiInquiryResolution(
    val outcome: UpiInquiryOutcome,
    val status: String?,
    val source: UpiStatusSource,
)

internal enum class UpiStatusSource {
    NONE,
    ORDER,
    PAYMENT,
}

/**
 * Resolves an inquiry without allowing another order or payment attempt to end this UPI flow.
 *
 * Inquiry is requested with the active payment id, so the top-level status describes that attempt.
 * Terminal order states still win because they describe the final state of the complete order.
 * An ATTEMPTED order is not terminal: it may have retries even when the current payment failed.
 */
internal fun resolveUpiInquiryStatus(
    expectedOrderId: String?,
    expectedPaymentId: String?,
    responseOrderId: String?,
    responsePaymentId: String?,
    orderStatus: String?,
    paymentStatus: String?,
): UpiInquiryResolution {
    if (!expectedOrderId.isNullOrBlank() &&
        !expectedOrderId.equals(responseOrderId, ignoreCase = true)
    ) {
        return UpiInquiryResolution(
            UpiInquiryOutcome.KEEP_POLLING,
            null,
            UpiStatusSource.NONE,
        )
    }

    if (!expectedPaymentId.isNullOrBlank() &&
        !responsePaymentId.isNullOrBlank() &&
        !expectedPaymentId.equals(responsePaymentId, ignoreCase = true)
    ) {
        return UpiInquiryResolution(
            UpiInquiryOutcome.KEEP_POLLING,
            null,
            UpiStatusSource.NONE,
        )
    }

    val normalizedOrderStatus = orderStatus.normalizedStatus()
    when (normalizedOrderStatus) {
        PROCESSED_STATUS, AUTHORIZED_STATUS -> return UpiInquiryResolution(
            UpiInquiryOutcome.SUCCESS,
            normalizedOrderStatus,
            UpiStatusSource.ORDER,
        )

        PROCESSED_FAILED, PROCESSED_CANCELLED -> return UpiInquiryResolution(
            UpiInquiryOutcome.FAILURE,
            normalizedOrderStatus,
            UpiStatusSource.ORDER,
        )
    }

    val normalizedPaymentStatus = paymentStatus.normalizedStatus()
    return UpiInquiryResolution(
        outcome = normalizedPaymentStatus.toPaymentOutcome(),
        status = normalizedPaymentStatus,
        source = if (normalizedPaymentStatus == null) {
            UpiStatusSource.NONE
        } else {
            UpiStatusSource.PAYMENT
        },
    )
}

private fun String?.normalizedStatus(): String? = this
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.uppercase(Locale.ROOT)

private fun String?.toPaymentOutcome(): UpiInquiryOutcome = when (this) {
    PROCESSED_STATUS, AUTHORIZED_STATUS -> UpiInquiryOutcome.SUCCESS
    PROCESSED_FAILED, PROCESSED_CANCELLED, PROCESSED_ATTEMPTED ->
        UpiInquiryOutcome.ATTEMPT_FAILED
    PROCESSED_PENDING, PROCESSED_CREATED, PROCESSED_INITIATED, null ->
        UpiInquiryOutcome.KEEP_POLLING
    else -> UpiInquiryOutcome.KEEP_POLLING
}
