package com.plural_pinelabs.expresscheckoutsdk

interface ExpressSDKCallback {

    fun onError(
        errorCode: String?,
        errorMessage: String?,
        errorDescription: String?
    )

    fun onSuccess(
        responseCode: String?,
        responseMessage: String?,
        responseDescription: String?
    )

    fun onCancel(
        responseCode: String?,
        responseMessage: String?,
        responseDescription: String?
    )
}