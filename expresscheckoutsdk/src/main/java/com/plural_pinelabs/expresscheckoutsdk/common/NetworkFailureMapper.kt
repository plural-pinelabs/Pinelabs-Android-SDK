package com.plural_pinelabs.expresscheckoutsdk.common

import android.util.Log
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLPeerUnverifiedException

@PublishedApi
internal fun mapNetworkFailure(throwable: Throwable): BaseResult.Error {
    val causeChain = generateSequence(throwable) { it.cause }.toList()
    runCatching {
        Log.e(
            "PineLabsSDK",
            "Request failed with ${throwable.javaClass.name}",
            throwable
        )
    }

    return when {
        causeChain.any { it is SSLPeerUnverifiedException || it is SSLException } ->
            BaseResult.Error(
                ErrorCode.TLS_FAILURE.code,
                "TLS_FAILURE",
                "Secure connection validation failed."
            )

        causeChain.any { it is SocketTimeoutException } ->
            BaseResult.Error(
                ErrorCode.NETWORK_TIMEOUT.code,
                "NETWORK_TIMEOUT",
                "The request timed out. Please try again."
            )

        causeChain.any { it is UnknownHostException } ->
            BaseResult.Error(
                ErrorCode.DNS_FAILURE.code,
                "DNS_FAILURE",
                "The payment service could not be reached."
            )

        causeChain.any { it is ConnectException || it is IOException } ->
            BaseResult.Error(
                ErrorCode.NETWORK_FAILURE.code,
                "NETWORK_FAILURE",
                "A network error occurred. Please try again."
            )

        else -> BaseResult.Error(
            ErrorCode.EXCEPTION_THROWN.code,
            "UNEXPECTED_ERROR",
            "An unexpected SDK error occurred."
        )
    }
}
