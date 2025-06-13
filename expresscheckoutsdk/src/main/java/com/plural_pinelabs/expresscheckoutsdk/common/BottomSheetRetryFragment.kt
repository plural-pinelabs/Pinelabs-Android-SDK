package com.plural_pinelabs.expresscheckoutsdk.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.data.model.Palette
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode

class BottomSheetRetryFragment(
    amount: String,
    retryAcs: Boolean,
    paymentModes: List<PaymentMode>?,
    palette: Palette?,
    token: String?,
    errorMessage: String?
) :
    BottomSheetDialogFragment() {

    var paymentModesil: List<PaymentMode>? = paymentModes
    var token: String? = token
    var retryAcs = retryAcs
    var amount = amount
    var palette = palette
    var errorMessage = errorMessage
    var paymentId: String? = null

    private lateinit var txt_payment: TextView
    private lateinit var close_icon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.retry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}