package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_TRANSACTION_STATUS_INTERVAL
import com.plural_pinelabs.expresscheckoutsdk.data.model.CancelTransactionResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ConvenienceFeesInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferEligibilityResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiFetchVpaRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiFetchVpaResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiOfferValidateRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletResetOtpResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class UPIViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) : ViewModel() {

    companion object {
        const val PAYMENT_TIMER_TOTAL_MILLIS = 600_000L
    }

    private val _processPaymentResult =
        MutableStateFlow<BaseResult<ProcessPaymentResponse>>(BaseResult.Loading(false))
    val processPaymentResult: StateFlow<BaseResult<ProcessPaymentResponse>> = _processPaymentResult

    private val _transactionStatusResult =
        MutableStateFlow<BaseResult<TransactionStatusResponse>>(BaseResult.Loading(false))
    val transactionStatusResult: StateFlow<BaseResult<TransactionStatusResponse>> =
        _transactionStatusResult

    private val _cancelTransactionResult =  MutableStateFlow<BaseResult<CancelTransactionResponse>>(BaseResult.Loading(false))
    val cancelTransactionResult: StateFlow<BaseResult<CancelTransactionResponse>> =
        _cancelTransactionResult

    private val _submitOtpResult =
        MutableStateFlow<BaseResult<OTPResponse>>(BaseResult.Loading(false))
    val submitOtpResult: StateFlow<BaseResult<OTPResponse>> = _submitOtpResult

    private val _resetWalletOtpResult =
        MutableStateFlow<BaseResult<WalletResetOtpResponse>>(BaseResult.Loading(false))
    val resetWalletOtpResult: StateFlow<BaseResult<WalletResetOtpResponse>> =
        _resetWalletOtpResult

    private val _countDownTimer = MutableStateFlow<Long>(-1)
    val countDownTimer: StateFlow<Long> = _countDownTimer
    var selectedConvenienceFee: ConvenienceFeesInfo? = null


    private var pollingJob: Job? = null
    private var paymentCountDownTimer: CountDownTimer? = null

    var isShowingVPADialog = false
    var isShowingUPIDialog = false

    fun processPayment(token: String?, paymentData: ProcessPaymentRequest?) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.processPayment(token, paymentData).collect {
                _processPaymentResult.value = it
            }
        }

    fun resetPaymentFlowResponse() {
        _processPaymentResult.value = BaseResult.Loading(false)
    }

    fun resetTransactionResponse() {
        _transactionStatusResult.value = BaseResult.Loading(false)
    }

    fun submitOtp(token: String?, otpRequest: OTPRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.submitOTP(token, otpRequest).collect {
                _submitOtpResult.value = it
            }
        }

    fun resetSubmitOtpState() {
        _submitOtpResult.value = BaseResult.Loading(false)
    }

    fun resetWalletOtp(token: String?, customerId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.resetWalletOtp(token, customerId).collect {
                _resetWalletOtpResult.value = it
            }
        }

    fun resetWalletOtpState() {
        _resetWalletOtpResult.value = BaseResult.Loading(false)
    }


    fun getTransactionStatus(token: String?, orderId: String? = null) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.transactionStatus(token, orderId).collect {
                _transactionStatusResult.value = it
            }
        }

    fun startCountDownTimer() {
        val interval = 1000L
        paymentCountDownTimer?.cancel()
        _countDownTimer.value = PAYMENT_TIMER_TOTAL_MILLIS
        paymentCountDownTimer = object : CountDownTimer(PAYMENT_TIMER_TOTAL_MILLIS, interval) {
            override fun onTick(millisUntilFinished: Long) {
                _countDownTimer.value = millisUntilFinished
            }

            override fun onFinish() {
                paymentCountDownTimer = null
                _countDownTimer.value = 0L
                // Optionally, you can reset the process payment result here
            }
        }.start()
    }

    fun stopCountDownTimer() {
        paymentCountDownTimer?.cancel()
        paymentCountDownTimer = null
        _countDownTimer.value = -1L
    }

    fun startPolling(orderId: String? = null) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                getTransactionStatus(ExpressSDKObject.getToken(), orderId)
                delay(UPI_TRANSACTION_STATUS_INTERVAL)
            }
        }
    }


    fun stopPolling() {
        pollingJob?.cancel()
    }

    fun cancelPayment(){
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.cancelPayment(ExpressSDKObject.getToken(),true).collect{
                _cancelTransactionResult.value = it
            }
        }
    }

    suspend fun fetchUpiVpa(
        token: String?,
        request: UpiFetchVpaRequest,
    ): BaseResult<UpiFetchVpaResponse> {
        return expressRepositoryImpl.fetchUpiVpa(token, request)
            .first { result -> result !is BaseResult.Loading }
    }

    suspend fun validateUpiOffer(
        token: String?,
        request: UpiOfferValidateRequest?,
    ): BaseResult<OfferEligibilityResponse> {
        return expressRepositoryImpl.validateOffersV2(token, request)
            .first { result -> result !is BaseResult.Loading }
    }

}