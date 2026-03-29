package com.plural_pinelabs.expresscheckoutsdk.presentation.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.CreateWalletRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.CreateWalletResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentModeViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) : ViewModel() {

    private val _createWalletResult =
        MutableStateFlow<BaseResult<CreateWalletResponse>>(BaseResult.Loading(false))
    val createWalletResult: StateFlow<BaseResult<CreateWalletResponse>> = _createWalletResult

    fun createWallet(token: String?, request: CreateWalletRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.createWallet(token, request).collect {
                _createWalletResult.value = it
            }
        }

    fun resetCreateWalletState() {
        _createWalletResult.value = BaseResult.Loading(false)
    }
}

