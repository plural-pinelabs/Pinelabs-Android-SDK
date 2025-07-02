package com.plural_pinelabs.expresscheckoutsdk.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode

class PaymentModeSharedViewModel : ViewModel() {
    val retryEvent = MutableLiveData<Boolean>()
    val selectedPaymentMethod = MutableLiveData<PaymentMode>()
}
