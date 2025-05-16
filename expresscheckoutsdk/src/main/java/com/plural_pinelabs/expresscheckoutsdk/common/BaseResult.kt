package com.plural_pinelabs.expresscheckoutsdk.common

sealed class BaseResult<out T> {
    data class Success<out T>(val data: T) : BaseResult<T>()
    data class Error(
        val errorCode: String, val errorMessage: String? = null, val errorDescription: String? = null
    ) : BaseResult<Nothing>()

    data class Loading(val isLoading: Boolean) : BaseResult<Nothing>()
}