package com.plural_pinelabs.expresscheckoutsdk.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {
    val timeLeft: LiveData<Long> = TimerManager.timeLeft
}