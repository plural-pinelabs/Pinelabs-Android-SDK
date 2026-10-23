package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import android.os.CountDownTimer
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_PAYMENT_INQUIRY_INTERVAL
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
    private val paymentSession = UpiPaymentSession()
    private val inquiryMutex = Mutex()

    val isUpiPaymentInProgress: Boolean
        get() = paymentSession.isActive

    val shouldShowPaymentTimer: Boolean
        get() = paymentSession.shouldShowPaymentTimer

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
        val remainingMillis = remainingPaymentTimeMillis()
        paymentCountDownTimer?.cancel()
        _countDownTimer.value = remainingMillis
        if (remainingMillis <= 0L) return
        paymentCountDownTimer = object : CountDownTimer(remainingMillis, interval) {
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

    private fun startPolling(attempt: UpiPaymentSession.Attempt) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive && paymentSession.isCurrent(attempt)) {
                val result = inquiryMutex.withLock {
                    fetchInquiry(attempt)
                }
                if (paymentSession.isCurrent(attempt)) {
                    _transactionStatusResult.value = result
                }
                delay(UPI_PAYMENT_INQUIRY_INTERVAL)
            }
        }
    }

    fun beginPaymentMonitoring(
        orderId: String? = null,
        paymentId: String? = null,
        startTimer: Boolean = true,
    ) {
        paymentSession.begin(
            orderId = orderId,
            paymentId = paymentId,
            showPaymentTimer = startTimer,
            deadlineElapsedRealtimeMs = SystemClock.elapsedRealtime() + PAYMENT_TIMER_TOTAL_MILLIS,
        )
        if (startTimer) startCountDownTimer()
        paymentSession.currentAttempt()?.let(::startPolling)
    }

    fun ensurePaymentMonitoring() {
        val attempt = paymentSession.currentAttempt() ?: return
        if (paymentSession.shouldShowPaymentTimer && _countDownTimer.value == -1L) {
            startCountDownTimer()
        }
        if (pollingJob?.isActive != true) {
            startPolling(attempt)
        } else {
            requestImmediateInquiry(attempt)
        }
    }

    private fun requestImmediateInquiry(attempt: UpiPaymentSession.Attempt) {
        viewModelScope.launch(Dispatchers.IO) {
            // A periodic inquiry already in flight is sufficient reconciliation. Skipping here
            // avoids overlapping calls when both ActivityResult and onResume fire together.
            if (!inquiryMutex.tryLock()) return@launch
            try {
                val result = fetchInquiry(attempt)
                if (paymentSession.isCurrent(attempt)) {
                    _transactionStatusResult.value = result
                }
            } finally {
                inquiryMutex.unlock()
            }
        }
    }

    suspend fun performFinalInquiry(): BaseResult<TransactionStatusResponse>? {
        val attempt = paymentSession.currentAttempt() ?: return null
        stopPolling()
        return inquiryMutex.withLock {
            val result = fetchInquiry(attempt)
            result.takeIf { paymentSession.isCurrent(attempt) }
        }
    }

    private suspend fun fetchInquiry(
        attempt: UpiPaymentSession.Attempt,
    ): BaseResult<TransactionStatusResponse> {
        return expressRepositoryImpl.transactionStatus(
            token = ExpressSDKObject.getToken(),
            orderId = attempt.orderId,
            paymentId = attempt.paymentId,
        ).first { result -> result !is BaseResult.Loading }
    }

    fun tryHandleTerminalResult(): Boolean = paymentSession.tryHandleTerminalResult()

    fun remainingPaymentTimeMillis(): Long {
        val deadline = paymentSession.currentAttempt()?.deadlineElapsedRealtimeMs
            ?: return PAYMENT_TIMER_TOTAL_MILLIS
        return (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
    }

    internal fun resolveInquiryStatus(response: TransactionStatusResponse): UpiInquiryResolution {
        val transaction = response.data
        val orderSummary = transaction.order_summary
        return resolveUpiInquiryStatus(
            expectedOrderId = paymentSession.inquiryOrderId,
            expectedPaymentId = paymentSession.inquiryPaymentId,
            responseOrderId = orderSummary?.order_id ?: transaction.order_id,
            responsePaymentId = transaction.payment_id,
            orderStatus = orderSummary?.status,
            paymentStatus = transaction.status,
        )
    }

    fun finishPaymentMonitoring() {
        paymentSession.finish()
        stopCountDownTimer()
        stopPolling()
    }


    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
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
