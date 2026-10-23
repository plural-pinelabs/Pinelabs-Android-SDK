package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import org.junit.Assert.assertEquals
import org.junit.Test

class UpiInquiryStatusResolverTest {
    @Test
    fun `processed order wins over a failed payment attempt`() {
        val result = resolve(orderStatus = "PROCESSED", paymentStatus = "FAILED")

        assertEquals(UpiInquiryOutcome.SUCCESS, result.outcome)
        assertEquals("PROCESSED", result.status)
        assertEquals(UpiStatusSource.ORDER, result.source)
    }

    @Test
    fun `authorized current payment is successful`() {
        val result = resolve(orderStatus = "PENDING", paymentStatus = "authorized")

        assertEquals(UpiInquiryOutcome.SUCCESS, result.outcome)
        assertEquals("AUTHORIZED", result.status)
        assertEquals(UpiStatusSource.PAYMENT, result.source)
    }

    @Test
    fun `failed current attempt is retryable rather than an order failure`() {
        val result = resolve(orderStatus = "ATTEMPTED", paymentStatus = "FAILED")

        assertEquals(UpiInquiryOutcome.ATTEMPT_FAILED, result.outcome)
        assertEquals(UpiStatusSource.PAYMENT, result.source)
    }

    @Test
    fun `attempted current payment ends only that attempt`() {
        val result = resolve(orderStatus = "ATTEMPTED", paymentStatus = "ATTEMPTED")

        assertEquals(UpiInquiryOutcome.ATTEMPT_FAILED, result.outcome)
    }

    @Test
    fun `attempted order with pending current payment keeps polling`() {
        val result = resolve(orderStatus = "ATTEMPTED", paymentStatus = "PENDING")

        assertEquals(UpiInquiryOutcome.KEEP_POLLING, result.outcome)
    }

    @Test
    fun `created initiated and unknown payment statuses remain unresolved`() {
        listOf("CREATED", "INITIATED", "A_NEW_STATUS", null).forEach { status ->
            assertEquals(
                UpiInquiryOutcome.KEEP_POLLING,
                resolve(orderStatus = "ATTEMPTED", paymentStatus = status).outcome,
            )
        }
    }

    @Test
    fun `failed order is terminal failure even if payment says processed`() {
        val result = resolve(orderStatus = "FAILED", paymentStatus = "PROCESSED")

        assertEquals(UpiInquiryOutcome.FAILURE, result.outcome)
        assertEquals(UpiStatusSource.ORDER, result.source)
    }

    @Test
    fun `another payment response cannot terminate active inquiry`() {
        val result = resolveUpiInquiryStatus(
            expectedOrderId = "order-1",
            expectedPaymentId = "payment-new",
            responseOrderId = "order-1",
            responsePaymentId = "payment-old",
            orderStatus = "PROCESSED",
            paymentStatus = "PROCESSED",
        )

        assertEquals(UpiInquiryOutcome.KEEP_POLLING, result.outcome)
        assertEquals(UpiStatusSource.NONE, result.source)
    }

    @Test
    fun `another order response is ignored`() {
        val result = resolveUpiInquiryStatus(
            expectedOrderId = "order-1",
            expectedPaymentId = "payment-1",
            responseOrderId = "order-2",
            responsePaymentId = "payment-1",
            orderStatus = "PROCESSED",
            paymentStatus = "PROCESSED",
        )

        assertEquals(UpiInquiryOutcome.KEEP_POLLING, result.outcome)
        assertEquals(UpiStatusSource.NONE, result.source)
    }

    @Test
    fun `scoped inquiry may omit response payment id`() {
        val result = resolveUpiInquiryStatus(
            expectedOrderId = "order-1",
            expectedPaymentId = "payment-1",
            responseOrderId = "order-1",
            responsePaymentId = null,
            orderStatus = "ATTEMPTED",
            paymentStatus = "FAILED",
        )

        assertEquals(UpiInquiryOutcome.ATTEMPT_FAILED, result.outcome)
    }

    private fun resolve(
        orderStatus: String?,
        paymentStatus: String?,
    ): UpiInquiryResolution {
        return resolveUpiInquiryStatus(
            expectedOrderId = "order-1",
            expectedPaymentId = "payment-1",
            responseOrderId = "order-1",
            responsePaymentId = "payment-1",
            orderStatus = orderStatus,
            paymentStatus = paymentStatus,
        )
    }
}
