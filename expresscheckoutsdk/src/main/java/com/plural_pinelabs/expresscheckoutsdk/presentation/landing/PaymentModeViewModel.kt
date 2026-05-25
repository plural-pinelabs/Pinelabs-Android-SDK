package com.plural_pinelabs.expresscheckoutsdk.presentation.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.CreateWalletRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.CreateWalletResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletAddMoneyRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletAddMoneyResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletResetOtpResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletValidateRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletValidateResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentModeViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) : ViewModel() {

    private val _processPaymentResult =
        MutableStateFlow<BaseResult<ProcessPaymentResponse>>(BaseResult.Loading(false))
    val processPaymentResult: StateFlow<BaseResult<ProcessPaymentResponse>> = _processPaymentResult

    private val _createWalletResult =
        MutableStateFlow<BaseResult<CreateWalletResponse>>(BaseResult.Loading(false))
    val createWalletResult: StateFlow<BaseResult<CreateWalletResponse>> = _createWalletResult

    private val _addMoneyToWalletResult =
        MutableStateFlow<BaseResult<WalletAddMoneyResponse>>(BaseResult.Loading(false))
    val addMoneyToWalletResult: StateFlow<BaseResult<WalletAddMoneyResponse>> = _addMoneyToWalletResult

    private val _submitOtpResult =
        MutableStateFlow<BaseResult<OTPResponse>>(BaseResult.Loading(false))
    val submitOtpResult: StateFlow<BaseResult<OTPResponse>> = _submitOtpResult

    private val _walletValidateResult =
        MutableStateFlow<BaseResult<WalletValidateResponse>>(BaseResult.Loading(false))
    val walletValidateResult: StateFlow<BaseResult<WalletValidateResponse>> = _walletValidateResult

    private val _resetWalletOtpResult =
        MutableStateFlow<BaseResult<WalletResetOtpResponse>>(BaseResult.Loading(false))
    val resetWalletOtpResult: StateFlow<BaseResult<WalletResetOtpResponse>> =
        _resetWalletOtpResult

    fun createWallet(token: String?, request: CreateWalletRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.createWallet(token, request).collect {
                _createWalletResult.value = it
            }
        }

    fun resetCreateWalletState() {
        _createWalletResult.value = BaseResult.Loading(false)
    }

    fun processPayment(token: String?, paymentData: ProcessPaymentRequest?) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.processPayment(token, paymentData).collect {
                _processPaymentResult.value = it
            }
        }

    fun resetProcessPaymentState() {
        _processPaymentResult.value = BaseResult.Loading(false)
    }


    fun addMoneyToWallet(token: String?, request: WalletAddMoneyRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.addMoneyWallet(token, request).collect {
                _addMoneyToWalletResult.value = it
            }
        }

    fun submitOtp(token: String?, otpRequest: OTPRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.submitOTP(token, otpRequest).collect {
                _submitOtpResult.value = it
            }
        }

    fun validateWalletBalance(token: String?, request: WalletValidateRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.validateWalletBalance(token, request).collect {
                _walletValidateResult.value = it
            }
        }

    fun resetWalletOtp(token: String?, customerId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.resetWalletOtp(token, customerId).collect {
                _resetWalletOtpResult.value = it
            }
        }

    fun resetSubmitOtpState() {
        _submitOtpResult.value = BaseResult.Loading(false)
    }

    fun resetWalletValidateState() {
        _walletValidateResult.value = BaseResult.Loading(false)
    }

    fun resetWalletOtpState() {
        _resetWalletOtpResult.value = BaseResult.Loading(false)
    }
}

