package com.plural_pinelabs.expresscheckoutsdk.common

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

internal object TimerManager {
    private val _timeLeft = MutableLiveData<Long>()
    val timeLeft: LiveData<Long> get() = _timeLeft

    private var timer: CountDownTimer? = null

    fun startTimer(durationInMillis: Long) {
        timer?.cancel() // Cancel any existing timer
        timer = object : CountDownTimer(durationInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.postValue(millisUntilFinished / 1000) // Update time left in seconds
            }

            override fun onFinish() {
                _timeLeft.postValue(0) // Notify timer finished
            }
        }.start()
    }

    fun stopTimer() {
        timer?.cancel()
        _timeLeft.postValue(0)
    }
}