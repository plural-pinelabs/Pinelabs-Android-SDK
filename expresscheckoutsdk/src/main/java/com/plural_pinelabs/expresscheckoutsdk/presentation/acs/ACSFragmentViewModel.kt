package com.plural_pinelabs.expresscheckoutsdk.presentation.acs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ACSFragmentViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) :
    ViewModel() {

    private val _transactionStatusResult =
        MutableStateFlow<BaseResult<TransactionStatusResponse>>(BaseResult.Loading(false))
    val transactionStatusResult: StateFlow<BaseResult<TransactionStatusResponse>> =
        _transactionStatusResult

    fun getTransactionStatus(token: String?) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.transactionStatus(token).collect {
                _transactionStatusResult.value = it
            }
        }
}