package com.plural_pinelabs.expresscheckoutsdk.presentation.terminal

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        Log.i(MTAG, "inside failure fragment")
        timer.startTimer(5000)
        timer.timeLeft.observe(viewLifecycleOwner, { timeLeft ->
            if (timeLeft == 0L) {
                ExpressSDKObject.getCallback()?.onError(
                    "1000",
                    "Failure",
                    "Some error occurred"
                ) // Replace with actual success data if needed
                requireActivity().finish()
                requireActivity().finish() // Close the activity or navigate to another screen
                // Handle timer finish, e.g., navigate to another fragment or activity
                // For example: findNavController().navigate(R.id.action_failureFragment_to_nextFragment)
            } else {
                val autoCloseTv = view.findViewById<TextView>(R.id.txt_autoclose)
                val autoCloseString = getString(
                    R.string.auto_close,
                    Utils.formatTimeInMinutes(requireContext(), timeLeft)
                )
                autoCloseTv.text = Html.fromHtml(autoCloseString)
            }
        })

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

    }

}