package com.plural_pinelabs.expresscheckoutsdk.logger

import android.content.Context
import com.google.gson.Gson
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.LogData
import com.plural_pinelabs.expresscheckoutsdk.data.model.SDKErrorDetails

object SdkLogger {

    private val gson = Gson()

    fun log(
        context: Context,
        errorCode: String?,
        errorMessage: String?,
        transactionId: String?,
        severity: String = "HIGH",
        source: String = "SDK"
    ) {
        val logCount = Utils.getLogCount()
        val sdkData = if (logCount <= 0) Utils.createSDKData(context) else null
        val logData = LogData(
            errorCode = errorCode,
            errorDetails = SDKErrorDetails(
                transactionId,
                sdkData,
                errorMessage
            ),
            severity = severity,
            source = source,
            timestamp = System.currentTimeMillis()
        )

        Utils.insertLog(context, gson.toJson(logData), logData.timestamp)
    }
}
