package com.plural_pinelabs.expresscheckoutsdk.presentation.terminal.retry

import android.os.Bundle
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
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardTokens
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import com.plural_pinelabs.expresscheckoutsdk.presentation.landing.PaymentModeRecyclerViewAdapter
import com.plural_pinelabs.expresscheckoutsdk.presentation.landing.SavedCardRecyclerViewAdapter
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration

class RetryFragment : Fragment() {
    private lateinit var retryPaymentValueText: TextView
    private lateinit var savedCardRecyclerView: RecyclerView
    private lateinit var savedCardsHeading: TextView
    private lateinit var paymentModeRecyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        ExpressSDKObject.setSelectedOfferDetail(null)
        return inflater.inflate(R.layout.fragment_retry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as LandingActivity).showHideHeaderLayout(true)
        setupViews(view)
        setPaymentMode()
        setSavedCardsView()
        (requireActivity() as LandingActivity).showHideConvenienceFessMessage(ExpressSDKObject.getFetchData()?.convenienceFeesInfo?.isEmpty() == false)
    }

    private fun setupViews(view: View) {
        paymentModeRecyclerView = view.findViewById(R.id.payment_option_list)
        savedCardRecyclerView = view.findViewById(R.id.saved_cards_list)
        savedCardsHeading = view.findViewById(R.id.saved_cards_title)
        retryPaymentValueText = view.findViewById(R.id.retry_payment_amount)
        retryPaymentValueText.text = String.format(
            getString(
                R.string.retry_payment_of_x,
            ),
            Utils.convertToRupeesWithSymobl(requireContext(), ExpressSDKObject.getAmount())
        )
    }

    private fun setSavedCardsView() {
        savedCardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        savedCardRecyclerView.addItemDecoration(
            DividerItemDecoration(
                ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_divider)
            )
        )
        val savedCards = ExpressSDKObject.getFetchData()?.customerInfo?.tokens
        if (savedCards.isNullOrEmpty()) {
            savedCardsHeading.visibility = View.GONE
            savedCardRecyclerView.visibility = View.GONE
        } else {
            savedCardsHeading.visibility = View.VISIBLE
            savedCardRecyclerView.visibility = View.VISIBLE
            val adapter =
                SavedCardRecyclerViewAdapter(requireContext(), savedCards, getSavedCardCallback())
            savedCardRecyclerView.adapter = adapter
        }
    }

    private fun getSavedCardCallback(): ItemClickListener<SavedCardTokens> {
        return object : ItemClickListener<SavedCardTokens> {
            override fun onItemClick(position: Int, item: SavedCardTokens) {
                // Handle saved card selection
                findNavController().navigate(R.id.action_retryFragment_to_cardFragment)
            }
        }
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
            availablePaymentModes.contains(it.paymentModeId.lowercase()) && it.paymentModeData != null
        }
        return filteredPaymentModes
    }

    private fun isPBPEnabled(paymentModes: List<PaymentMode>): Boolean {
        return paymentModes.any { it.paymentModeId == PAY_BY_POINTS_ID }
    }

    private fun getPaymentModeSelectionCallback(): ItemClickListener<PaymentMode> {
        return object : ItemClickListener<PaymentMode> {
            override fun onItemClick(position: Int, item: PaymentMode) {
                when (item.paymentModeId) {
                    PaymentModes.CREDIT_DEBIT.paymentModeID -> {
                        findNavController().navigate(R.id.action_retryFragment_to_cardFragment)
                    }

                    PaymentModes.UPI.paymentModeID -> {
                        findNavController().navigate(R.id.action_retryFragment_to_upiFragment)
                    }

                    PaymentModes.NET_BANKING.paymentModeID -> {
                        // Handle Netbanking selection
                        findNavController().navigate(R.id.action_retryFragment_to_netBankingFragment)
                    }

                    PaymentModes.WALLET.paymentModeID -> {
                        // Handle Wallet selection
                        findNavController().navigate(R.id.action_retryFragment_to_walletFragment)
                    }

                    PaymentModes.EMI.paymentModeID -> {
                        // Handle EMI selection
                        findNavController().navigate(R.id.action_retryFragment_to_EMIFragment)
                    }

                    else -> {
                        // Handle other selections
                    }
                }

            }
        }
    }


}