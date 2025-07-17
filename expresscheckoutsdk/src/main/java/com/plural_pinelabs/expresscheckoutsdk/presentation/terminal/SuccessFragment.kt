package com.plural_pinelabs.expresscheckoutsdk.presentation.terminal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
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
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.NET_BANKING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAYMENT_REFERENCE_TYPE_CARD
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_ATTEMPTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_CREATED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_PENDING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.WALLET_ID
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
    private lateinit var originalPaidValue: TextView
    private lateinit var cardNameView: TextView
    private lateinit var cardLast4DigitsText: TextView
    private lateinit var paymentIcon: ImageView
    private lateinit var cardsDivider: View
    private lateinit var cardDotsView: ImageView
    private lateinit var continueToMerchantButton: TextView
    private lateinit var reDirectingText: TextView

    private lateinit var viewModel: SuccessViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(
            this, SuccessViewModelFactory(NetworkHelper(requireContext()))
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
        paymentIcon = view.findViewById(R.id.payment_icon)
        cardDotsView = view.findViewById(R.id.card_dots)
        cardsDivider = view.findViewById(R.id.card_divider)
        originalPaidValue = view.findViewById(R.id.original_price_value)
        reDirectingText = view.findViewById(R.id.redirecting_text)
        continueToMerchantButton = view.findViewById(R.id.continue_to_merchant_text)
        continueToMerchantButton.setOnClickListener {
            ExpressSDKObject.getCallback()?.onSuccess(
                "200",
                "success",
                "trt"
            ) // Replace with actual success data if needed
            requireActivity().finish()
        }


        transactionIdValueText.setOnClickListener {
            val textToCopy = transactionIdValueText.text.toString()
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", textToCopy)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        successParentLayout.visibility = View.GONE
        (activity as LandingActivity).showHideHeaderLayout(false)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transactionStatusResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            bottomSheetDialog?.dismiss()
                            findNavController().navigate(R.id.action_successFragment_to_failureFragment)
                        }

                        is BaseResult.Loading -> {
                            if (bottomSheetDialog?.isShowing == false && it.isLoading) bottomSheetDialog =
                                Utils.showProcessPaymentDialog(requireContext())
                        }

                        is BaseResult.Success<TransactionStatusResponse> -> {
                            bottomSheetDialog?.dismiss()
                            val status = it.data.data.status
                            when (status) {
                                PROCESSED_PENDING, PROCESSED_CREATED -> {
                                    findNavController().navigate(R.id.action_successFragment_to_retryFragment)
                                }

                                PROCESSED_STATUS -> {
                                    updateViews(it.data)
                                    handleTimer()
                                }

                                PROCESSED_ATTEMPTED -> {
                                    if (it.data.data.is_retry_available)
                                        findNavController().navigate(R.id.action_successFragment_to_retryFragment)
                                    else
                                        findNavController().navigate(R.id.action_successFragment_to_failureFragment)
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
        val paymentsData = orderSummary?.payments?.filter { it.status == PROCESSED_STATUS }
        val paymentData = paymentsData?.get(0)
        dateTimeValueText.text = formatToReadableDate(orderSummary?.updated_at ?: "")
        orderNumberValueText.text = response.data.order_id
        transactionIdValueText.text =
            orderSummary?.payments?.find { it.status == PROCESSED_STATUS }?.id ?: ""
        if (paymentData != null) {
            cardDotsView.visibility = View.GONE
            cardsDivider.visibility = View.GONE
            amountPaidValue.text =
                Utils.convertToRupeesWithSymobl(requireContext(), paymentData.payment_amount.value)
            originalPaidValue.text =
                Utils.convertToRupeesWithSymobl(requireContext(), paymentData.payment_amount.value)
            if (paymentData.payment_method.equals(PAYMENT_REFERENCE_TYPE_CARD, true)) {
                val issuerName = paymentData.payment_option?.card_data?.issuer_name?.let { name ->
                    when {
                        name.isBlank() -> ""
                        name.contains(" ") -> name.substring(0, name.indexOfFirst { it == ' ' })
                        name.length >= 5 -> name.substring(0, 7)
                        else -> name
                    }
                } ?: ""
                cardNameView.text = issuerName
                cardLast4DigitsText.text = paymentData.payment_option?.card_data?.last4_digit ?: ""
                paymentIcon.setImageResource(R.drawable.ic_cards_colured_icon)
                cardDotsView.visibility = View.VISIBLE
                cardsDivider.visibility = View.VISIBLE
            } else if (paymentData.payment_method.equals(NET_BANKING, true)) {
                paymentIcon.setImageResource(R.drawable.ic_net_banking_coloured)
                cardNameView.text = NET_BANKING
            } else if (paymentData.payment_method.equals(UPI_ID, true)) {
                paymentIcon.setImageResource(R.drawable.ic_upi_tinted)
                cardNameView.text = UPI_ID
            } else if (paymentData.payment_method.equals(WALLET_ID, true)) {
                paymentIcon.setImageResource(R.drawable.ic_wallets_payment_icon)
                cardNameView.text = WALLET_ID
            } else {
                paymentIcon.setImageResource(R.drawable.ic_generic)
                cardNameView.text = paymentData.payment_method
            }


        }
        successParentLayout.visibility = View.VISIBLE
    }


    private fun handleTimer() {
        val timer = TimerManager
        timer.startTimer(10000)
        timer.timeLeft.observe(viewLifecycleOwner) { timeLeft ->
            if (timeLeft == 0L) {
                ExpressSDKObject.getCallback()?.onSuccess("200", "success", "trt")
                requireActivity().finish()
            } else {
                reDirectingText.text = String.format(
                    getString(R.string.redirecting_to_website_in_x_sec),
                    Utils.formatTimeInMinutes(requireContext(), timeLeft)
                )
            }
        }
    }


    override fun onDestroyView() {
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
        super.onDestroyView()
    }

}