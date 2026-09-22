package com.plural_pinelabs.expresscheckoutsdk.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.StringReader

class FetchErrorParserTest {
    @Test
    fun parsesConcreteFetchErrorWithoutTypeToken() {
        val error = parseFetchError(
            StringReader(
                """{"error_code":"PAYMENT_FAILED","error_message":"Payment failed"}"""
            )
        )

        assertEquals("PAYMENT_FAILED", error?.error_code)
        assertEquals("Payment failed", error?.error_message)
    }

    @Test
    fun malformedBodyReturnsNull() {
        assertNull(parseFetchError(StringReader("not-json")))
    }
}
