package com.plural_pinelabs.expresscheckoutsdk

import android.content.Context
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import java.util.concurrent.atomic.AtomicReference

internal data class SDKObject(
    val context: Context,
    val callback: ExpressSDKCallback,
    val token: String,
    var fetchResponseDTO: FetchResponseDTO? = null,
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
        return fetchResponse?.paymentData?.originalTxnAmount?.amount ?: run {
            -1
        }
    }
}


