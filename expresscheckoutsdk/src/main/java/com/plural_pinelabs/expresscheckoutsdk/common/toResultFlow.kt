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
        val isInternetConnected = networkHelper.hasInternetConnection()
        if (isInternetConnected) {
            emit(BaseResult.Loading(true))
            try {
                val c = call()
                c?.let { response ->
                    Log.d("Toresultflow", "Response: ${response.raw().body}")
                    Log.d("Toresultflow", "Response: ${response.raw().isSuccessful}")
                    Log.d("Toresultflow", "Response: ${response.raw().toString()}")
                    if (c.isSuccessful && c.body() != null) {
                        c.body()?.let {
                            emit(BaseResult.Success(it))
                        }
                    } else {
                        Log.d("Toresultflow", "Response: emit error  ${response.raw().body}")
                        val type = object : TypeToken<FetchError>() {}.type
                        val errorResponse: FetchError? =
                            Gson().fromJson(response.errorBody()?.charStream(), type)
                        emit(
                            BaseResult.Error(
                                errorResponse?.error_code ?: ErrorCode.INTERNAL_SERVER_ERROR.code,
                                errorResponse?.error_message
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.d("Toresultflow", "exception error: ${e.message}")

                emit(
                    BaseResult.Error(
                        ErrorCode.EXCEPTION_THROWN.code,
                        e.message,
                        e.stackTrace.toString()
                    )
                )
            }
        } else {
            emit(BaseResult.Error(ErrorCode.INTERNET_NOT_AVAILABLE.code))
        }
    }.catch { e ->
            Log.e("Toresultflow", "Caught exception: ${e.message}")
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

