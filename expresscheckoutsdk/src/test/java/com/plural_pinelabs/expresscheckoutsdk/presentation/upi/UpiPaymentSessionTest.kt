package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiPaymentSessionTest {
    @Test
    fun `terminal result is handled once for an active payment`() {
        val session = UpiPaymentSession()

        session.begin("order-1", "payment-1")

        assertEquals("payment-1", session.inquiryPaymentId)
        assertEquals("payment-1", session.currentAttempt()?.paymentId)
        assertTrue(session.tryHandleTerminalResult())
        assertFalse(session.tryHandleTerminalResult())
    }

    @Test
    fun `new payment can handle a terminal result after previous payment finishes`() {
        val session = UpiPaymentSession()
        session.begin("order-1")
        assertTrue(session.tryHandleTerminalResult())
        session.finish()

        assertEquals(null, session.inquiryPaymentId)

        session.begin("order-2")

        assertTrue(session.isActive)
        assertEquals("order-2", session.inquiryOrderId)
        assertTrue(session.shouldShowPaymentTimer)
        assertTrue(session.tryHandleTerminalResult())
    }

    @Test
    fun `inactive payment ignores terminal results`() {
        val session = UpiPaymentSession()

        assertFalse(session.tryHandleTerminalResult())
    }

    @Test
    fun `QR monitoring does not request the external app return timer`() {
        val session = UpiPaymentSession()

        session.begin(showPaymentTimer = false)

        assertFalse(session.shouldShowPaymentTimer)
    }

    @Test
    fun `new attempt invalidates an older generation`() {
        val session = UpiPaymentSession()
        session.begin("order-1", "payment-1")
        val oldAttempt = session.currentAttempt()!!

        session.begin("order-1", "payment-2")

        assertFalse(session.isCurrent(oldAttempt))
        assertTrue(session.isCurrent(session.currentAttempt()!!))
    }

    @Test
    fun `active attempt retains its original deadline`() {
        val session = UpiPaymentSession()

        session.begin(
            orderId = "order-1",
            paymentId = "payment-1",
            deadlineElapsedRealtimeMs = 123_456L,
        )

        assertEquals(123_456L, session.currentAttempt()?.deadlineElapsedRealtimeMs)
    }
}
