package com.plural_pinelabs.expresscheckoutsdk.presentation.terminal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_ATTEMPTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_PENDING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.SuccessViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.TimerManager
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.formatToReadableDate
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import kotlinx.coroutines.launch

class SuccessFragment : Fragment() {
    private lateinit var successParentLayout: ScrollView
    private lateinit var orderNumberLabelText: TextView
    private lateinit var orderNumberValueText: TextView
    private lateinit var dateTimeLabelText: TextView
    private lateinit var dateTimeValueText: TextView
    private lateinit var transactionIdLabelText: TextView
    private lateinit var transactionIdValueText: TextView
    private lateinit var amountPaidValue: TextView
    private lateinit var cardNameView: TextView
    private lateinit var cardLast4DigitsText: TextView

    private lateinit var viewModel: SuccessViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(
            this,
            SuccessViewModelFactory(NetworkHelper(requireContext()))
        )[SuccessViewModel::class.java]
        return inflater.inflate(R.layout.fragment_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        observeViewModel()
        viewModel.getTransactionStatus(ExpressSDKObject.getToken())
    }

    private fun setUpViews(view: View) {
        successParentLayout = view.findViewById(R.id.success_parent_layout)
        orderNumberLabelText = view.findViewById(R.id.order_number_label)
        orderNumberValueText = view.findViewById(R.id.order_number_value)
        dateTimeLabelText = view.findViewById(R.id.date_time_label)
        dateTimeValueText = view.findViewById(R.id.date_time_value)
        transactionIdLabelText = view.findViewById(R.id.transaction_id_label)
        transactionIdValueText = view.findViewById(R.id.transaction_id_value)
        amountPaidValue = view.findViewById(R.id.final_price_value)
        cardNameView = view.findViewById(R.id.card_name)
        cardLast4DigitsText = view.findViewById(R.id.card_number)



        successParentLayout.visibility = View.GONE
        (activity as LandingActivity).showHideHeaderLayout(false)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.transactionStatusResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            bottomSheetDialog?.dismiss()
                            findNavController().navigate(R.id.action_successFragment_to_failureFragment)
                        }

                        is BaseResult.Loading -> {
                            if (bottomSheetDialog?.isShowing == false && it.isLoading)
                                bottomSheetDialog = Utils.showProcessPaymentDialog(requireContext())
                        }

                        is BaseResult.Success<TransactionStatusResponse> -> {
                            bottomSheetDialog?.dismiss()
                            val status = it.data.data.status
                            when (status) {
                                PROCESSED_PENDING -> {
                                    findNavController().navigate(R.id.action_successFragment_to_retryFragment)
                                }

                                PROCESSED_STATUS -> {
                                    updateViews(it.data)
                                    //   handleTimer()
                                }

                                PROCESSED_ATTEMPTED -> {
                                    findNavController().navigate(R.id.action_successFragment_to_retryFragment)

                                }

                                PROCESSED_FAILED -> {
                                    findNavController().navigate(R.id.action_successFragment_to_failureFragment)
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private fun updateViews(response: TransactionStatusResponse) {
        val orderSummary = response.data.order_summary
        val paymentsData =
            orderSummary?.payments?.filter { it.status == PROCESSED_STATUS }
        val paymentData = paymentsData?.get(0)
        dateTimeValueText.text = formatToReadableDate(orderSummary?.updated_at ?: "")
        orderNumberValueText.text = response.data.order_id
        transactionIdValueText.text = orderSummary?.payments?.find { it.status == PROCESSED_STATUS }
            ?.id ?: ""
        if (paymentData != null) {
            amountPaidValue.text = Utils.convertInRupees(paymentData.payment_amount.value)
            val issuerName = paymentData.payment_option?.card_data?.issuer_name?.let { name ->
                when {
                    name.isBlank() -> ""
                    name.contains(" ") -> name.substring(0, name.indexOfFirst { it == ' ' })
                    name.length >= 5 -> name.substring(0, 7)
                    else -> name
                }
            } ?: ""
            cardNameView.text = issuerName
            cardLast4DigitsText.text = paymentData.payment_option?.card_data?.card_number ?: ""
        }
        successParentLayout.visibility = View.VISIBLE
    }


    private fun handleTimer() {
        val timer = TimerManager
        timer.startTimer(100000)
        timer.timeLeft.observe(viewLifecycleOwner) { timeLeft ->
            if (timeLeft == 0L) {
                ExpressSDKObject.getCallback()?.onSuccess("200", "success", "trt")
                requireActivity().finish()
            }
        }
    }


    override fun onDestroyView() {
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
        super.onDestroyView()
    }

}