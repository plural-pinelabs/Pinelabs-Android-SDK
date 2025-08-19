package com.plural_pinelabs.expresscheckoutsdk.presentation.landing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
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
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardTokenData
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerData
import com.plural_pinelabs.expresscheckoutsdk.data.model.EMIPaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardTokens
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import com.plural_pinelabs.expresscheckoutsdk.presentation.card.CardFragmentViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.offers.OfferSummaryDialog
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration
import kotlinx.coroutines.launch

class PaymentModeFragment : Fragment() {
    private lateinit var savedCardRecyclerView: RecyclerView
    private lateinit var savedCardsHeading: TextView
    private lateinit var paymentModeRecyclerView: RecyclerView
    private lateinit var logoAnimation: LottieAnimationView
    private lateinit var viewModel: CardFragmentViewModel
    private lateinit var addNewCardText: TextView
    private lateinit var savedCardView: CardView
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var offerViewLayout: ConstraintLayout? = null
    private var saveUptoTextView: TextView? = null
    private lateinit var viewOffersBtn: TextView

    private lateinit var contactDeliveryCollapsedLayout: ConstraintLayout
    private lateinit var contactDeliveryExpandedLayout: ConstraintLayout
    private lateinit var contactDetailsTitle: TextView
    private lateinit var contactDetailsValue: TextView
    private lateinit var deliveryDetailsTitle: TextView
    private lateinit var deliveryDetailsValue: TextView
    private lateinit var contactEditIcon: ImageView
    private lateinit var deliveryEditIcon: ImageView
    private lateinit var addresType:TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(
            this,
            CardFragmentViewModelFactory(NetworkHelper(requireContext()))
        )[CardFragmentViewModel::class.java]
        ExpressSDKObject.setSelectedOfferDetail(null)
        return inflater.inflate(R.layout.fragment_payment_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        setContactAndDeliveryDetails()
        initOffersAnimation()
        setPaymentMode()
        setSavedCardsView()
        getMaxSavings()
        addNewCardText.setOnClickListener {
            findNavController().navigate(R.id.action_paymentModeFragment_to_cardFragment)
        }
        viewOffersBtn.setOnClickListener {
            showOffers()
        }
        (requireActivity() as LandingActivity).showHideConvenienceFessMessage(ExpressSDKObject.getFetchData()?.convenienceFeesInfo?.isEmpty() == false)
    }

    private fun setContactAndDeliveryDetails() {
        contactDeliveryCollapsedLayout.visibility = View.VISIBLE
        contactDeliveryExpandedLayout.visibility = View.GONE
        val a: String? = ExpressSDKObject.getFetchData()?.customerInfo?.mobileNo
        val b: String? = ExpressSDKObject.getFetchData()?.customerInfo?.emailId

        val result = listOfNotNull(a, b).joinToString(" | ")

        contactDetailsValue.text = result
        val address = ExpressSDKObject.getSelectedAddress()?: ExpressSDKObject.getFetchData()?.customerInfo?.shippingAddress?: ExpressSDKObject.getFetchData()?.customerInfo?.shipping_address
        if (address == null||address.address1.isNullOrEmpty()) {
            deliveryDetailsTitle.visibility=View.GONE
            deliveryDetailsValue.visibility=View.GONE
            deliveryEditIcon.visibility=View.GONE
        }
        val deliveryAddress = listOfNotNull(address?.full_name,address?.address1,address?.address2, address?.city, address?.state, address?.country, address?.pincode).joinToString(", ")
        deliveryDetailsValue.text = deliveryAddress
        addresType.text = address?.address_type ?: getString(R.string.home)

        contactEditIcon.setOnClickListener {
            findNavController().navigate(R.id.action_paymentModeFragment_to_phoneNumberFragment)
        }
        deliveryEditIcon.setOnClickListener {
            findNavController().navigate(R.id.action_paymentModeFragment_to_savedAddressFragment)
        }
        contactDeliveryCollapsedLayout.setOnClickListener {
            contactDeliveryCollapsedLayout.visibility = View.GONE
            contactDeliveryExpandedLayout.visibility = View.VISIBLE
        }
    }

    private fun setViews(view: View) {
        paymentModeRecyclerView = view.findViewById(R.id.payment_option_list)
        savedCardRecyclerView = view.findViewById(R.id.saved_cards_list)
        savedCardsHeading = view.findViewById(R.id.saved_cards_title)
        logoAnimation = view.findViewById(R.id.offers_gif)
        addNewCardText = view.findViewById(R.id.add_new_card_btn)
        savedCardView = view.findViewById(R.id.saved_cards_card_view)
        offerViewLayout = view.findViewById(R.id.offers_parent_layout)
        saveUptoTextView = view.findViewById(R.id.save_upto_text)
        viewOffersBtn = view.findViewById(R.id.view_offers_btn)

        contactDeliveryCollapsedLayout =
            view.findViewById(R.id.contact_and_delivery_details_collapsed_layout)
        contactDeliveryExpandedLayout =
            view.findViewById(R.id.contact_and_delivery_details_expanded_layout)
        contactDetailsTitle = view.findViewById(R.id.contact_details_title)
        contactDetailsValue = view.findViewById(R.id.contact_details)
        deliveryDetailsTitle = view.findViewById(R.id.delivery_details_title)
        deliveryDetailsValue = view.findViewById(R.id.delivery_details)
        contactEditIcon = view.findViewById(R.id.edit_contact_icon)
        deliveryEditIcon = view.findViewById(R.id.edit_delivery_icon)
        addresType = view.findViewById(R.id.address_type)
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
            savedCardView.visibility = View.GONE
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

            val isCard = it.paymentModeId == PaymentModes.CREDIT_DEBIT.paymentModeID
            val hasToken = !ExpressSDKObject.getFetchData()?.customerInfo?.tokens.isNullOrEmpty()
            it.paymentModeData != null && availablePaymentModes.contains(it.paymentModeId.lowercase()) &&
                    !(isCard && hasToken)
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

    private fun getMaxSavings() {
        processDataForEMI()
        val emiPaymentData = ExpressSDKObject.getEMIPaymentModeData()
        if (emiPaymentData == null) {
            //Hide the offer views
            offerViewLayout?.visibility = View.GONE
            return
        }
        val maxSavings = emiPaymentData.offerDetails?.firstOrNull()?.maxSaving
        saveUptoTextView?.text = getString(
            R.string.save_up_to,
            Utils.convertToRupeesWithSymobl(requireContext(), maxSavings ?: 0)
        )

    }

    private fun processDataForEMI() {
        val data = ExpressSDKObject.getFetchData()
        data?.paymentModes?.filter { paymentMode -> paymentMode.paymentModeId == PaymentModes.EMI.paymentModeID }
            ?.forEach { paymentMode ->
                when (val pm = paymentMode.paymentModeData) {
                    is LinkedTreeMap<*, *> -> {
                        val emiPaymentModeData = convertMapToJsonObject(pm)
                        ExpressSDKObject.setEMIPaymentModeData(emiPaymentModeData)
                    }
                }
            }
    }

    private fun convertMapToJsonObject(yourMap: Map<*, *>): EMIPaymentModeData {
        val gson = Gson().toJsonTree(yourMap).asJsonObject
        return Gson().fromJson(gson.toString(), EMIPaymentModeData::class.java)
    }

    private fun showOffers() {
        val topFragment = OfferSummaryDialog()
        topFragment.show(requireActivity().supportFragmentManager, "TopSheetDialogFragment")

    }

}
