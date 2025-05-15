package com.plural_pinelabs.expresscheckoutsdk

import android.content.Context
import java.util.concurrent.atomic.AtomicReference

internal data class SDKObject(
    val context: Context,
    val callback: ExpressSDKCallback,
    val token: String,
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
}


