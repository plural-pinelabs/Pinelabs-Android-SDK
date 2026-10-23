package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

/**
 * Tracks the part of a UPI payment that outlives the external UPI activity.
 *
 * The activity result returned by UPI apps is not a reliable payment result. The backend inquiry
 * remains the source of truth, and this state prevents repeated inquiry responses from causing
 * more than one terminal navigation.
 */
internal class UpiPaymentSession {
    data class Attempt(
        val orderId: String?,
        val paymentId: String?,
        val generation: Long,
        val deadlineElapsedRealtimeMs: Long?,
    )

    var isActive: Boolean = false
        private set

    var inquiryOrderId: String? = null
        private set

    var inquiryPaymentId: String? = null
        private set

    var shouldShowPaymentTimer: Boolean = false
        private set

    private var terminalResultHandled: Boolean = false
    private var generation: Long = 0
    private var deadlineElapsedRealtimeMs: Long? = null

    fun begin(
        orderId: String? = null,
        paymentId: String? = null,
        showPaymentTimer: Boolean = true,
        deadlineElapsedRealtimeMs: Long? = null,
    ) {
        generation += 1
        inquiryOrderId = orderId
        inquiryPaymentId = paymentId
        shouldShowPaymentTimer = showPaymentTimer
        this.deadlineElapsedRealtimeMs = deadlineElapsedRealtimeMs
        terminalResultHandled = false
        isActive = true
    }

    fun currentAttempt(): Attempt? {
        if (!isActive) return null
        return Attempt(inquiryOrderId, inquiryPaymentId, generation, deadlineElapsedRealtimeMs)
    }

    fun isCurrent(attempt: Attempt): Boolean {
        return isActive &&
            generation == attempt.generation &&
            inquiryOrderId == attempt.orderId &&
            inquiryPaymentId == attempt.paymentId &&
            deadlineElapsedRealtimeMs == attempt.deadlineElapsedRealtimeMs
    }

    fun tryHandleTerminalResult(): Boolean {
        if (!isActive || terminalResultHandled) return false
        terminalResultHandled = true
        return true
    }

    fun finish() {
        isActive = false
        inquiryOrderId = null
        inquiryPaymentId = null
        deadlineElapsedRealtimeMs = null
        shouldShowPaymentTimer = false
    }
}
