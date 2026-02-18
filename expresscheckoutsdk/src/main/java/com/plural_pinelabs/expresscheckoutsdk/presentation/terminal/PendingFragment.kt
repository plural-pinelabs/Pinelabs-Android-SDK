package com.plural_pinelabs.expresscheckoutsdk.presentation.terminal


import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAY_BY_POINTS_ID
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.TimerManager
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardTokens
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import com.plural_pinelabs.expresscheckoutsdk.presentation.landing.PaymentModeRecyclerViewAdapter
import com.plural_pinelabs.expresscheckoutsdk.presentation.landing.SavedCardRecyclerViewAdapter
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration

class PendingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        ExpressSDKObject.setSelectedOfferDetail(null)
        return inflater.inflate(R.layout.fragment_pending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as LandingActivity).showHideHeaderLayout(true)
        (requireActivity() as LandingActivity).showHideConvenienceFessMessage(ExpressSDKObject.getFetchData()?.convenienceFeesInfo?.isEmpty() == false)
        val timer = TimerManager
        timer.startTimer(10000)

        timer.timeLeft.observe(viewLifecycleOwner, { timeLeft ->
            if (timeLeft == 0L) {
                handleClosingSDK(false)
            } else {
                val autoCloseTv = view.findViewById<TextView>(R.id.txt_autoclose)
                val autoCloseString = getString(
                    R.string.auto_close,
                    Utils.formatTimeInMinutes(requireContext(), timeLeft)
                )
                autoCloseTv.text = Html.fromHtml(autoCloseString)
            }
        })

        val timing = view.findViewById<TextView>(R.id.timing)
        val time = ExpressSDKObject.getCreatedAt()
        val formattedTime = time?.let {
            try {
                val instant = java.time.Instant.parse(it)
                val dateTime = java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
                formatter.format(dateTime)
            } catch (e: Exception) {
                ""
            }
        } ?: ""
        val timingString = getString(R.string._9_28_am_upi_net_banking, formattedTime)
        timing.text = timingString


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
                "1003",
                "Failure for Pending Transaction",
                message,
                ExpressSDKObject.getFetchData()?.transactionInfo?.orderId
            )
        }// Replace with actual success data if needed
        requireActivity().finish()
    }






}