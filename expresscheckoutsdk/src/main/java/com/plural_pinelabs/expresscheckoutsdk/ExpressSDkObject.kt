package com.plural_pinelabs.expresscheckoutsdk

import android.content.Context
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import java.util.concurrent.atomic.AtomicReference

internal data class SDKObject(
    val context: Context,
    val callback: ExpressSDKCallback,
    val token: String,
    var fetchResponseDTO: FetchResponseDTO? = null,
    var processPaymentResponse: ProcessPaymentResponse? = null,
)

internal object ExpressSDKObject {
    private val sdkObjectRef = AtomicReference<SDKObject?>()

    fun initialize(context: Context, callback: ExpressSDKCallback, token: String) {
        sdkObjectRef.set(SDKObject(context, callback, token))
    }

    fun getToken(): String? {
        return sdkObjectRef.get()?.token
    }

    fun getSDKObject(): SDKObject? {
        return sdkObjectRef.get()
    }

    fun setFetchData(it: FetchResponseDTO) {
        getSDKObject()?.fetchResponseDTO = it
    }

    fun getFetchData(): FetchResponseDTO? {
        return getSDKObject()?.fetchResponseDTO
    }

    fun getAmount(): Int {
        val fetchResponse = getFetchData()
        return (fetchResponse?.paymentData?.originalTxnAmount?.amount ?: run {
            -1
        })
    }

    fun getCurrency(): String {
        val fetchResponse = getFetchData()
        return (fetchResponse?.paymentData?.originalTxnAmount?.currency ?: run {
            ""
        })
    }

    fun setProcessPaymentResponse(it: ProcessPaymentResponse) {
        getSDKObject()?.processPaymentResponse = it
    }

    fun getProcessPaymentResponse(): ProcessPaymentResponse? {
        return getSDKObject()?.processPaymentResponse
    }
}


