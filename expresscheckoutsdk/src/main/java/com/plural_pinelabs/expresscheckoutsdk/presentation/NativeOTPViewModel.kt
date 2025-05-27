package com.plural_pinelabs.expresscheckoutsdk.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NativeOTPViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) :
    ViewModel() {

    private val _otpRequestResult =
        MutableStateFlow<BaseResult<OTPResponse>>(BaseResult.Loading(true))
    val otpRequestResult: StateFlow<BaseResult<OTPResponse>> = _otpRequestResult

    private val _otpSubmitResult =
        MutableStateFlow<BaseResult<OTPResponse>>(BaseResult.Loading(true))
    val otpSubmitResult: StateFlow<BaseResult<OTPResponse>> = _otpSubmitResult

    fun submitOtp(token: String?, otpRequest: OTPRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.submitOTP(token, otpRequest).collect { values ->
                _otpRequestResult.value = values
            }
        }


    fun generateOTP(token: String?, otpRequest: OTPRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.requestOTP(token, otpRequest).collect { values ->
                _otpRequestResult.value = values
            }
        }

}