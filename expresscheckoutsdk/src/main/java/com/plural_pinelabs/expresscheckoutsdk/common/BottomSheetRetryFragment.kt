package com.plural_pinelabs.expresscheckoutsdk.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAY_BY_POINTS_ID
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode
import com.plural_pinelabs.expresscheckoutsdk.presentation.landing.PaymentModeRecyclerViewAdapter
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration

class BottomSheetRetryFragment(
) :
    BottomSheetDialogFragment() {
    private lateinit var paymentModeRecyclerView: RecyclerView
    private lateinit var retryText: TextView
    private lateinit var closeIcon: ImageView

    private val viewModel by activityViewModels<PaymentModeSharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.retry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retryText = view.findViewById(R.id.txt_payment)
        retryText.text = getString(
            R.string.retry_payment_of_9_449,
            Utils.convertInRupees(ExpressSDKObject.getAmount()).toString()
        )
        closeIcon = view.findViewById(R.id.x_icon)
        closeIcon.setOnClickListener {
            Utils.showCancelPaymentDialog(requireContext())
        }
        paymentModeRecyclerView = view.findViewById(R.id.recycler_payment_options)
        setPaymentMode()
    }

    private fun setPaymentMode() {
        paymentModeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        paymentModeRecyclerView.addItemDecoration(
            DividerItemDecoration(
                ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_divider)
            )
        )
        val paymentModes = getPaymentModes()
        paymentModes?.let {
            val adapter =
                PaymentModeRecyclerViewAdapter(
                    requireContext(),
                    it,
                    isPBPEnabled(paymentModes),
                    getPaymentModeSelectionCallback()
                )
            paymentModeRecyclerView.adapter = adapter
        }
    }

    private fun getPaymentModes(): List<PaymentMode>? {
        val availablePaymentModes: ArrayList<String> = arrayListOf()
        PaymentModes.entries.forEach {
            availablePaymentModes.add(it.paymentModeID.lowercase())
        }
        val filteredPaymentModes = ExpressSDKObject.getFetchData()?.paymentModes?.filter {
            availablePaymentModes.contains(it.paymentModeId.lowercase())
        }
        return filteredPaymentModes
    }

    private fun isPBPEnabled(paymentModes: List<PaymentMode>): Boolean {
        return paymentModes.any { it.paymentModeId == PAY_BY_POINTS_ID }
    }

    private fun getPaymentModeSelectionCallback(): ItemClickListener<PaymentMode> {
        return object : ItemClickListener<PaymentMode> {
            override fun onItemClick(position: Int, item: PaymentMode) {
                dismissAllowingStateLoss()
                viewModel.selectedPaymentMethod.value = item
            }
        }
    }

}