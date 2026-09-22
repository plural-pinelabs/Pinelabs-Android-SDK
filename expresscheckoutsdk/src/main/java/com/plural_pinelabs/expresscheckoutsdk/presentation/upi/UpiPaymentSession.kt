package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

/**
 * Tracks the part of a UPI payment that outlives the external UPI activity.
 *
 * The activity result returned by UPI apps is not a reliable payment result. The backend inquiry
 * remains the source of truth, and this state prevents repeated inquiry responses from causing
 * more than one terminal navigation.
 */
internal class UpiPaymentSession {
    var isActive: Boolean = false
        private set

    var inquiryOrderId: String? = null
        private set

    var shouldShowPaymentTimer: Boolean = false
        private set

    private var terminalResultHandled: Boolean = false

    fun begin(orderId: String? = null, showPaymentTimer: Boolean = true) {
        inquiryOrderId = orderId
        shouldShowPaymentTimer = showPaymentTimer
        terminalResultHandled = false
        isActive = true
    }

    fun tryHandleTerminalResult(): Boolean {
        if (!isActive || terminalResultHandled) return false
        terminalResultHandled = true
        return true
    }

    fun finish() {
        isActive = false
        inquiryOrderId = null
        shouldShowPaymentTimer = false
    }
}
