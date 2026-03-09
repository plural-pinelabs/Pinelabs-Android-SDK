package com.plural_pinelabs.expresscheckoutsdk

interface ExpressSDKCallback {

    fun onError(
        errorCode: String?,
        errorMessage: String?,
        errorDescription: String?,
        orderId:String?=null
    )

    fun onSuccess(
        responseCode: String?,
        responseMessage: String?,
        responseDescription: String?,
        orderId:String?=null
    )

    fun onCancel(
        responseCode: String?,
        responseMessage: String?,
        responseDescription: String?,
        orderId:String?=null
    )
}