package com.plural_pinelabs.expresscheckoutsdk.presentation.acs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_TRANSACTION_STATUS_INTERVAL
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ACSFragmentViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) :
    ViewModel() {

    private val _transactionStatusResult =
        MutableStateFlow<BaseResult<TransactionStatusResponse>>(BaseResult.Loading(false))
    val transactionStatusResult: StateFlow<BaseResult<TransactionStatusResponse>> =
        _transactionStatusResult

    private var pollingJob: Job? = null


    fun getTransactionStatus(token: String?) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.transactionStatus(token).collect {
                _transactionStatusResult.value = it
            }
        }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                getTransactionStatus(ExpressSDKObject.getToken())
                delay(UPI_TRANSACTION_STATUS_INTERVAL)
            }
        }
    }


    fun stopPolling() {
        pollingJob?.cancel()
    }
}