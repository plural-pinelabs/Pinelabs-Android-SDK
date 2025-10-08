package com.plural_pinelabs.expresscheckoutsdk.presentation.terminal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.MTAG
import com.plural_pinelabs.expresscheckoutsdk.data.model.LogData
import com.plural_pinelabs.expresscheckoutsdk.data.model.LogRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.LogResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SuccessViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) :
    ViewModel() {
    private var isTransactionStatusAPiCalled = false

    private val _transactionStatusResult =
        MutableStateFlow<BaseResult<TransactionStatusResponse>>(BaseResult.Loading(false))
    val transactionStatusResult: StateFlow<BaseResult<TransactionStatusResponse>> =
        _transactionStatusResult

    private val _logsResult =
        MutableStateFlow<BaseResult<LogResponse>>(BaseResult.Loading(false))
    val logsResult: StateFlow<BaseResult<LogResponse>> =
        _logsResult



    fun getTransactionStatus(token: String?) {
        if (!isTransactionStatusAPiCalled) {
            isTransactionStatusAPiCalled = true
            viewModelScope.launch(Dispatchers.IO) {
                expressRepositoryImpl.transactionStatus(token).collect {
                    _transactionStatusResult.value = it
                }
            }
        }
    }

    fun logData(token: String?, logs: List<LogData>) {
        viewModelScope.launch(Dispatchers.IO) {
            val logRequest = LogRequest(logs)
            expressRepositoryImpl.logData(token, logRequest).collect {
                // No need to handle response
                _logsResult.value = it
            }
        }
    }
}