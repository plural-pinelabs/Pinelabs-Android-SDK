package com.plural_pinelabs.expresscheckoutsdk.common

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

inline fun <reified T> toResultFlow(
    networkHelper: NetworkHelper,
    crossinline call: suspend () -> retrofit2.Response<T>?
): Flow<BaseResult<T>> {

    return flow {

        var idlingAcquired = false   // 🧠 local guard flag

        // 🔓 Acquire idling ONLY in test mode
        if (SdkTestMode.enabled) {
            SdkE2ETestController.idlingResource.increment()
            idlingAcquired = true
        }

        try {
            val isInternetConnected = networkHelper.hasInternetConnection()
            if (!isInternetConnected) {
                emit(BaseResult.Error(ErrorCode.INTERNET_NOT_AVAILABLE.code))
                return@flow
            }

            emit(BaseResult.Loading(true))

            val c = call()   // 🌍 REAL BACKEND CALL

            c?.let { response ->
                Log.d("Toresultflow", "Response successful=${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    emit(BaseResult.Success(response.body()!!))
                } else {
                    val type = object : TypeToken<FetchError>() {}.type
                    val errorResponse: FetchError? =
                        Gson().fromJson(response.errorBody()?.charStream(), type)

                    emit(
                        BaseResult.Error(
                            errorResponse?.error_code
                                ?: ErrorCode.INTERNAL_SERVER_ERROR.code,
                            errorResponse?.error_message
                        )
                    )
                }
            }

        } catch (e: Exception) {
            Log.e("Toresultflow", "exception error: ${e.message}", e)

            emit(
                BaseResult.Error(
                    ErrorCode.EXCEPTION_THROWN.code,
                    e.message,
                    e.stackTraceToString()
                )
            )

        } finally {
            // 🔒 Guaranteed release (only if acquired)
            if (SdkTestMode.enabled && idlingAcquired) {
                if (!SdkE2ETestController.idlingResource.isIdleNow) {
                    SdkE2ETestController.idlingResource.decrement()
                }
            }
        }

    }.catch { e ->

        // 🧯 Absolute safety net (flow cancellation / upstream crash)
        if (SdkTestMode.enabled && !SdkE2ETestController.idlingResource.isIdleNow) {
            SdkE2ETestController.idlingResource.decrement()
        }

        Log.e("Toresultflow", "Caught exception in flow: ${e.message}", e)

        when (e) {
            is java.net.SocketTimeoutException -> {
                emit(
                    BaseResult.Error(
                        ErrorCode.UNKNOWN_PAYMENT_ERROR.code,
                        "Request timed out. Please try again.",
                        e.stackTraceToString()
                    )
                )
            }

            else -> {
                emit(
                    BaseResult.Error(
                        ErrorCode.EXCEPTION_THROWN.code,
                        e.message,
                        e.stackTraceToString()
                    )
                )
            }
        }

    }.flowOn(Dispatchers.IO)
}
