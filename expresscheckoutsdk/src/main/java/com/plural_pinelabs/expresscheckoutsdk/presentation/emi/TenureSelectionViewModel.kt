package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.ConvenienceFeesInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.KFSResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TenureSelectionViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) :
    ViewModel() {

    var selectedConvenienceFee: ConvenienceFeesInfo? = null

    private val _kfsRequestResult =
        MutableStateFlow<BaseResult<KFSResponse>>(BaseResult.Loading(false))
    val kfsRequestResult: StateFlow<BaseResult<KFSResponse>> = _kfsRequestResult


    fun getKFS(token: String?, paymentData: ProcessPaymentRequest?) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.getKFS(token, paymentData).collect {
                _kfsRequestResult.value = it
            }
        }

}