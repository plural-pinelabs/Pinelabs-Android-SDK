package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VerifyOTPFragmentViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) :
    ViewModel() {


    private val _requestOTPResult =
        MutableStateFlow<BaseResult<SavedCardResponse>>(BaseResult.Loading(true))
    val requestOTPResult: StateFlow<BaseResult<SavedCardResponse>> = _requestOTPResult

    private val _submitOTPResult =
        MutableStateFlow<BaseResult<SavedCardResponse>>(BaseResult.Loading(true))
    val submitOTPResult: StateFlow<BaseResult<SavedCardResponse>> = _submitOTPResult

    fun requestOTP(otpRequest: OTPRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.sendOTPCustomer(token = ExpressSDKObject.getToken(), otpRequest)
                .collect {
                    _requestOTPResult.value = it
                }
        }
    }

    fun submitOTP(token: String?, otpRequest: OTPRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.validateOTPCustomer(
                token = token,
                otpRequest
            )
                .collect {
                    _requestOTPResult.value = it
                }
        }
    }

    fun validateUpdateOrderDetails(token: String?, otpRequest: OTPRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.validateOTPCustomer(
                token = token,
                otpRequest
            )
                .collect {
                    _requestOTPResult.value = it
                }
        }
    }



}