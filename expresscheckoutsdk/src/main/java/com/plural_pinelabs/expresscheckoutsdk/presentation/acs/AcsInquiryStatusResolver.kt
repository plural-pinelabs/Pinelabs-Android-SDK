package com.plural_pinelabs.expresscheckoutsdk.presentation.acs

import com.plural_pinelabs.expresscheckoutsdk.common.Constants.AUTHORIZED_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_ATTEMPTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_CANCELLED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_CREATED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_INITIATED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_PENDING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_STATUS
import java.util.Locale

/** Matches web checkout's ACS inquiry terminal-state handling. */
internal enum class AcsInquiryOutcome {
    KEEP_POLLING,
    SUCCESS,
    FAILURE,
}

internal fun resolveAcsInquiryStatus(
    orderStatus: String?,
    paymentStatus: String?,
): AcsInquiryOutcome {
    val status = (orderStatus ?: paymentStatus)
        ?.trim()
        ?.uppercase(Locale.ROOT)

    return when (status) {
        PROCESSED_STATUS, AUTHORIZED_STATUS -> AcsInquiryOutcome.SUCCESS
        PROCESSED_FAILED, PROCESSED_ATTEMPTED, PROCESSED_CANCELLED -> AcsInquiryOutcome.FAILURE
        PROCESSED_PENDING, PROCESSED_CREATED, PROCESSED_INITIATED, null -> AcsInquiryOutcome.KEEP_POLLING
        else -> AcsInquiryOutcome.KEEP_POLLING
    }
}
