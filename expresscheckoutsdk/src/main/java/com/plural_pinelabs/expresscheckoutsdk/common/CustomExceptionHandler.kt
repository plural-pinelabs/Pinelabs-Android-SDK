package com.plural_pinelabs.expresscheckoutsdk.common

import android.util.Log
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.data.model.LogRequest
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.RetrofitBuilder
import com.plural_pinelabs.expresscheckoutsdk.logger.SdkLogger
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class CustomExceptionHandler(
    private val applicationContext: android.content.Context,
    private val originalHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Handle the exception (e.g., log it, report it, etc.)
        Log.e("ExpressLibrary", "Uncaught exception: ${throwable.message}", throwable)
        SdkLogger.log(
            applicationContext,
            errorCode = "UNCAUGHT_EXCEPTION",
            errorMessage = throwable.message + " " + throwable.stackTraceToString(),
            transactionId = ExpressSDKObject.getFetchData()?.transactionInfo?.orderId ?: "",
            severity = "CRASH",
            source = "SDK"
        )

        // Optionally prevent crash by not calling originalHandler
        // But be cautious: suppressing all crashes can hide critical issues
        CleverTapUtil.sdkCrashed(
            CleverTapUtil.getInstance(applicationContext),
            ExpressSDKObject.getFetchData(),
            errorCode = "UNCAUGHT_EXCEPTION",
            errorMessage = "${throwable.message}",
            throwable.stackTraceToString()
        )

        try {
            runBlocking {
                withTimeout(3000) { // Optional: timeout to avoid hanging
                    val repo = ExpressRepositoryImpl(
                        RetrofitBuilder.fetchApiService,
                        NetworkHelper(applicationContext)
                    )
                    val logs = Utils.getUnSyncedErrors(applicationContext)
                    repo.logData(
                        ExpressSDKObject.getToken(), LogRequest(logs)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ExpressLibrary", "Failed to report crash", e)
        } finally {
            // If you want to let the app crash after logging:
            originalHandler?.uncaughtException(thread, throwable)
        }

    }
}
