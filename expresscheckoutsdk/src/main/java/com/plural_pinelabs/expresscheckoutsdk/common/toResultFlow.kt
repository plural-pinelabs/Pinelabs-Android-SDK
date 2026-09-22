package com.plural_pinelabs.expresscheckoutsdk.common

import com.google.gson.Gson
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchError
import kotlinx.coroutines.CancellationException
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
                if (c == null) {
                    emit(
                        BaseResult.Error(
                            ErrorCode.EXCEPTION_THROWN.code,
                            "EMPTY_RESPONSE",
                            "The payment service returned no response."
                        )
                    )
                } else {
                    val response = c
                    if (c.isSuccessful && c.body() != null) {
                        c.body()?.let {
                            emit(BaseResult.Success(it))
                        }
                    } else {
                        val errorResponse = parseFetchError(response.errorBody()?.charStream())
                        emit(
                            BaseResult.Error(
                                errorResponse?.error_code ?: ErrorCode.INTERNAL_SERVER_ERROR.code,
                                errorResponse?.error_message ?: "HTTP_ERROR",
                                "Payment service returned HTTP ${response.code()}."
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                emit(mapNetworkFailure(e))
            }
        } else {
            emit(
                BaseResult.Error(
                    ErrorCode.INTERNET_NOT_AVAILABLE.code,
                    "INTERNET_NOT_AVAILABLE",
                    "No internet connection is available."
                )
            )
        }
    }.catch { e ->
        if (e is CancellationException) throw e
        emit(mapNetworkFailure(e))
    }.flowOn(Dispatchers.IO)
}

@PublishedApi
internal fun parseFetchError(reader: java.io.Reader?): FetchError? = runCatching {
    reader?.use { Gson().fromJson(it, FetchError::class.java) }
}.getOrNull()
