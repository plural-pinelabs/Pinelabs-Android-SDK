package com.plural_pinelabs.expresscheckoutsdk.presentation.terminal

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.CleverTapUtil
import com.plural_pinelabs.expresscheckoutsdk.common.TimerManager
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.MTAG


class FailureFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_failure, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val timer = TimerManager
        val isCancelled = arguments?.getBoolean("isCancelled") ?: false

        Log.i(MTAG, "inside failure fragment")

        CleverTapUtil.sdkTransactionFailed(
            CleverTapUtil.getInstance(requireContext()),
            ExpressSDKObject.getFetchData(),
            "",
            Utils.getCartValue(),
            "failure",
            "1000",
            "Step",
            false,
            Utils.createSDKData(requireContext()).toString(),
        )

        view.findViewById<Button>(R.id.continue_btn).setOnClickListener {
            timer.stopTimer()
            handleClosingSDK(isCancelled)
        }
        timer.startTimer(5000)
        timer.timeLeft.observe(viewLifecycleOwner, { timeLeft ->
            if (timeLeft == 0L) {
                handleClosingSDK(isCancelled)
            } else {
                val autoCloseTv = view.findViewById<TextView>(R.id.txt_autoclose)
                val autoCloseString = getString(
                    R.string.auto_close,
                    Utils.formatTimeInMinutes(requireContext(), timeLeft)
                )
                autoCloseTv.text = Html.fromHtml(autoCloseString)
            }
        })
    }

    private fun handleClosingSDK(isCancelled: Boolean) {
        val message =
            if (isCancelled) "Transaction Cancelled by User" else "Transaction Failed"
        if (isCancelled) {
            ExpressSDKObject.getCallback()?.onCancel(
                "1001",
                "Cancelled",
                message,
                ExpressSDKObject.getFetchData()?.transactionInfo?.orderId
            )
        } else {
            ExpressSDKObject.getCallback()?.onError(
                "1000",
                "Failure",
                message,
                ExpressSDKObject.getFetchData()?.transactionInfo?.orderId
            )
        }// Replace with actual success data if needed
        requireActivity().finish()
        requireActivity().finish()
    }

}