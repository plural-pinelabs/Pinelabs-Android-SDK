package com.plural_pinelabs.expresscheckoutsdk.presentation.terminal

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.CleverTapUtil
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.RetrofitBuilder
import com.plural_pinelabs.expresscheckoutsdk.logger.SdkLogger
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class CancelReasonsFragment : Fragment() {

    private lateinit var adapter: CancelReasonsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_cancel_reasons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contentScroll = view.findViewWithTag<NestedScrollView>("cancel_reasons_content_scroll")
        val reasonsRv = view.findViewById<RecyclerView>(R.id.reasons_rv)
        val otherInput = view.findViewById<EditText>(R.id.other_input)
        val backBtn = view.findViewById<View>(R.id.back_btn)
        val confirmCancelBtn = view.findViewById<Button>(R.id.confirm_cancel_btn)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.popBackStack()
                }
            }
        )

        val reasons = listOf(
            CancelReasonItem("taking_too_long", getString(R.string.cancel_reason_taking_too_long)),
            CancelReasonItem("wrong_payment_method", getString(R.string.cancel_reason_wrong_payment_method)),
            CancelReasonItem("order_changed", getString(R.string.cancel_reason_order_changed)),
            CancelReasonItem("technical_issue", getString(R.string.cancel_reason_technical_issue)),
            CancelReasonItem("other", getString(R.string.other), isOther = true)
        )

        adapter = CancelReasonsAdapter(reasons) { selected ->
            val showOther = selected?.isOther == true
            otherInput.isVisible = showOther
            if (!showOther) {
                otherInput.text?.clear()
            } else {
                otherInput.post {
                    otherInput.requestFocus()
                    contentScroll.smoothScrollTo(0, otherInput.bottom)
                }
            }
            updateConfirmButtonText(confirmCancelBtn, selected, otherInput.text?.toString().orEmpty())
        }

        reasonsRv.adapter = adapter
        if (reasonsRv.itemDecorationCount == 0) {
            reasonsRv.addItemDecoration(
                DividerItemDecoration(
                    ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_divider)
                )
            )
        }
        otherInput.isVisible = false
        updateConfirmButtonText(confirmCancelBtn, adapter.getSelected(), otherInput.text?.toString().orEmpty())
        otherInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                updateConfirmButtonText(confirmCancelBtn, adapter.getSelected(), s?.toString().orEmpty())
                if (otherInput.isVisible) {
                    otherInput.post { contentScroll.smoothScrollTo(0, otherInput.bottom) }
                }
            }
        })

        backBtn.setOnClickListener { parentFragmentManager.popBackStack() }
        confirmCancelBtn.setOnClickListener {
            val selectedReason = adapter.getSelected()
            val otherText = otherInput.text?.toString().orEmpty().trim()
            confirmCancelBtn.isEnabled = false
            cancelPayment(
                confirmCancelBtn = confirmCancelBtn,
                selectedReasonId = selectedReason?.id,
                otherText = if (selectedReason?.isOther == true) otherText else ""
            )
        }
    }

    private fun updateConfirmButtonText(
        confirmCancelBtn: Button,
        selectedReason: CancelReasonItem?,
        otherText: String
    ) {
        val buttonTextRes = if (selectedReason == null || (selectedReason.isOther && otherText.trim().isEmpty())) {
            R.string.skip_cancel
        } else {
            R.string.submit_cancel
        }
        confirmCancelBtn.setText(buttonTextRes)
    }

    private fun cancelPayment(
        confirmCancelBtn: Button,
        selectedReasonId: String?,
        otherText: String
    ) {
        if (!selectedReasonId.isNullOrBlank() || otherText.isNotBlank()) {
            Log.i(
                "ExpressLibrary",
                "Cancelling payment with reason=$selectedReasonId, otherText=$otherText"
            )
        }

        val appContext = requireContext().applicationContext

        try {
            runBlocking {
                withTimeout(3000) {
                    val repo = ExpressRepositoryImpl(
                        RetrofitBuilder.commonApiService,
                        NetworkHelper(appContext)
                    )
                    val result = repo.cancelPayment(
                        ExpressSDKObject.getToken(),
                        ExpressSDKObject.getProcessPaymentResponse() != null
                    )
                    result.collect {
                        when (it) {
                            is BaseResult.Success -> {
                            }

                            is BaseResult.Error -> {
                                Log.e(
                                    "ExpressLibrary",
                                    "Failed to cancel payment: ${it.errorDescription}"
                                )
                            }

                            is BaseResult.Loading -> {
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.i("PineLabs error", "Error cancelling the transaction", e)
        } finally {
            SdkLogger.log(
                requireActivity(),
                "PAYMENT_CANCELLED",
                "Payment cancelled by user",
                ExpressSDKObject.getFetchData()?.transactionInfo?.orderId ?: "",
                "INFO",
                "SDK"
            )
            CleverTapUtil.sdkTransactionAbandoned(
                CleverTapUtil.getInstance(appContext),
                ExpressSDKObject.getFetchData(),
                System.currentTimeMillis().toString(),
                "",
                "",
                Utils.createSDKData(appContext).toString(),
                ""
            )

            try {
                runBlocking {
                    withTimeout(3000) {
                        val repo = ExpressRepositoryImpl(
                            RetrofitBuilder.fetchApiService,
                            NetworkHelper(appContext)
                        )
                        val logs = Utils.getUnSyncedErrors(appContext)
                        val result = repo.logData(ExpressSDKObject.getToken(), logs)
                        result.collect {
                            when (it) {
                                is BaseResult.Success -> {
                                    if (it.data.status.equals("success", ignoreCase = true)) {
                                        Utils.clearLogs(appContext)
                                    }
                                    Log.i("ExpressLibrary", "Crash logs reported successfully")
                                }

                                is BaseResult.Error -> {
                                    Log.e(
                                        "ExpressLibrary",
                                        "Failed to report crash logs: ${it.errorDescription}"
                                    )
                                }

                                is BaseResult.Loading -> {
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ExpressLibrary", "Failed to report crash", e)
            } finally {
                confirmCancelBtn.isEnabled = true
                val bundle = Bundle().apply {
                    putBoolean("isCancelled", true)
                }
                findNavController().navigate(R.id.failureFragment, bundle)
            }
        }
    }
}