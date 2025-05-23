package com.plural_pinelabs.expresscheckoutsdk.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
            val c = call()
            c?.let { response ->
                try {
                    if (c.isSuccessful && c.body() != null) {
                        c.body()?.let {
                            emit(BaseResult.Success(it))
                        }
                    } else {
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
                } catch (e: Exception) {

                    emit(
                        BaseResult.Error(
                            ErrorCode.EXCEPTION_THROWN.code,
                            e.message,
                            e.stackTrace.toString()
                        )
                    )
                }
            }
        } else {
            emit(BaseResult.Error(ErrorCode.INTERNET_NOT_AVAILABLE.code))
        }
    }.flowOn(Dispatchers.IO)
}

