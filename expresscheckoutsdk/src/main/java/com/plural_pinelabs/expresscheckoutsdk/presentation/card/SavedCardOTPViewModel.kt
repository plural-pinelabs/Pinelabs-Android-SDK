package com.plural_pinelabs.expresscheckoutsdk.presentation.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SavedCardOTPViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) :
    ViewModel() {


    private val _sendOTPResult =
        MutableStateFlow<BaseResult<SavedCardResponse>>(BaseResult.Loading(false))
    val sendOTPResult: StateFlow<BaseResult<SavedCardResponse>> = _sendOTPResult

    private val _verifyOTPResult =
        MutableStateFlow<BaseResult<SavedCardResponse>>(BaseResult.Loading(false))
    val verifyOTPResult: StateFlow<BaseResult<SavedCardResponse>> = _verifyOTPResult

    fun submitOtp(token: String?, otpRequest: OTPRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.sendOTPCustomer(token, otpRequest).collect { values ->
                _sendOTPResult.value = values
            }
        }


    fun generateOTP(token: String?, otpRequest: OTPRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.sendOTPCustomer(token, otpRequest).collect { values ->
                _sendOTPResult.value = values
            }
        }

    fun validateUpdateOrderDetails(token: String?, otpRequest: OTPRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.validateOTPCustomer(
                token = token,
                otpRequest
            )
                .collect {
                    _verifyOTPResult.value = it
                }
        }
    }
}