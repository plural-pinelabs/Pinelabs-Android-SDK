package com.plural_pinelabs.expresscheckoutsdk.common

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLPeerUnverifiedException
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkFailureMapperTest {
    @Test
    fun `classifies TLS pin failures`() {
        val result = mapNetworkFailure(SSLPeerUnverifiedException("pin mismatch"))

        assertEquals(ErrorCode.TLS_FAILURE.code, result.errorCode)
        assertEquals("TLS_FAILURE", result.errorMessage)
    }

    @Test
    fun `classifies timeout failures through a cause chain`() {
        val result = mapNetworkFailure(IOException("wrapped", SocketTimeoutException()))

        assertEquals(ErrorCode.NETWORK_TIMEOUT.code, result.errorCode)
        assertEquals("NETWORK_TIMEOUT", result.errorMessage)
    }

    @Test
    fun `classifies DNS failures`() {
        val result = mapNetworkFailure(UnknownHostException("api.example.invalid"))

        assertEquals(ErrorCode.DNS_FAILURE.code, result.errorCode)
        assertEquals("DNS_FAILURE", result.errorMessage)
    }

    @Test
    fun `does not expose exception details for generic network failures`() {
        val result = mapNetworkFailure(IOException("sensitive internal detail"))

        assertEquals(ErrorCode.NETWORK_FAILURE.code, result.errorCode)
        assertEquals("A network error occurred. Please try again.", result.errorDescription)
    }
}
