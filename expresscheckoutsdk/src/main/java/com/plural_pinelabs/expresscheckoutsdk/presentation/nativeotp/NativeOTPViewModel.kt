package com.plural_pinelabs.expresscheckoutsdk.presentation.nativeotp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TIMEOUT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_TRANSACTION_STATUS_INTERVAL
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NativeOTPViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) :
    ViewModel() {

    private val _otpSubmitResult =
        MutableStateFlow<BaseResult<OTPResponse>>(BaseResult.Loading(false))
    val otpSubmitResult: StateFlow<BaseResult<OTPResponse>> = _otpSubmitResult

    private val _transactionStatusResult =
        MutableStateFlow<BaseResult<TransactionStatusResponse>>(BaseResult.Loading(false))
    val transactionStatusResult: StateFlow<BaseResult<TransactionStatusResponse>> =
        _transactionStatusResult
    private var pollingJob: Job? = null

    fun submitOtp(token: String?, otpRequest: OTPRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.submitOTP(token, otpRequest).collect { values ->
                _otpSubmitResult.value = values
            }
        }



    fun getTransactionStatus(token: String?) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.transactionStatus(token).collect { values ->
                _transactionStatusResult.value = values
            }
        }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            val timeoutAt = System.currentTimeMillis() + (TIMEOUT * 1000)
            while (isActive && System.currentTimeMillis() < timeoutAt) {
                getTransactionStatus(ExpressSDKObject.getToken())
                delay(UPI_TRANSACTION_STATUS_INTERVAL)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }

}