package com.plural_pinelabs.expresscheckoutsdk.presentation.netbanking

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_TRANSACTION_STATUS_INTERVAL
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NetBankingViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) :
    ViewModel() {

    private var pollingJob: Job? = null

    var isShowingUPIDialog = false


    private val _countDownTimer = MutableStateFlow<Long>(-1)
    val countDownTimer: StateFlow<Long> = _countDownTimer

    private val _processPaymentResult =
        MutableStateFlow<BaseResult<ProcessPaymentResponse>>(BaseResult.Loading(false))
    val processPaymentResult: StateFlow<BaseResult<ProcessPaymentResponse>> = _processPaymentResult


    private val _transactionStatusResult =
        MutableStateFlow<BaseResult<TransactionStatusResponse>>(BaseResult.Loading(false))
    val transactionStatusResult: StateFlow<BaseResult<TransactionStatusResponse>> =
        _transactionStatusResult

    fun processPayment(token: String?, paymentData: ProcessPaymentRequest?) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.processPayment(token, paymentData).collect {
                _processPaymentResult.value = it
            }
        }

    fun getTransactionStatus(token: String?) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.transactionStatus(token).collect {
                _transactionStatusResult.value = it
            }
        }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                getTransactionStatus(ExpressSDKObject.getToken())
                delay(UPI_TRANSACTION_STATUS_INTERVAL)
            }
        }
    }


    fun stopPolling() {
        pollingJob?.cancel()
    }

    fun resetTransactionResponse() {
        _transactionStatusResult.value = BaseResult.Loading(false)
    }

    fun startCountDownTimer() {
        val totalTime = 600000L
        val interval = 1000L
        viewModelScope.launch(Dispatchers.Main) {
            object : CountDownTimer(totalTime, interval) {
                override fun onTick(millisUntilFinished: Long) {
                    _countDownTimer.value = millisUntilFinished
                }

                override fun onFinish() {
                    _countDownTimer.value = 0L
                    // Optionally, you can reset the process payment result here
                }
            }.start()
        }
    }

}