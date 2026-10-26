package com.plural_pinelabs.expresscheckoutsdk.presentation.acs

import org.junit.Assert.assertEquals
import org.junit.Test

class AcsInquiryStatusResolverTest {
    @Test
    fun `processed and authorized are successful`() {
        assertEquals(AcsInquiryOutcome.SUCCESS, resolveAcsInquiryStatus("PROCESSED", "PENDING"))
        assertEquals(AcsInquiryOutcome.SUCCESS, resolveAcsInquiryStatus("AUTHORIZED", "PENDING"))
    }

    @Test
    fun `attempted is failure and never success`() {
        assertEquals(AcsInquiryOutcome.FAILURE, resolveAcsInquiryStatus("ATTEMPTED", "ATTEMPTED"))
    }

    @Test
    fun `order status takes precedence over payment attempt status`() {
        assertEquals(AcsInquiryOutcome.FAILURE, resolveAcsInquiryStatus("FAILED", "PROCESSED"))
    }

    @Test
    fun `pending statuses keep polling`() {
        assertEquals(AcsInquiryOutcome.KEEP_POLLING, resolveAcsInquiryStatus("PENDING", "PENDING"))
        assertEquals(AcsInquiryOutcome.KEEP_POLLING, resolveAcsInquiryStatus(null, "CREATED"))
    }
}
