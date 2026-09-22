package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiPaymentSessionTest {
    @Test
    fun `terminal result is handled once for an active payment`() {
        val session = UpiPaymentSession()

        session.begin("order-1")

        assertTrue(session.tryHandleTerminalResult())
        assertFalse(session.tryHandleTerminalResult())
    }

    @Test
    fun `new payment can handle a terminal result after previous payment finishes`() {
        val session = UpiPaymentSession()
        session.begin("order-1")
        assertTrue(session.tryHandleTerminalResult())
        session.finish()

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
}
