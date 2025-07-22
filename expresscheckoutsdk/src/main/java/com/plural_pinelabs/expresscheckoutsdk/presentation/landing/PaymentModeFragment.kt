package com.plural_pinelabs.expresscheckoutsdk.presentation.landing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.CardFragmentViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.Constants
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_MESSAGE_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAY_BY_POINTS_ID
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardTokenData
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardTokens
import com.plural_pinelabs.expresscheckoutsdk.presentation.card.CardFragmentViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration
import kotlinx.coroutines.launch

class PaymentModeFragment : Fragment() {
    private lateinit var savedCardRecyclerView: RecyclerView
    private lateinit var savedCardsHeading: TextView
    private lateinit var paymentModeRecyclerView: RecyclerView
    private lateinit var logoAnimation: LottieAnimationView
    private lateinit var viewModel: CardFragmentViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(
            this,
            CardFragmentViewModelFactory(NetworkHelper(requireContext()))
        )[CardFragmentViewModel::class.java]
        return inflater.inflate(R.layout.fragment_payment_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        paymentModeRecyclerView = view.findViewById(R.id.payment_option_list)
        savedCardRecyclerView = view.findViewById(R.id.saved_cards_list)
        savedCardsHeading = view.findViewById(R.id.saved_cards_title)
        logoAnimation = view.findViewById(R.id.offers_gif)
        initOffersAnimation()
        setPaymentMode()
        setSavedCardsView()
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
                observeViewModel()
                val createProcessPaymentRequest = createProcessPaymentRequest(item)
                viewModel.processPayment(
                    token = ExpressSDKObject.getToken(),
                    paymentData = createProcessPaymentRequest
                )
            }
        }
    }

    private fun createProcessPaymentRequest(savedCardTokens: SavedCardTokens): ProcessPaymentRequest {
        val paymentData = ExpressSDKObject.getFetchData()?.paymentData
        if (paymentData == null) {
            findNavController().navigate(R.id.action_paymentModeFragment_to_successFragment)
        }
        val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
        val amount = paymentData?.originalTxnAmount?.amount
        val currency = paymentData?.originalTxnAmount?.currency
        val cardTokenData = CardTokenData(savedCardTokens.tokenId, savedCardTokens.cvvInput)
        val customerInfoData = CustomerData(
            emailId = customerInfo?.emailId ?: "",
            mobileNo = customerInfo?.mobileNumber ?: customerInfo?.mobileNo ?: ""
        )
        val paymentMode = arrayListOf<String>()
        paymentMode.add(Constants.CREDIT_DEBIT_ID)

        val cardDataExtra =
            Extra(
                paymentMode,
                amount,
                currency,
                null,
                null,
                null,
                null,
                null,
                null,
                null,// dccstatus pass this from dcc api call
                null
            )

        val processPaymentRequest =
            ProcessPaymentRequest(
                cardTokenData,
                customerInfoData,
                null,
                upi_data = null,
                null,
                null,
                cardDataExtra,
                null,
                null
            )
        return processPaymentRequest
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
                    getPaymentModeSelectionCallback(requireContext())
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

    private fun getPaymentModeSelectionCallback(context: Context): ItemClickListener<PaymentMode>? {
        return object : ItemClickListener<PaymentMode> {
            override fun onItemClick(position: Int, item: PaymentMode) {
                when (item.paymentModeId) {
                    PaymentModes.CREDIT_DEBIT.paymentModeID -> {
                        findNavController().navigate(R.id.action_paymentModeFragment_to_cardFragment)
                    }

                    PaymentModes.UPI.paymentModeID -> {
                        findNavController().navigate(R.id.action_paymentModeFragment_to_UPIFragment)
                    }

                    PaymentModes.NET_BANKING.paymentModeID -> {
                        // Handle Netbanking selection
                        findNavController().navigate(R.id.action_paymentModeFragment_to_netBankingFragment)
                    }

                    PaymentModes.WALLET.paymentModeID -> {
                        // Handle Wallet selection
                        findNavController().navigate(R.id.action_paymentModeFragment_to_walletFragment)
                    }

                    PaymentModes.EMI.paymentModeID -> {
                        // Handle EMI selection
                        findNavController().navigate(R.id.action_paymentModeFragment_to_EMIFragment)
                    }

                    else -> {
                        // Handle other selections
                    }
                }

            }
        }
    }

    private fun initOffersAnimation() {
        logoAnimation.setAnimation(R.raw.offers)
        logoAnimation.playAnimation()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.processPaymentResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            val bundle = Bundle()
                            bundle.putString(ERROR_KEY, it.errorCode)
                            bundle.putString(ERROR_MESSAGE_KEY, it.errorMessage)
                            bottomSheetDialog?.dismiss()
                            findNavController().navigate(R.id.action_paymentModeFragment_to_successFragment)
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading)
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())
                        }

                        is BaseResult.Success<ProcessPaymentResponse> -> {
                            ExpressSDKObject.setProcessPaymentResponse(it.data)
                            bottomSheetDialog?.dismiss()
                            findNavController().navigate(R.id.action_paymentModeFragment_to_ACSFragment)

                        }
                    }
                }
            }
        }
    }
}
