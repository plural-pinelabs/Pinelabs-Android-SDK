package com.plural_pinelabs.expresscheckoutsdk.presentation.landing

import BankColors
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject.getAmount
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject.getCurrency
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.CardFragmentViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.CleverTapUtil
import com.plural_pinelabs.expresscheckoutsdk.common.Constants
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.AXIS_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EMI_DC_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_MESSAGE_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.HDFC_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ICICI_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.INDIAN_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.INDUSIND_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ISSUE_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KOTAK_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MAHARASHTRA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAY_BY_POINTS_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SBI_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.STANDARD_CHARTERED_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TENURE_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.YES_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModeViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentDialog
import com.plural_pinelabs.expresscheckoutsdk.common.safeNavigate
import com.plural_pinelabs.expresscheckoutsdk.data.model.BrandWalletBalance
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardTokenData
import com.plural_pinelabs.expresscheckoutsdk.data.model.CreateWalletRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.CreateWalletResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerData
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.EMIPaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetail
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentOptions
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardTokens
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiData
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiTransactionData
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletDetails
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletAddMoneyRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletAddMoneyResponse
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import com.plural_pinelabs.expresscheckoutsdk.presentation.card.CardFragmentViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.offers.OfferSummaryDialog
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class PaymentModeFragment : Fragment() {
    private enum class BrandWalletVerificationSheetState {
        EMAIL,
        OTP,
    }

    private lateinit var savedCardRecyclerView: RecyclerView
    private lateinit var savedCardsHeading: TextView
    private lateinit var paymentModeRecyclerView: RecyclerView
    private lateinit var logoAnimation: LottieAnimationView
    private lateinit var viewModel: CardFragmentViewModel
    private lateinit var paymentModeViewModel: PaymentModeViewModel
    private lateinit var addNewCardText: TextView
    private lateinit var savedCardView: CardView
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var offerViewLayout: ConstraintLayout? = null
    private var saveUptoTextView: TextView? = null
    private lateinit var viewOffersBtn: TextView

    private lateinit var bankLogoMap: HashMap<String, String>
    private lateinit var banKTitleToCodeMap: HashMap<String, String>
    private lateinit var bankNameKeyList: List<String>

    private lateinit var contactDeliveryCollapsedLayout: ConstraintLayout
    private lateinit var contactDeliveryExpandedLayout: ConstraintLayout
    private lateinit var contactDetailsTitle: TextView
    private lateinit var contactDetailsValue: TextView
    private lateinit var deliveryDetailsTitle: TextView
    private lateinit var deliveryDetailsValue: TextView
    private lateinit var contactEditIcon: ImageView
    private lateinit var deliveryEditIcon: ImageView
    private lateinit var addresType: TextView

    private lateinit var recommendedOptionLabel: TextView
    private lateinit var recommendedParentLayout: ConstraintLayout
    private lateinit var recommendedSavedCardParentLayout: LinearLayout
    private lateinit var recommendedUPIVPAParentLayout: LinearLayout
    private lateinit var recommendedUPIVPATextview: TextView
    private lateinit var bankLogoName: ImageView
    private lateinit var offerType: TextView
    private lateinit var emiDiscount: TextView
    private lateinit var bankOffer: TextView
    private lateinit var perMonthEmi: TextView
    private lateinit var emiDuration: TextView
    private lateinit var totalPayable: TextView
    private lateinit var actionBtn: TextView
    private lateinit var upiVPACheck: CheckBox
    private lateinit var payByUPIVPABtn: TextView

    private lateinit var recommendedSavedCardIcon: ImageView
    private lateinit var recommendedSavedCardText: TextView
    private lateinit var recommendedSavedCardLast4: TextView
    private lateinit var recommendedSavedCardCheckBox: CheckBox
    private lateinit var paymentOptionCard: CardView
    private lateinit var brandWalletCard: CardView
    private lateinit var brandWalletIcon: ImageView
    private lateinit var brandWalletTitle: TextView
    private lateinit var brandWalletBalanceRow: LinearLayout
    private lateinit var brandWalletBalance: TextView
    private lateinit var brandWalletBalanceRefreshIcon: ImageView
    private lateinit var brandWalletDescription: TextView
    private lateinit var brandWalletPrimaryAction: TextView
    private lateinit var brandWalletSecondaryAction: TextView
    private lateinit var brandWalletProceedAction: TextView
    private lateinit var brandWalletActivateCtaRow: LinearLayout
    private lateinit var brandWalletActionsRow: LinearLayout
    private lateinit var brandWalletSelectedIcon: ImageView
    private var brandWalletBottomSheetDialog: BottomSheetDialog? = null
    private var brandWalletReadyDismissJob: Job? = null
    private var brandWalletOtpCountdownJob: Job? = null
    private var brandWalletRedeemProgressJob: Job? = null
    private var brandWalletRedeemMockResultJob: Job? = null
    private var isBrandWalletActivatedCardToggled: Boolean = false
    private var isBrandWalletCardSelected: Boolean = false
    private var brandWalletVerificationEmail: String? = null
    private var isBrandWalletOtpTriggerProcessPayment: Boolean = false
    private var isBrandWalletProceedPaymentInFlight: Boolean = false
    private var isBrandWalletProceedOtpFlow: Boolean = false
    private var brandWalletOtpPaymentId: String? = null
    private var hasObservedPaymentResult = false

    private companion object {
        const val BRAND_WALLET_PIN_LENGTH = 6
        const val BRAND_WALLET_PIN_RESEND_SECONDS = 120
        const val BRAND_WALLET_REDEEM_TIMEOUT_MS = 60_000L
        const val BRAND_WALLET_REDEEM_MOCK_RESULT_DELAY_MS = 3_000L
        const val BRAND_WALLET_REDEEM_PROGRESS_INTERVAL_MS = 250L
        const val BRAND_WALLET_TERMS_SHEET_HEIGHT_RATIO = 0.82f
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(
            this,
            CardFragmentViewModelFactory(NetworkHelper(requireContext()))
        )[CardFragmentViewModel::class.java]
        paymentModeViewModel = ViewModelProvider(
            this,
            PaymentModeViewModelFactory(NetworkHelper(requireContext()))
        )[PaymentModeViewModel::class.java]
        ExpressSDKObject.setSelectedOfferDetail(null)
        return inflater.inflate(R.layout.fragment_payment_mode, container, false)
    }

    override fun onDestroyView() {
        brandWalletReadyDismissJob?.cancel()
        brandWalletReadyDismissJob = null
        brandWalletOtpCountdownJob?.cancel()
        brandWalletOtpCountdownJob = null
        cancelBrandWalletRedeemJobs()
        brandWalletBottomSheetDialog?.dismiss()
        brandWalletBottomSheetDialog = null
        isBrandWalletOtpTriggerProcessPayment = false
        isBrandWalletProceedPaymentInFlight = false
        isBrandWalletProceedOtpFlow = false
        brandWalletOtpPaymentId = null
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Intentionally block back navigation on payment mode.
                }
            }
        )
        setViews(view)
        mapBanKLogo()
        getMaxSavings()
        handleRecommendationOptions()

        setContactAndDeliveryDetails()
        initOffersAnimation()
        setPaymentMode()
        setSavedCardsView()
        observeViewModel()
        addNewCardText.setOnClickListener {
            safeNavigate(R.id.action_paymentModeFragment_to_cardFragment)
        }
        viewOffersBtn.setOnClickListener {
            showOffers()
        }
        observeCreateWalletResult()
        (requireActivity() as LandingActivity).showHideConvenienceFessMessage(ExpressSDKObject.getFetchData()?.convenienceFeesInfo?.isEmpty() == false)
        CleverTapUtil.sdkPaymentModeView(
            CleverTapUtil.getInstance(requireContext()),
            ExpressSDKObject.getFetchData(),
            getPaymentModeArray(),
            ExpressSDKObject.getFetchData()?.customerInfo?.lastUsedPaymode?.lastTransactionPaymentMode
                ?: "",
            false
        )
    }

    private fun getPaymentModeArray(): String {
        val paymentModes = getPaymentModes()
        val paymentModeArray = arrayListOf<String>()
        paymentModes?.forEach {
            paymentModeArray.add(it.paymentModeId)
        }
        getBrandWalletPaymentMode()?.paymentModeId?.let { brandWalletId ->
            if (!paymentModeArray.contains(brandWalletId)) {
                paymentModeArray.add(brandWalletId)
            }
        }
        val joinedString = paymentModeArray.joinToString(
            separator = ", ",
            prefix = "",
            postfix = ""
        )

        return joinedString
    }

    private fun setContactAndDeliveryDetails() {
        contactDeliveryCollapsedLayout.visibility = View.VISIBLE
        contactDeliveryExpandedLayout.visibility = View.GONE
        val a: String? = ExpressSDKObject.getFetchData()?.customerInfo?.mobileNo
        val b: String? = ExpressSDKObject.getFetchData()?.customerInfo?.emailId

        val result = listOfNotNull(a, b).joinToString(" | ")

        contactDetailsValue.text = result
        val address = ExpressSDKObject.getSelectedAddress()
            ?: ExpressSDKObject.getFetchData()?.customerInfo?.shippingAddress
            ?: ExpressSDKObject.getFetchData()?.customerInfo?.shipping_address
        if (address == null || address.address1.isNullOrEmpty()) {
            deliveryDetailsTitle.visibility = View.GONE
            deliveryDetailsValue.visibility = View.GONE
            deliveryEditIcon.visibility = View.GONE
        }

        if (ExpressSDKObject.getFetchData()?.merchantMetadata?.express_checkout_allowed_action?.contains(
                "checkoutCollectAddress"
            ) == false
        ) {
            deliveryEditIcon.visibility = View.GONE
        }

        if (ExpressSDKObject.getFetchData()?.merchantMetadata?.express_checkout_allowed_action?.contains(
                "checkoutCollectMobile"
            ) == false
        ) {
            contactEditIcon.visibility = View.GONE
        }

        val deliveryAddress = listOfNotNull(
            address?.full_name,
            address?.address1,
            address?.address2,
            address?.city,
            address?.state,
            address?.country,
            address?.pincode
        ).joinToString(", ")
        deliveryDetailsValue.text = deliveryAddress
        addresType.text = address?.address_type ?: getString(R.string.home)

        contactEditIcon.setOnClickListener {
            safeNavigate(R.id.action_paymentModeFragment_to_phoneNumberFragment)
        }
        deliveryEditIcon.setOnClickListener {
            safeNavigate(R.id.action_paymentModeFragment_to_savedAddressFragment)
        }
        contactDeliveryCollapsedLayout.setOnClickListener {
            contactDeliveryCollapsedLayout.visibility = View.GONE
            contactDeliveryExpandedLayout.visibility = View.VISIBLE
        }
    }

    private fun setViews(view: View) {
        paymentModeRecyclerView = view.findViewById(R.id.payment_option_list)
        paymentOptionCard = view.findViewById(R.id.payment_option_card)
        savedCardRecyclerView = view.findViewById(R.id.saved_cards_list)
        savedCardsHeading = view.findViewById(R.id.saved_cards_title)
        logoAnimation = view.findViewById(R.id.offers_gif)
        addNewCardText = view.findViewById(R.id.add_new_card_btn)
        savedCardView = view.findViewById(R.id.saved_cards_card_view)
        offerViewLayout = view.findViewById(R.id.offers_parent_layout)
        saveUptoTextView = view.findViewById(R.id.save_upto_text)
        viewOffersBtn = view.findViewById(R.id.view_offers_btn)

        recommendedOptionLabel = view.findViewById(R.id.recommended_option_title)
        recommendedSavedCardParentLayout = view.findViewById(R.id.saved_card_cvv_parent_layout)
        recommendedUPIVPAParentLayout = view.findViewById(R.id.upi_vpa_parent_layout)
        recommendedParentLayout = view.findViewById(R.id.parent_recommended_card_item_cl)
        bankLogoName = view.findViewById(R.id.bank_logo)
        offerType = view.findViewById(R.id.offer_type)
        emiDiscount = view.findViewById(R.id.emi_discount_value)
        bankOffer = view.findViewById(R.id.bank_offer_value)
        totalPayable = view.findViewById(R.id.total_payable_value)
        perMonthEmi = view.findViewById(R.id.emi_per_month_value)
        emiDuration = view.findViewById(R.id.emi_per_x_month)
        actionBtn = view.findViewById(R.id.action_btn)
        recommendedUPIVPATextview = view.findViewById(R.id.upi_vpa)
        upiVPACheck = view.findViewById(R.id.upi_vpa_check)
        payByUPIVPABtn = view.findViewById(R.id.pay_by_upi_vpa)

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

        recommendedSavedCardIcon = view.findViewById(R.id.card_icon)
        recommendedSavedCardText = view.findViewById(R.id.card_issuer_name)
        recommendedSavedCardLast4 = view.findViewById(R.id.card_last_4_digits)
        recommendedSavedCardCheckBox = view.findViewById(R.id.cvv_less_selection)

        brandWalletCard = view.findViewById(R.id.brand_wallet_card_view)
        brandWalletIcon = view.findViewById(R.id.brand_wallet_icon)
        brandWalletTitle = view.findViewById(R.id.brand_wallet_title)
        brandWalletBalanceRow = view.findViewById(R.id.brand_wallet_balance_row)
        brandWalletBalance = view.findViewById(R.id.brand_wallet_balance)
        brandWalletBalanceRefreshIcon = view.findViewById(R.id.brand_wallet_balance_refresh)
        brandWalletDescription = view.findViewById(R.id.brand_wallet_description)
        brandWalletPrimaryAction = view.findViewById(R.id.brand_wallet_primary_action)
        brandWalletSecondaryAction = view.findViewById(R.id.brand_wallet_secondary_action)
        brandWalletProceedAction = view.findViewById(R.id.brand_wallet_proceed_action)
        brandWalletActivateCtaRow = view.findViewById(R.id.brand_wallet_activate_cta_row)
        brandWalletActionsRow = view.findViewById(R.id.brand_wallet_actions_row)
        brandWalletSelectedIcon = view.findViewById(R.id.brand_wallet_selected_icon)
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
                CleverTapUtil.sdkPaymentModeSelected(
                    CleverTapUtil.getInstance(requireContext()),
                    ExpressSDKObject.getFetchData(),
                    getPaymentModeArray(),
                    false,
                    "",
                    "",
                    "",
                    true

                )
            }
        }
    }

    private fun createProcessPaymentRequest(savedCardTokens: SavedCardTokens): ProcessPaymentRequest {
        val paymentData = ExpressSDKObject.getFetchData()?.paymentData
        if (paymentData == null) {
            safeNavigate(R.id.action_paymentModeFragment_to_successFragment)
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

    private fun createBrandWalletProcessPaymentRequest(): ProcessPaymentRequest {
        val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
        val customerId = customerInfo?.customer_id ?: customerInfo?.customerId
        val amount = getBrandWalletOrderAmount()
        val currency = getCurrency()

        val paymentOption = PaymentOptions(
            wallet_details = WalletDetails(customer_id = customerId)
        )
        val extras = Extra(
            payment_mode = arrayListOf(Constants.BRAND_WALLET_ID),
            payment_amount = amount,
            payment_currency = currency,
            card_last4 = null,
            redeemable_amount = null,
            registered_mobile_number = null,
            txn_mode = null,
            device_info = null,
            risk_validation_details = null,
            dcc_status = null,
            sdk_data = Utils.createSDKData(requireActivity()),
            order_amount = amount,
            language = null,
            is_final_part_payment = null,
            location_info = null,
            order_currency = currency,
        )

        return ProcessPaymentRequest(
            extras = extras,
            payment_option = paymentOption,
        )
    }

    private fun createBrandWalletOtpProcessPaymentRequest(): ProcessPaymentRequest {
        val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
        val customerId = customerInfo?.customer_id ?: customerInfo?.customerId
        val amount = getBrandWalletOrderAmount()
        val currency = getCurrency()

        val paymentOption = PaymentOptions(
            wallet_details = WalletDetails(customer_id = customerId)
        )
        val extras = Extra(
            payment_mode = arrayListOf(Constants.BRAND_WALLET_ID),
            payment_amount = amount,
            payment_currency = currency,
            card_last4 = null,
            redeemable_amount = null,
            registered_mobile_number = null,
            txn_mode = null,
            device_info = null,
            risk_validation_details = null,
            dcc_status = null,
            sdk_data = null,
            order_amount = amount,
            language = null,
            is_final_part_payment = null,
            location_info = null,
            order_currency = currency,
        )

        return ProcessPaymentRequest(
            payment_option = paymentOption,
            extras = extras,
        )
    }

    private fun addMoneyToWallet(addMoneyAmountInPaise: Int): WalletAddMoneyRequest {
        val paymentMode = arrayListOf(UPI_ID)
        val extra = Extra(
            payment_mode = paymentMode,
            payment_amount = addMoneyAmountInPaise,
            payment_currency = getCurrency(),
            card_last4 = null,
            redeemable_amount = null,
            registered_mobile_number = null,
            txn_mode = null,
            device_info = null,
            risk_validation_details = null,
            dcc_status = null,
            sdk_data = Utils.createSDKData(requireActivity()),
            order_amount = addMoneyAmountInPaise,
            is_final_part_payment = false
        )
        val upiData = UpiData(UPI_ID, null, "INTENT")
        val mode = "CASH"
        val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
        val customer = CustomerInfo(
            customer_id = customerInfo?.customerId ?: customerInfo?.customer_id ?: "",
            country_code = customerInfo?.countryCode ?: customerInfo?.country_code ?: "91",
            email_id = customerInfo?.emailId ?: customerInfo?.email_id ?: "",
            mobile_number = customerInfo?.mobileNo ?: customerInfo?.mobile_number
            ?: customerInfo?.mobileNumber ?: ""
        )
        val upiTxnData = UpiTransactionData(10)
        val addMoneyToWalletRequest =
            WalletAddMoneyRequest(
                upiData,
                mode,
                customer,
                upiTxnData,
                extra
            )

        return addMoneyToWalletRequest
    }


    private fun setPaymentMode() {
        paymentModeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        paymentModeRecyclerView.addItemDecoration(
            DividerItemDecoration(
                ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_divider)
            )
        )
        bindBrandWalletCard()
        val paymentModes = getPaymentModes().orEmpty()
        if (paymentModes.isEmpty()) {
            paymentOptionCard.visibility = View.GONE
            paymentModeRecyclerView.visibility = View.GONE
            return
        }
        paymentOptionCard.visibility = View.VISIBLE
        paymentModeRecyclerView.visibility = View.VISIBLE
        val adapter =
            PaymentModeRecyclerViewAdapter(
                requireContext(),
                paymentModes,
                isPBPEnabled(paymentModes),
                getPaymentModeSelectionCallback(requireContext())
            )
        paymentModeRecyclerView.adapter = adapter
    }

    private fun bindBrandWalletCard() {
        val brandWalletMode = getBrandWalletPaymentMode()
        if (brandWalletMode == null) {
            brandWalletCard.visibility = View.GONE
            return
        }

        val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
        val isActivatedState = customerInfo?.brandWalletEnabled == true
        val walletBalanceValue = customerInfo?.brandWalletBalance?.value ?: 0
        val resolvedBalanceText = if (isActivatedState) {
            formatBrandWalletBalance(walletBalanceValue)
        } else {
            null
        }
        val orderAmount = getBrandWalletOrderAmount()
        val hasSufficientBalanceForOrder =
            isActivatedState && orderAmount > 0 && walletBalanceValue >= orderAmount
        val shouldShowProceedCta = hasSufficientBalanceForOrder && isBrandWalletCardSelected
        val isZeroBalance = isActivatedState && walletBalanceValue == 0
        if (!isActivatedState) {
            isBrandWalletCardSelected = false
        }

        brandWalletCard.visibility = View.VISIBLE
        brandWalletIcon.imageTintList = null
        brandWalletTitle.text = getBrandWalletTitle()

        brandWalletBalance.text = resolvedBalanceText
        brandWalletBalance.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isActivatedState) R.color.black_text else R.color.green_00722F
            )
        )
        val isBalanceVisible = isActivatedState && !resolvedBalanceText.isNullOrBlank()
        brandWalletBalanceRow.visibility = if (isBalanceVisible) View.VISIBLE else View.GONE
        brandWalletBalance.visibility = if (isBalanceVisible) View.VISIBLE else View.GONE
        brandWalletBalanceRefreshIcon.visibility = if (isZeroBalance) View.VISIBLE else View.GONE
        brandWalletBalanceRefreshIcon.setOnClickListener(
            if (isZeroBalance) {
                View.OnClickListener {
                    bindBrandWalletCard()
                }
            } else {
                null
            }
        )
        brandWalletBalanceRefreshIcon.isClickable = isZeroBalance
        brandWalletBalanceRefreshIcon.isFocusable = isZeroBalance
        brandWalletSelectedIcon.visibility = if (isActivatedState) View.VISIBLE else View.GONE
        brandWalletSelectedIcon.setImageResource(
            if (isBrandWalletCardSelected) R.drawable.ic_check_circle_selected
            else R.drawable.ic_check_circle_unselected
        )
        brandWalletActionsRow.visibility = if (isActivatedState) View.VISIBLE else View.GONE
        brandWalletDescription.visibility = if (isActivatedState) View.GONE else View.VISIBLE
        brandWalletActivateCtaRow.visibility = if (isActivatedState) View.GONE else View.VISIBLE

        brandWalletDescription.text = getString(R.string.brand_wallet_activate_description)
        brandWalletCard.isClickable = !isActivatedState
        brandWalletActivateCtaRow.isClickable = !isActivatedState
        if (!isActivatedState) {
            val activateClickListener = View.OnClickListener {
                showBrandWalletBottomSheet()
            }
            brandWalletCard.setOnClickListener(activateClickListener)
            brandWalletActivateCtaRow.setOnClickListener(activateClickListener)
        } else {
            brandWalletCard.setOnClickListener(null)
            brandWalletActivateCtaRow.setOnClickListener(null)
        }

        brandWalletSelectedIcon.setOnClickListener(
            if (isActivatedState) {
                View.OnClickListener {
                    isBrandWalletCardSelected = !isBrandWalletCardSelected
                    bindBrandWalletCard()
                }
            } else {
                null
            }
        )
        brandWalletSelectedIcon.isClickable = isActivatedState
        brandWalletSelectedIcon.isFocusable = isActivatedState

        brandWalletPrimaryAction.text = getString(R.string.brand_wallet_action_add_money)
        brandWalletPrimaryAction.visibility =
            if (isActivatedState && !hasSufficientBalanceForOrder) View.VISIBLE else View.GONE

        val secondaryActionLabel = getString(R.string.brand_wallet_action_redeem_gift_card)
        brandWalletSecondaryAction.text = secondaryActionLabel
        brandWalletSecondaryAction.visibility =
            if (!isActivatedState || secondaryActionLabel.isBlank()) View.GONE else View.VISIBLE

        brandWalletProceedAction.visibility =
            if (shouldShowProceedCta) View.VISIBLE else View.GONE
        brandWalletProceedAction.setOnClickListener(
            if (shouldShowProceedCta) {
                View.OnClickListener {
                    observeViewModel()
                    isBrandWalletProceedPaymentInFlight = true
                    isBrandWalletProceedOtpFlow = false
                    brandWalletOtpPaymentId = null
                    paymentModeViewModel.processPayment(
                        token = ExpressSDKObject.getToken(),
                        paymentData = createBrandWalletProcessPaymentRequest(),
                    )
                }
            } else {
                null
            }
        )

        bindBrandWalletActionListeners(
            isActivatedState,
            brandWalletPrimaryAction,
            brandWalletPrimaryAction.text?.toString()
        )
        bindBrandWalletActionListeners(
            isActivatedState,
            brandWalletSecondaryAction,
            secondaryActionLabel
        )
    }

    private fun bindBrandWalletActionListeners(
        isActivatedState: Boolean,
        actionView: TextView,
        actionLabel: String?
    ) {
        if (!isActivatedState || actionLabel.isNullOrBlank()) {
            actionView.setOnClickListener(null)
            return
        }
        if (actionLabel.equals(getString(R.string.brand_wallet_action_add_money), true)) {
            actionView.setOnClickListener {
                showBrandWalletAddMoneyBottomSheet()
            }
        } else if (actionLabel.equals(
                getString(R.string.brand_wallet_action_redeem_gift_card),
                true
            )
        ) {
            actionView.setOnClickListener {
                showBrandWalletRedeemGiftCardBottomSheet()
            }
        } else {
            actionView.setOnClickListener(null)
        }
    }

    private fun showBrandWalletBottomSheet() {
        if (!isAdded) return

        brandWalletBottomSheetDialog?.dismiss()
        brandWalletBottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.brand_wallet_activation_bottom_sheet, null)

        val closeButton = view.findViewById<ImageView>(R.id.brand_wallet_sheet_close)
        val createWalletButton = view.findViewById<Button>(R.id.brand_wallet_sheet_create_wallet)
        val termsText = view.findViewById<TextView>(R.id.brand_wallet_sheet_terms)
        Utils.applyPrimaryButtonBackground(createWalletButton)
        termsText.text = HtmlCompat.fromHtml(
            getString(R.string.brand_wallet_terms_text),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        closeButton.setOnClickListener {
            brandWalletBottomSheetDialog?.dismiss()
        }
        createWalletButton.setOnClickListener {
            if (getBrandWalletCustomerEmail().isNullOrBlank()) {
                showBrandWalletVerificationBottomSheet()
            } else {
                createBrandWallet()
            }
        }

        brandWalletBottomSheetDialog?.setContentView(view)
        val bottomSheet =
            brandWalletBottomSheetDialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            layoutParams.height = (screenHeight * 0.74f).toInt()
            it.layoutParams = layoutParams
            behavior.peekHeight = layoutParams.height
            behavior.expandedOffset = (screenHeight * 0.26f).toInt()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isFitToContents = false
            behavior.isDraggable = true
            it.setBackgroundColor(Color.TRANSPARENT)
        }
        brandWalletBottomSheetDialog?.setCancelable(true)
        brandWalletBottomSheetDialog?.setCanceledOnTouchOutside(true)
        brandWalletBottomSheetDialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        brandWalletBottomSheetDialog?.show()
    }

    private fun showBrandWalletVerificationBottomSheet() {
        if (!isAdded) return

        brandWalletReadyDismissJob?.cancel()
        brandWalletReadyDismissJob = null
        brandWalletOtpCountdownJob?.cancel()
        brandWalletOtpCountdownJob = null
        brandWalletBottomSheetDialog?.dismiss()
        brandWalletBottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.brand_wallet_verification_bottom_sheet, null)

        val closeButton = view.findViewById<ImageView>(R.id.brand_wallet_verification_close)
        val emailContainer =
            view.findViewById<LinearLayout>(R.id.brand_wallet_verification_email_state)
        val otpContainer = view.findViewById<LinearLayout>(R.id.brand_wallet_verification_otp_state)
        val emailInput = view.findViewById<EditText>(R.id.brand_wallet_verification_email_input)
        val otpInput = view.findViewById<EditText>(R.id.brand_wallet_verification_otp_input)
        val otpResendText = view.findViewById<TextView>(R.id.brand_wallet_verification_otp_resend)
        val ctaButton = view.findViewById<Button>(R.id.brand_wallet_verification_cta)

        var currentState = BrandWalletVerificationSheetState.EMAIL
        emailInput.setText(brandWalletVerificationEmail ?: "")
        Utils.handleCTAEnableDisable(
            requireContext(),
            Utils.isValidEmail(emailInput.text?.toString()?.trim()),
            ctaButton
        )

        val emailWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (currentState == BrandWalletVerificationSheetState.EMAIL) {
                    Utils.handleCTAEnableDisable(
                        requireContext(),
                        Utils.isValidEmail(s?.toString()?.trim()),
                        ctaButton
                    )
                }
            }
        }
        val otpWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (currentState == BrandWalletVerificationSheetState.OTP) {
                    Utils.handleCTAEnableDisable(
                        requireContext(),
                        (s?.length ?: 0) == 6,
                        ctaButton
                    )
                }
            }
        }
        emailInput.addTextChangedListener(emailWatcher)
        otpInput.addTextChangedListener(otpWatcher)

        fun startBrandWalletOtpCountdown() {
            brandWalletOtpCountdownJob?.cancel()
            brandWalletOtpCountdownJob = viewLifecycleOwner.lifecycleScope.launch {
                var timeLeftMs = 100_000L
                while (timeLeftMs > 0) {
                    otpResendText.isEnabled = false
                    otpResendText.text = HtmlCompat.fromHtml(
                        getString(
                            R.string.resend_otp_in,
                            Utils.formatTimeInMinutes(requireContext(), timeLeftMs)
                        ),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    delay(1000)
                    timeLeftMs -= 1000
                }
                otpResendText.text = getString(R.string.resend_otp)
                otpResendText.isEnabled = true
            }
        }

        fun bindVerificationState(state: BrandWalletVerificationSheetState) {
            currentState = state
            when (state) {
                BrandWalletVerificationSheetState.EMAIL -> {
                    brandWalletOtpCountdownJob?.cancel()
                    brandWalletOtpCountdownJob = null
                    emailContainer.visibility = View.VISIBLE
                    otpContainer.visibility = View.GONE
                    ctaButton.text = getString(R.string.brand_wallet_continue)
                    Utils.handleCTAEnableDisable(
                        requireContext(),
                        Utils.isValidEmail(emailInput.text?.toString()?.trim()),
                        ctaButton
                    )
                }

                BrandWalletVerificationSheetState.OTP -> {
                    emailContainer.visibility = View.GONE
                    otpContainer.visibility = View.VISIBLE
                    ctaButton.text = getString(R.string.brand_wallet_verify_and_create)
                    startBrandWalletOtpCountdown()
                    Utils.handleCTAEnableDisable(
                        requireContext(),
                        (otpInput.text?.length ?: 0) == 6,
                        ctaButton
                    )
                }
            }
            (ctaButton.layoutParams as? ConstraintLayout.LayoutParams)?.topToBottom =
                if (state == BrandWalletVerificationSheetState.EMAIL) {
                    R.id.brand_wallet_verification_email_state
                } else {
                    R.id.brand_wallet_verification_otp_state
                }
            ctaButton.requestLayout()
        }

        closeButton.setOnClickListener {
            brandWalletBottomSheetDialog?.dismiss()
        }
        otpResendText.setOnClickListener {
            if (currentState != BrandWalletVerificationSheetState.OTP || !otpResendText.isEnabled) {
                return@setOnClickListener
            }
            otpInput.text?.clear()
            Utils.handleCTAEnableDisable(requireContext(), false, ctaButton)
            startBrandWalletOtpCountdown()
        }
        ctaButton.setOnClickListener {
            when (currentState) {
                BrandWalletVerificationSheetState.EMAIL -> {
                    val enteredEmail = emailInput.text?.toString()?.trim().orEmpty()
                    if (!Utils.isValidEmail(enteredEmail)) return@setOnClickListener
                    brandWalletVerificationEmail = enteredEmail
                    otpInput.text?.clear()
                    bindVerificationState(BrandWalletVerificationSheetState.OTP)
                }

                BrandWalletVerificationSheetState.OTP -> {
                    if ((otpInput.text?.length ?: 0) != 6) return@setOnClickListener
                    persistBrandWalletCustomerEmail(brandWalletVerificationEmail)
                    createBrandWallet()
                }
            }
        }

        bindVerificationState(BrandWalletVerificationSheetState.EMAIL)
        brandWalletBottomSheetDialog?.setContentView(view)
        val bottomSheet =
            brandWalletBottomSheetDialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isFitToContents = true
            behavior.isDraggable = true
            it.setBackgroundColor(Color.TRANSPARENT)
        }
        brandWalletBottomSheetDialog?.setCancelable(true)
        brandWalletBottomSheetDialog?.setCanceledOnTouchOutside(true)
        brandWalletBottomSheetDialog?.setOnDismissListener {
            brandWalletOtpCountdownJob?.cancel()
            brandWalletOtpCountdownJob = null
            brandWalletBottomSheetDialog = null
        }
        brandWalletBottomSheetDialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        brandWalletBottomSheetDialog?.show()
    }

    private fun showBrandWalletAddMoneyBottomSheet() {
        if (!isAdded) return

        brandWalletOtpCountdownJob?.cancel()
        brandWalletOtpCountdownJob = null
        brandWalletBottomSheetDialog?.dismiss()
        brandWalletBottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.brand_wallet_add_money_bottom_sheet, null)

        val closeButton = view.findViewById<ImageView>(R.id.brand_wallet_add_money_close)
        val amountInput = view.findViewById<EditText>(R.id.brand_wallet_add_money_input)
        val minimumText = view.findViewById<TextView>(R.id.brand_wallet_add_money_minimum)
        val quickAdd3000 = view.findViewById<TextView>(R.id.brand_wallet_quick_add_3000)
        val quickAdd5000 = view.findViewById<TextView>(R.id.brand_wallet_quick_add_5000)
        val quickAdd8000 = view.findViewById<TextView>(R.id.brand_wallet_quick_add_8000)
        val ctaButton = view.findViewById<Button>(R.id.brand_wallet_add_money_cta)
        val requiredTopUpAmount = getBrandWalletRequiredTopUpAmount()
        val nearestHundredTopUpAmount = getNextRoundedTopUpAmount(requiredTopUpAmount, 10_000)
        val plusOneThousandTopUpAmount = nearestHundredTopUpAmount + 100_000
        val plusTwoThousandTopUpAmount = plusOneThousandTopUpAmount + 100_000

        fun updateAddMoneyCtaState(value: CharSequence?) {
            Utils.handleCTAEnableDisable(
                requireContext(),
                value?.toString()?.trim()?.isNotEmpty() == true,
                ctaButton
            )
        }

        amountInput.filters = arrayOf(InputFilter.LengthFilter(10))
        amountInput.isSingleLine = true
        amountInput.maxLines = 1
        updateAddMoneyCtaState(amountInput.text)

        minimumText.text = HtmlCompat.fromHtml(
            getString(
                R.string.brand_wallet_add_money_required_text,
                formatBrandWalletBalanceWithoutPaise(requiredTopUpAmount)
            ),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        fun updateAmount(amount: Int) {
            val amountText = formatBrandWalletInputAmount(amount)
            amountInput.setText(amountText)
            amountInput.setSelection(amountText.length)
        }

        quickAdd3000.text = formatBrandWalletBalanceWithoutPaise(nearestHundredTopUpAmount)
        quickAdd5000.text = formatBrandWalletBalanceWithoutPaise(plusOneThousandTopUpAmount)
        quickAdd8000.text = formatBrandWalletBalanceWithoutPaise(plusTwoThousandTopUpAmount)
        if (requiredTopUpAmount > 0) {
            updateAmount(requiredTopUpAmount)
        } else {
            amountInput.text?.clear()
        }
        updateAddMoneyCtaState(amountInput.text)

        amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                updateAddMoneyCtaState(s)
            }
        })

        closeButton.setOnClickListener {
            brandWalletBottomSheetDialog?.dismiss()
        }
        quickAdd3000.setOnClickListener { updateAmount(nearestHundredTopUpAmount) }
        quickAdd5000.setOnClickListener { updateAmount(plusOneThousandTopUpAmount) }
        quickAdd8000.setOnClickListener { updateAmount(plusTwoThousandTopUpAmount) }
        ctaButton.setOnClickListener {
            val enteredTopUpAmount = extractBrandWalletAmountValue(amountInput.text?.toString())
            val isAmountValid = isBrandWalletAddMoneyAmountValid(enteredTopUpAmount, requiredTopUpAmount)
            if (!isAmountValid) return@setOnClickListener

            paymentModeViewModel.addMoneyToWallet(
                ExpressSDKObject.getToken(),
                addMoneyToWallet(enteredTopUpAmount)
            )
        }

        brandWalletBottomSheetDialog?.setContentView(view)
        val bottomSheet =
            brandWalletBottomSheetDialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isFitToContents = true
            behavior.isDraggable = true
            it.setBackgroundColor(Color.TRANSPARENT)
        }
        brandWalletBottomSheetDialog?.setCancelable(true)
        brandWalletBottomSheetDialog?.setCanceledOnTouchOutside(true)
        brandWalletBottomSheetDialog?.setOnDismissListener {
            brandWalletOtpCountdownJob?.cancel()
            brandWalletOtpCountdownJob = null
            brandWalletBottomSheetDialog = null
        }
        brandWalletBottomSheetDialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        brandWalletBottomSheetDialog?.show()
    }

    private fun showBrandWalletAddMoneyOtpBottomSheet(triggerProcessPayment: Boolean = true) {
        if (!isAdded) return

        brandWalletOtpCountdownJob?.cancel()
        brandWalletOtpCountdownJob = null
        brandWalletBottomSheetDialog?.dismiss()
        brandWalletBottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.brand_wallet_add_money_otp_bottom_sheet, null)

        val otpInput = view.findViewById<EditText>(R.id.brand_wallet_add_money_otp_input)
        val resendText = view.findViewById<TextView>(R.id.brand_wallet_add_money_otp_resend)
        val ctaButton = view.findViewById<Button>(R.id.brand_wallet_add_money_otp_cta)

        otpInput.filters = arrayOf(InputFilter.LengthFilter(BRAND_WALLET_PIN_LENGTH))
        Utils.handleCTAEnableDisable(requireContext(), false, ctaButton)

        otpInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                Utils.handleCTAEnableDisable(
                    requireContext(),
                    (s?.length ?: 0) == BRAND_WALLET_PIN_LENGTH,
                    ctaButton
                )
            }
        })

        fun startPinCountdown() {
            brandWalletOtpCountdownJob?.cancel()
            brandWalletOtpCountdownJob = viewLifecycleOwner.lifecycleScope.launch {
                var secondsLeft = BRAND_WALLET_PIN_RESEND_SECONDS
                while (secondsLeft > 0) {
                    resendText.isEnabled = false
                    resendText.text =
                        getString(R.string.brand_wallet_add_money_resend_pin_in, secondsLeft)
                    delay(1000)
                    secondsLeft -= 1
                }
                resendText.text = getString(R.string.brand_wallet_add_money_resend_pin)
                resendText.isEnabled = true
            }
        }

        resendText.setOnClickListener {
            if (!resendText.isEnabled) return@setOnClickListener
            otpInput.text?.clear()
            Utils.handleCTAEnableDisable(requireContext(), false, ctaButton)
            startPinCountdown()
        }
        ctaButton.setOnClickListener {
            if ((otpInput.text?.length ?: 0) != BRAND_WALLET_PIN_LENGTH) {
                return@setOnClickListener
            }
            val paymentId =
                brandWalletOtpPaymentId ?: ExpressSDKObject.getProcessPaymentResponse()?.payment_id
            if (paymentId.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Unable to verify OTP. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            paymentModeViewModel.resetSubmitOtpState()
            paymentModeViewModel.submitOtp(
                token = ExpressSDKObject.getToken(),
                otpRequest = OTPRequest(
                    payment_id = paymentId,
                    otp = otpInput.text?.toString().orEmpty(),
                )
            )
        }

        startPinCountdown()
        brandWalletBottomSheetDialog?.setContentView(view)
        val bottomSheet =
            brandWalletBottomSheetDialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isFitToContents = true
            behavior.isDraggable = true
            it.setBackgroundColor(Color.TRANSPARENT)
        }
        brandWalletBottomSheetDialog?.setCancelable(true)
        brandWalletBottomSheetDialog?.setCanceledOnTouchOutside(true)
        brandWalletBottomSheetDialog?.setOnDismissListener {
            brandWalletOtpCountdownJob?.cancel()
            brandWalletOtpCountdownJob = null
            isBrandWalletOtpTriggerProcessPayment = false
            isBrandWalletProceedOtpFlow = false
            brandWalletOtpPaymentId = null
            brandWalletBottomSheetDialog = null
        }
        brandWalletBottomSheetDialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        brandWalletBottomSheetDialog?.show()

        if (triggerProcessPayment) {
            isBrandWalletOtpTriggerProcessPayment = true
            brandWalletOtpPaymentId = null
            paymentModeViewModel.processPayment(
                token = ExpressSDKObject.getToken(),
                paymentData = createBrandWalletOtpProcessPaymentRequest(),
            )
        }
    }

    private fun showBrandWalletRedeemGiftCardBottomSheet() {
        if (!isAdded) return

        cancelBrandWalletRedeemJobs()
        brandWalletBottomSheetDialog?.dismiss()
        brandWalletBottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.brand_wallet_redeem_gift_card_bottom_sheet, null)

        val formContainer = view.findViewById<View>(R.id.brand_wallet_redeem_form_container)
        val loadingContainer = view.findViewById<View>(R.id.brand_wallet_redeem_loading_container)
        val closeButton = view.findViewById<ImageView>(R.id.brand_wallet_redeem_close)
        val loadingCloseButton =
            view.findViewById<ImageView>(R.id.brand_wallet_redeem_loading_close)
        val giftCardNumberInput =
            view.findViewById<EditText>(R.id.brand_wallet_redeem_card_number_input)
        val giftCardPinInput = view.findViewById<EditText>(R.id.brand_wallet_redeem_card_pin_input)
        val redeemButton = view.findViewById<Button>(R.id.brand_wallet_redeem_cta)
        val progressBar = view.findViewById<ProgressBar>(R.id.brand_wallet_redeem_loading_progress)

        giftCardPinInput.filters = arrayOf(InputFilter.LengthFilter(BRAND_WALLET_PIN_LENGTH))
        formContainer.visibility = View.VISIBLE
        loadingContainer.visibility = View.GONE
        progressBar.progress = 0

        fun updateRedeemButtonState() {
            val isEnabled =
                isValidBrandWalletGiftCardNumber(giftCardNumberInput.text?.toString()) &&
                        isValidBrandWalletGiftCardPin(giftCardPinInput.text?.toString())
            Utils.handleCTAEnableDisable(requireContext(), isEnabled, redeemButton)
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                updateRedeemButtonState()
            }
        }

        giftCardNumberInput.addTextChangedListener(textWatcher)
        giftCardPinInput.addTextChangedListener(textWatcher)
        updateRedeemButtonState()

        closeButton.setOnClickListener {
            brandWalletBottomSheetDialog?.dismiss()
        }
        loadingCloseButton.setOnClickListener {
            brandWalletBottomSheetDialog?.dismiss()
        }
        redeemButton.setOnClickListener {
            val giftCardNumber = giftCardNumberInput.text?.toString().orEmpty()
            val giftCardPin = giftCardPinInput.text?.toString().orEmpty()
            if (!isValidBrandWalletGiftCardNumber(giftCardNumber) ||
                !isValidBrandWalletGiftCardPin(giftCardPin)
            ) {
                return@setOnClickListener
            }
            formContainer.visibility = View.GONE
            loadingContainer.visibility = View.VISIBLE
            startBrandWalletRedeemMockFlow(progressBar)
        }

        brandWalletBottomSheetDialog?.setContentView(view)
        val bottomSheet =
            brandWalletBottomSheetDialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isFitToContents = true
            behavior.isDraggable = true
            it.setBackgroundColor(Color.TRANSPARENT)
        }
        brandWalletBottomSheetDialog?.setCancelable(true)
        brandWalletBottomSheetDialog?.setCanceledOnTouchOutside(true)
        brandWalletBottomSheetDialog?.setOnDismissListener {
            cancelBrandWalletRedeemJobs()
            brandWalletBottomSheetDialog = null
        }
        brandWalletBottomSheetDialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        brandWalletBottomSheetDialog?.show()
    }

    private fun startBrandWalletRedeemMockFlow(progressBar: ProgressBar) {
        cancelBrandWalletRedeemJobs()
        progressBar.max = 100
        progressBar.progress = 0

        brandWalletRedeemProgressJob = viewLifecycleOwner.lifecycleScope.launch {
            var elapsedMs = 0L
            while (elapsedMs < BRAND_WALLET_REDEEM_TIMEOUT_MS) {
                val progress =
                    ((elapsedMs.toFloat() / BRAND_WALLET_REDEEM_TIMEOUT_MS) * 100).toInt()
                        .coerceIn(0, 95)
                progressBar.progress = progress
                delay(BRAND_WALLET_REDEEM_PROGRESS_INTERVAL_MS)
                elapsedMs += BRAND_WALLET_REDEEM_PROGRESS_INTERVAL_MS
            }
            progressBar.progress = 100
            if (isAdded) {
                brandWalletBottomSheetDialog?.dismiss()
                showBrandWalletRedeemNotEligibleBottomSheet()
            }
        }

        brandWalletRedeemMockResultJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(BRAND_WALLET_REDEEM_MOCK_RESULT_DELAY_MS)
            brandWalletRedeemProgressJob?.cancel()
            brandWalletRedeemProgressJob = null
            progressBar.progress = 100
            delay(150)
            if (isAdded) {
                brandWalletBottomSheetDialog?.dismiss()
                showBrandWalletRedeemNotEligibleBottomSheet()
            }
        }
    }

    private fun showBrandWalletRedeemNotEligibleBottomSheet() {
        if (!isAdded) return

        cancelBrandWalletRedeemJobs()
        brandWalletBottomSheetDialog?.dismiss()
        brandWalletBottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.brand_wallet_redeem_not_eligible_bottom_sheet, null)

        val closeButton = view.findViewById<ImageView>(R.id.brand_wallet_redeem_not_eligible_close)
        val tryAnotherButton = view.findViewById<Button>(R.id.brand_wallet_redeem_not_eligible_cta)
        val termsText =
            view.findViewById<TextView>(R.id.brand_wallet_redeem_not_eligible_terms_text)

        closeButton.setOnClickListener {
            brandWalletBottomSheetDialog?.dismiss()
        }
        tryAnotherButton.setOnClickListener {
            brandWalletBottomSheetDialog?.dismiss()
            showBrandWalletRedeemGiftCardBottomSheet()
        }
        termsText.setOnClickListener {
            brandWalletBottomSheetDialog?.dismiss()
            showBrandWalletTermsConditionsBottomSheet()
        }

        brandWalletBottomSheetDialog?.setContentView(view)
        val bottomSheet =
            brandWalletBottomSheetDialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isFitToContents = true
            behavior.isDraggable = true
            it.setBackgroundColor(Color.TRANSPARENT)
        }
        brandWalletBottomSheetDialog?.setCancelable(true)
        brandWalletBottomSheetDialog?.setCanceledOnTouchOutside(true)
        brandWalletBottomSheetDialog?.setOnDismissListener {
            brandWalletBottomSheetDialog = null
        }
        brandWalletBottomSheetDialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        brandWalletBottomSheetDialog?.show()
    }

    private fun showBrandWalletTermsConditionsBottomSheet() {
        if (!isAdded) return

        brandWalletBottomSheetDialog?.dismiss()
        brandWalletBottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.brand_wallet_terms_conditions_bottom_sheet, null)

        val closeButton = view.findViewById<ImageView>(R.id.brand_wallet_terms_close)
        val gotItButton = view.findViewById<Button>(R.id.brand_wallet_terms_cta)

        closeButton.setOnClickListener {
            brandWalletBottomSheetDialog?.dismiss()
        }
        gotItButton.setOnClickListener {
            brandWalletBottomSheetDialog?.dismiss()
        }

        brandWalletBottomSheetDialog?.setContentView(view)
        val bottomSheet =
            brandWalletBottomSheetDialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            layoutParams.height = (screenHeight * BRAND_WALLET_TERMS_SHEET_HEIGHT_RATIO).toInt()
            it.layoutParams = layoutParams
            behavior.peekHeight = layoutParams.height
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isFitToContents = false
            behavior.expandedOffset =
                ((1 - BRAND_WALLET_TERMS_SHEET_HEIGHT_RATIO) * screenHeight).toInt()
            behavior.isDraggable = true
            it.setBackgroundColor(Color.TRANSPARENT)
        }
        brandWalletBottomSheetDialog?.setCancelable(true)
        brandWalletBottomSheetDialog?.setCanceledOnTouchOutside(true)
        brandWalletBottomSheetDialog?.setOnDismissListener {
            brandWalletBottomSheetDialog = null
        }
        brandWalletBottomSheetDialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        brandWalletBottomSheetDialog?.show()
    }

    private fun cancelBrandWalletRedeemJobs() {
        brandWalletRedeemProgressJob?.cancel()
        brandWalletRedeemProgressJob = null
        brandWalletRedeemMockResultJob?.cancel()
        brandWalletRedeemMockResultJob = null
    }

    private fun isValidBrandWalletGiftCardNumber(value: String?): Boolean {
        return value?.trim()?.isNotEmpty() == true
    }

    private fun isValidBrandWalletGiftCardPin(value: String?): Boolean {
        return value?.length == BRAND_WALLET_PIN_LENGTH && value.all { it.isDigit() }
    }

    private fun extractBrandWalletAmountValue(amountText: String?): Int {
        val sanitizedAmount = amountText.orEmpty().replace(",", "").trim()
        if (sanitizedAmount.isEmpty()) return 0

        return try {
            BigDecimal(sanitizedAmount)
                .setScale(0, RoundingMode.CEILING)
                .toInt()
                .coerceAtLeast(0) * 100
        } catch (_: NumberFormatException) {
            0
        }
    }

    private fun formatBrandWalletInputAmount(amountInPaise: Int): String {
        return ceilBrandWalletAmountToRupee(amountInPaise).toString()
    }

    private fun ceilBrandWalletAmountToRupee(amountInPaise: Int): Int {
        if (amountInPaise <= 0) return 0
        return (amountInPaise + 99) / 100
    }

    private fun formatBrandWalletBalanceWithoutPaise(amountInPaise: Int): String {
        val formatter = DecimalFormat("##,##,##0")
        val amountInRupee = ceilBrandWalletAmountToRupee(amountInPaise)
        return "${ExpressSDKObject.getCurrencySymbol()} ${formatter.format(amountInRupee)}"
    }

    private fun showBrandWalletReadyBottomSheet() {
        if (!isAdded) return

        brandWalletReadyDismissJob?.cancel()
        brandWalletReadyDismissJob = null
        brandWalletBottomSheetDialog?.dismiss()
        brandWalletBottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.brand_wallet_ready_bottom_sheet, null)

        val closeButton = view.findViewById<ImageView>(R.id.brand_wallet_ready_close)
        val subtitle = view.findViewById<TextView>(R.id.brand_wallet_ready_subtitle)
        subtitle.text = HtmlCompat.fromHtml(
            getString(R.string.brand_wallet_ready_subtitle),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        closeButton.setOnClickListener {
            brandWalletBottomSheetDialog?.dismiss()
        }

        brandWalletBottomSheetDialog?.setOnDismissListener {
            brandWalletReadyDismissJob?.cancel()
            brandWalletReadyDismissJob = null
            setBrandWalletActivatedCardToggle(true)
            brandWalletBottomSheetDialog = null
        }
        brandWalletBottomSheetDialog?.setContentView(view)
        val bottomSheet =
            brandWalletBottomSheetDialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isFitToContents = true
            behavior.isDraggable = true
            it.setBackgroundColor(Color.TRANSPARENT)
        }
        brandWalletBottomSheetDialog?.setCancelable(true)
        brandWalletBottomSheetDialog?.setCanceledOnTouchOutside(true)
        brandWalletBottomSheetDialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        brandWalletBottomSheetDialog?.show()
        brandWalletReadyDismissJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(5000)
            if (brandWalletBottomSheetDialog?.isShowing == true) {
                brandWalletBottomSheetDialog?.dismiss()
            }
        }
    }

    private fun setBrandWalletActivatedCardToggle(enabled: Boolean) {
        if (isBrandWalletActivatedCardToggled == enabled) return
        isBrandWalletActivatedCardToggled = enabled
        ExpressSDKObject.getFetchData()?.customerInfo?.apply {
            brandWalletEnabled = enabled
            if (enabled && brandWalletBalance == null) {
                brandWalletBalance = BrandWalletBalance(value = 0, currency = "INR")
            }
        }
        if (view != null && isAdded) {
            bindBrandWalletCard()
        }
    }

    private fun getBrandWalletCustomerEmail(): String? {
        val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
        return customerInfo?.emailId?.trim()?.takeIf { it.isNotEmpty() }
            ?: customerInfo?.email_id?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun createBrandWallet() {
        val request = buildCreateWalletRequest() ?: run {
            showBrandWalletReadyBottomSheet()
            return
        }
        paymentModeViewModel.resetCreateWalletState()
        paymentModeViewModel.createWallet(ExpressSDKObject.getToken(), request)
    }

    private fun buildCreateWalletRequest(): CreateWalletRequest? {
        val fetchData = ExpressSDKObject.getFetchData() ?: return null
        val customer = fetchData.customerInfo ?: return null
        val emailId = getBrandWalletCustomerEmail() ?: return null
        val shippingAddress =
            ExpressSDKObject.getSelectedAddress() ?: customer.shippingAddress
            ?: customer.shipping_address
        val billingAddress = customer.billingAddress ?: fetchData.billingAddress ?: shippingAddress

        val walletCustomer = CustomerInfo(
            firstName = customer.firstName,
            first_name = customer.first_name ?: customer.firstName,
            lastName = customer.lastName,
            last_name = customer.last_name ?: customer.lastName,
            countryCode = customer.countryCode,
            country_code = customer.country_code ?: customer.countryCode,
            mobileNo = customer.mobileNo,
            mobileNumber = customer.mobileNumber,
            mobile_number = customer.mobile_number ?: customer.mobileNumber ?: customer.mobileNo,
            emailId = emailId,
            email_id = emailId,
            customerId = customer.customerId,
            customer_id = customer.customer_id ?: customer.customerId,
            billingAddress = billingAddress,
            shippingAddress = shippingAddress,
            shipping_address = shippingAddress,
        )

        val currencyCode = fetchData.paymentData?.originalTxnAmount?.currency ?: "INR"
        return CreateWalletRequest(
            currency_code = currencyCode,
            customers = listOf(walletCustomer),
        )
    }

    private fun mergeBrandWalletCustomerResponse(customerResponse: CreateWalletResponse) {
        val updatedCustomer = customerResponse.data?.customers?.firstOrNull() ?: return
        val currentCustomer = ExpressSDKObject.getFetchData()?.customerInfo ?: return
        currentCustomer.first_name = updatedCustomer.first_name ?: currentCustomer.first_name
        currentCustomer.last_name = updatedCustomer.last_name ?: currentCustomer.last_name
        currentCustomer.country_code = updatedCustomer.country_code ?: currentCustomer.country_code
        currentCustomer.mobile_number =
            updatedCustomer.mobile_number ?: currentCustomer.mobile_number
        currentCustomer.email_id = updatedCustomer.email_id ?: currentCustomer.email_id
        currentCustomer.emailId = updatedCustomer.email_id ?: currentCustomer.emailId
        currentCustomer.customer_id = updatedCustomer.customer_id ?: currentCustomer.customer_id
        currentCustomer.shipping_address =
            updatedCustomer.shipping_address ?: currentCustomer.shipping_address
        currentCustomer.shippingAddress =
            updatedCustomer.shipping_address ?: currentCustomer.shippingAddress
        currentCustomer.billingAddress =
            updatedCustomer.billingAddress ?: currentCustomer.billingAddress
    }

    private fun persistBrandWalletCustomerEmail(email: String?) {
        val resolvedEmail = email?.trim()?.takeIf { it.isNotEmpty() } ?: return
        ExpressSDKObject.getFetchData()?.customerInfo?.apply {
            emailId = resolvedEmail
            email_id = resolvedEmail
        }
        brandWalletVerificationEmail = resolvedEmail
        if (view != null && isAdded) {
            setContactAndDeliveryDetails()
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

    private fun getBrandWalletPaymentMode(): PaymentMode? {
        return ExpressSDKObject.getFetchData()?.paymentModes?.firstOrNull {
            it.paymentModeId.equals(Constants.BRAND_WALLET_ID, true)
        }
    }

    private fun getBrandWalletTitle(): String {
        val merchantName = ExpressSDKObject.getFetchData()?.merchantInfo?.merchantDisplayName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: ExpressSDKObject.getFetchData()?.merchantInfo?.merchantName
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
            ?: getString(R.string.brand_wallet_title).replace(" Wallet", "")
        return "$merchantName Wallet"
    }

    private fun formatBrandWalletBalance(balance: Int?): String {
        return Utils.convertToRupeesWithSymobl(requireContext(), balance ?: 0)
    }

    private fun getBrandWalletOrderAmount(): Int {
        val payableAmount = ExpressSDKObject.getPayableAmount() ?: ExpressSDKObject.getAmount()
        val orderAmount = if ((payableAmount ?: 0) > 0) {
            payableAmount
        } else {
            ExpressSDKObject.getAmount()
        }
        return orderAmount.coerceAtLeast(0)
    }

    private fun getBrandWalletRequiredTopUpAmount(): Int {
        val payableAmount = getBrandWalletOrderAmount()
        val walletBalance =
            ExpressSDKObject.getFetchData()?.customerInfo?.brandWalletBalance?.value ?: 0
        return (payableAmount - walletBalance).coerceAtLeast(1)
    }

    private fun getNextRoundedTopUpAmount(baseAmount: Int, step: Int): Int {
        if (step <= 0) return baseAmount.coerceAtLeast(0)
        val normalizedAmount = baseAmount.coerceAtLeast(0)
        val remainder = normalizedAmount % step
        return if (remainder == 0) {
            normalizedAmount
        } else {
            normalizedAmount + (step - remainder)
        }
    }

    private fun isBrandWalletAddMoneyAmountValid(enteredAmount: Int, requiredAmount: Int): Boolean {
        return if (requiredAmount > 0) {
            enteredAmount >= requiredAmount
        } else {
            enteredAmount > 0
        }
    }

    private fun isPBPEnabled(paymentModes: List<PaymentMode>): Boolean {
        return paymentModes.any { it.paymentModeId == PAY_BY_POINTS_ID }
    }

    private fun getPaymentModeSelectionCallback(context: Context): ItemClickListener<PaymentMode>? {
        return object : ItemClickListener<PaymentMode> {
            override fun onItemClick(position: Int, item: PaymentMode) {
                CleverTapUtil.sdkPaymentModeSelected(
                    CleverTapUtil.getInstance(requireContext()),
                    ExpressSDKObject.getFetchData(),
                    item.paymentModeId,
                    false,
                    "",
                    "",
                    "",


                    false

                )
                when (item.paymentModeId) {
                    PaymentModes.CREDIT_DEBIT.paymentModeID -> {
                        safeNavigate(R.id.action_paymentModeFragment_to_cardFragment)
                    }

                    PaymentModes.UPI.paymentModeID -> {
                        ExpressSDKObject.setSelectedMode(null)
                        safeNavigate(R.id.action_paymentModeFragment_to_UPIFragment)
                    }

                    PaymentModes.NET_BANKING.paymentModeID -> {
                        // Handle Netbanking selection
                        safeNavigate(R.id.action_paymentModeFragment_to_netBankingFragment)
                    }

                    PaymentModes.WALLET.paymentModeID -> {
                        // Handle Wallet selection
                        safeNavigate(R.id.action_paymentModeFragment_to_walletFragment)
                    }

                    PaymentModes.EMI.paymentModeID -> {
                        // Handle EMI selection
                        safeNavigate(R.id.action_paymentModeFragment_to_EMIFragment)
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
        if (hasObservedPaymentResult) return
        hasObservedPaymentResult = true

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
                            safeNavigate(R.id.action_paymentModeFragment_to_successFragment)
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading)
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())
                        }

                        is BaseResult.Success<ProcessPaymentResponse> -> {
                            ExpressSDKObject.setProcessPaymentResponse(it.data)
                            bottomSheetDialog?.dismiss()
                            safeNavigate(R.id.action_paymentModeFragment_to_ACSFragment)

                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                paymentModeViewModel.processPaymentResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            if (isBrandWalletOtpTriggerProcessPayment &&
                                brandWalletBottomSheetDialog?.isShowing == true
                            ) {
                                isBrandWalletOtpTriggerProcessPayment = false
                                bottomSheetDialog?.dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    it.errorMessage ?: "Unable to send OTP.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@collect
                            }
                            if (isBrandWalletProceedPaymentInFlight) {
                                isBrandWalletProceedPaymentInFlight = false
                                brandWalletOtpPaymentId = null
                                bottomSheetDialog?.dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    it.errorMessage ?: "Unable to proceed with payment.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@collect
                            }
                            val bundle = Bundle()
                            bundle.putString(ERROR_KEY, it.errorCode)
                            bundle.putString(ERROR_MESSAGE_KEY, it.errorMessage)
                            brandWalletBottomSheetDialog?.dismiss()
                            bottomSheetDialog?.dismiss()
                            safeNavigate(R.id.action_paymentModeFragment_to_successFragment)
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading)
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())
                        }

                        is BaseResult.Success<ProcessPaymentResponse> -> {
                            if (isBrandWalletOtpTriggerProcessPayment &&
                                brandWalletBottomSheetDialog?.isShowing == true
                            ) {
                                isBrandWalletOtpTriggerProcessPayment = false
                                brandWalletOtpPaymentId = it.data.payment_id
                                ExpressSDKObject.setProcessPaymentResponse(it.data)
                                bottomSheetDialog?.dismiss()
                                return@collect
                            }
                            if (isBrandWalletProceedPaymentInFlight) {
                                isBrandWalletProceedPaymentInFlight = false
                                ExpressSDKObject.setProcessPaymentResponse(it.data)
                                brandWalletOtpPaymentId = it.data.payment_id
                                bottomSheetDialog?.dismiss()
                                isBrandWalletProceedOtpFlow = true
                                showBrandWalletAddMoneyOtpBottomSheet(triggerProcessPayment = false)
                                return@collect
                            }
                            ExpressSDKObject.setProcessPaymentResponse(it.data)
                            brandWalletBottomSheetDialog?.dismiss()
                            bottomSheetDialog?.dismiss()
                            safeNavigate(R.id.action_paymentModeFragment_to_ACSFragment)

                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                paymentModeViewModel.addMoneyToWalletResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            val bundle = Bundle()
                            bundle.putString(ERROR_KEY, it.errorCode)
                            bundle.putString(ERROR_MESSAGE_KEY, it.errorMessage)
                            brandWalletBottomSheetDialog?.dismiss()
                            bottomSheetDialog?.dismiss()
                            safeNavigate(R.id.action_paymentModeFragment_to_successFragment)
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading)
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())

                        }

                        is BaseResult.Success<WalletAddMoneyResponse> -> {
                            ExpressSDKObject.setWalletAddMoneyResponse(it.data)
                            ExpressSDKObject.setProcessPaymentResponse(
                                mapWalletAddMoneyToProcessPaymentResponse(it.data)
                            )
                            ExpressSDKObject.setSelectedMode(Constants.BRAND_WALLET_ID)
                            brandWalletBottomSheetDialog?.dismiss()
                            bottomSheetDialog?.dismiss()
                            val bundle = Bundle()
                            bundle.putString("MODE", Constants.BRAND_WALLET_ID)
                            safeNavigate(R.id.action_paymentModeFragment_to_UPIFragment, bundle)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                paymentModeViewModel.submitOtpResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            paymentModeViewModel.resetSubmitOtpState()
                            bottomSheetDialog?.dismiss()
                            Toast.makeText(
                                requireContext(),
                                it.errorMessage ?: "Invalid OTP. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading) {
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())
                            }
                        }

                        is BaseResult.Success<OTPResponse> -> {
                            paymentModeViewModel.resetSubmitOtpState()
                            isBrandWalletProceedPaymentInFlight = false
                            val shouldNavigateToSuccess = isBrandWalletProceedOtpFlow
                            if (shouldNavigateToSuccess) {
                                isBrandWalletProceedOtpFlow = false
                            }
                            brandWalletBottomSheetDialog?.dismiss()
                            bottomSheetDialog?.dismiss()
                            if (shouldNavigateToSuccess) {
                                safeNavigate(R.id.action_paymentModeFragment_to_successFragment)
                            } else {
                                ExpressSDKObject.setSelectedMode(Constants.BRAND_WALLET_ID)
                                val bundle = Bundle()
                                bundle.putString("MODE", Constants.BRAND_WALLET_ID)
                                safeNavigate(R.id.action_paymentModeFragment_to_UPIFragment, bundle)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun mapWalletAddMoneyToProcessPaymentResponse(
        response: WalletAddMoneyResponse
    ): ProcessPaymentResponse {
        val firstPayment = response.charge_order?.payments?.firstOrNull()
        val deepLink = response.charge_order?.challenge_url ?: firstPayment?.challenge_url
        val paymentId = firstPayment?.id
        val orderId = response.charge_order?.order_id ?: response.order_id

        return ProcessPaymentResponse(
            redirect_url = null,
            response_code = response.response_code?.toString() ?: "-1",
            response_message = response.response_message ?: "",
            pg_upi_unique_request_id = null,
            deep_link = deepLink,
            payment_id = paymentId,
            order_id = orderId,
            short_link = null,
        )
    }

    private fun observeCreateWalletResult() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                paymentModeViewModel.createWalletResult.collect { result ->
                    when (result) {
                        is BaseResult.Error -> {
                            paymentModeViewModel.resetCreateWalletState()
                            Toast.makeText(
                                requireContext(),
                                result.errorMessage ?: "Something went wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is BaseResult.Loading -> Unit

                        is BaseResult.Success<CreateWalletResponse> -> {
                            paymentModeViewModel.resetCreateWalletState()
                            mergeBrandWalletCustomerResponse(result.data)
                            showBrandWalletReadyBottomSheet()
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
        if (maxSavings == null || maxSavings <= 0) {
            //Hide the offer views
            offerViewLayout?.visibility = View.GONE
            return
        }
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

    private fun getBestOfferRecommended(offerDetails: List<OfferDetail>?) {
        val item = offerDetails?.firstOrNull()
            ?: ExpressSDKObject.getEMIPaymentModeData()?.offerDetails?.firstOrNull()
        if (item != null) {
            val spec = BankColors.getGradientColors(item.issuer?.display_name)

            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                spec.colors.toIntArray()
            )
            recommendedParentLayout.background = gradientDrawable

            //TODO this could be null handle that case
            getBankLogo(item.name)?.let { bankLogoName.setImageResource(it) }
            offerType.text = getEMITypeLabel(item.tenureOffers?.firstOrNull()?.emiType)
            emiDiscount.text = Utils.convertToRupeesWithSymobl(
                requireContext(),
                item.tenureOffers?.firstOrNull()?.discountAmount ?: 0
            )
            bankOffer.text = Utils.convertToRupeesWithSymobl(
                requireContext(),
                item.tenureOffers?.firstOrNull()?.cashbackAmount ?: 0
            )
            val fullTenure = item.tenureOffers?.firstOrNull()?.fullTenure
                ?: ExpressSDKObject.getEMIPaymentModeData()?.issuers?.find { it.id == item.issuerId }?.tenures?.find { it.tenure_id == item.tenureOffers?.firstOrNull()?.tenureId }
            perMonthEmi.text = Utils.convertToRupeesWithSymobl(
                requireContext(),
                fullTenure?.monthly_emi_amount?.value ?: 0
            )
            if (item.tenureOffers?.firstOrNull()?.tenureId == "7") {
                emiDuration.visibility = View.GONE
                totalPayable.visibility = View.GONE
            }
            emiDuration.text = String.format(
                requireContext().getString(R.string.for_x_months),
                fullTenure?.tenure_value.toString()
            )
            totalPayable.text = Utils.convertToRupeesWithSymobl(
                requireContext(),
                fullTenure?.loan_amount?.value ?: 0
            ) + "/month"
            handleRecommendedOptionClick()
        } else
            recommendedParentLayout.visibility = View.GONE
    }

    private fun getEMITypeLabel(emiType: String?): String {
        return when (emiType) {
            "NO_COST" -> getString(R.string.no_cost_emi)
            "LOW_COST" -> getString(R.string.low_interest)
            else -> ""
        }
    }

    private fun handleRecommendedOptionClick() {
        actionBtn.setOnClickListener {
            CleverTapUtil.sdkPaymentModeSelected(
                CleverTapUtil.getInstance(requireContext()),
                ExpressSDKObject.getFetchData(),
                getPaymentModeArray(),
                true,
                "",
                "",
                "",
                false

            )
            val offerDetails = ExpressSDKObject.getEMIPaymentModeData()?.offerDetails?.firstOrNull()
            ExpressSDKObject.setSelectedOfferDetail(offerDetails)
            val issuer =
                ExpressSDKObject.getEMIPaymentModeData()?.issuers?.find { it.id == offerDetails?.issuerId }
            val tenure =
                issuer?.tenures?.find { it.tenure_id == offerDetails?.tenureOffers?.firstOrNull()?.tenureId }
            val bundle = Bundle()
            bundle.putString(ISSUE_ID, offerDetails?.issuerId)
            bundle.putString(TENURE_ID, tenure?.tenure_id)
            if (offerDetails?.type?.equals(EMI_DC_TYPE, true) == false)
                safeNavigate(
                    R.id.action_paymentModeFragment_to_EMICardDetailsFragment,
                    bundle
                )
            else
                safeNavigate(
                    R.id.action_paymentModeFragment_to_DCEMICardDetailsFragment,
                    bundle
                )
        }
    }

    private fun handleRecommendationOptions() {
        recommendedOptionLabel.visibility = View.GONE
        recommendedUPIVPAParentLayout.visibility = View.GONE
        recommendedParentLayout.visibility = View.GONE
        recommendedSavedCardParentLayout.visibility = View.GONE

        val lastPaymentMode = ExpressSDKObject.getFetchData()?.customerInfo?.lastUsedPaymode
        val lastPayModeUsed = lastPaymentMode?.lastTransactionPaymentMode
        if (lastPaymentMode == null) {
            // do not show any paymodes
            // do nothing
            return
        } else if (lastPayModeUsed.isNullOrEmpty()) {
            // show last used method
            recommendedOptionLabel.visibility = View.VISIBLE
            recommendedParentLayout.visibility = View.VISIBLE
            getBestOfferRecommended(null)
        } else if (lastPayModeUsed.equals("REWARD", true) || lastPayModeUsed.contains(
                "CARD",
                true
            ) && (!lastPaymentMode.card.lastUsedCard.firstOrNull()?.cardLast4.isNullOrEmpty())
        ) {
            //handle for card
            recommendedOptionLabel.visibility = View.VISIBLE
            val isSavedCardsAvailable =
                ExpressSDKObject.getFetchData()?.customerInfo?.tokens?.filter {
                    it?.cardData?.last4Digit == lastPaymentMode.card.lastUsedCard.firstOrNull()?.cardLast4?.takeLast(
                        4
                    )
                }
            if (!isSavedCardsAvailable.isNullOrEmpty()) {
                //show saved card view
                recommendedSavedCardParentLayout.visibility = View.VISIBLE
                recommendedSavedCardText.text =
                    isSavedCardsAvailable.firstOrNull()?.cardData?.issuerName
                recommendedSavedCardLast4.text =
                    isSavedCardsAvailable.firstOrNull()?.cardData?.last4Digit
                recommendedSavedCardCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    recommendedSavedCardParentLayout.background = AppCompatResources.getDrawable(
                        requireContext(),
                        if (isChecked) R.color.dense_background else R.drawable.input_field_border
                    )
                    actionBtn.visibility = if (isChecked) View.VISIBLE else View.GONE
                    actionBtn.isClickable = isChecked
                    actionBtn.text = getString(
                        R.string.pay_amount_text, ExpressSDKObject.getCurrencySymbol(),
                        Utils.convertInRupees(ExpressSDKObject.getAmount())
                    )
                    actionBtn.setOnClickListener {
                        // process payment with saved card
                        observeViewModel()
                        val createProcessPaymentRequest =
                            createProcessPaymentRequest(isSavedCardsAvailable.first())
                        viewModel.processPayment(
                            token = ExpressSDKObject.getToken(),
                            paymentData = createProcessPaymentRequest
                        )
                    }
                }
            } else {
                // now check for if its in offerdetails
                val offerDetails =
                    ExpressSDKObject.getFetchData()?.customerInfo?.lastUsedPaymode?.card?.lastUsedCard?.firstOrNull()?.emiData?.offerDetails
                recommendedParentLayout.visibility = View.VISIBLE
                handleRecommendedOptionClick()
                getBestOfferRecommended(offerDetails)

            }
            //if we want to show the cards lets first check if there are any saved cards and


        } else if (lastPayModeUsed.contains("UPI", true)) {
            //handle for UPI
            recommendedUPIVPAParentLayout.visibility = View.VISIBLE
            val upiId = lastPaymentMode.upi.lastUsedVPAs.firstOrNull()
            if (!upiId.isNullOrEmpty()) {
                recommendedUPIVPAParentLayout.visibility = View.VISIBLE
                recommendedUPIVPATextview.text = upiId
                upiVPACheck.setOnCheckedChangeListener { buttonView, isChecked ->
                    recommendedUPIVPAParentLayout.background = AppCompatResources.getDrawable(
                        requireContext(),
                        if (isChecked) R.color.dense_background else R.drawable.input_field_border
                    )
                    payByUPIVPABtn.visibility = if (isChecked) View.VISIBLE else View.GONE
                    payByUPIVPABtn.isClickable = isChecked
                    payByUPIVPABtn.setOnClickListener {
                        // pass upi id to upifragment and run the process payment
                        val bundle = Bundle()
                        bundle.putString("RECOMMENDED_ACTION_UPI", upiId)
                        ExpressSDKObject.setSelectedMode(null)
                        safeNavigate(
                            R.id.action_paymentModeFragment_to_UPIFragment,
                            bundle
                        )
                    }
                }


            }


        }

    }


    private fun mapBanKLogo() {
        bankLogoMap = Utils.getBankLogoHashMap()
        bankNameKeyList = Utils.getListOfBanKTitle()
        banKTitleToCodeMap = Utils.bankTitleAndCodeMapper()
    }


    private fun primaryBanksLogo(): HashMap<String, Int> {
        val hashMap: HashMap<String, Int> = hashMapOf(
            AXIS_TITLE to R.drawable.ic_axis_bank,
            HDFC_TITLE to R.drawable.ic_hdfc_bank,
            ICICI_TITLE to R.drawable.ic_icici_bank,
            INDIAN_TITLE to R.drawable.ic_indian_overseas_bank,
            YES_TITLE to R.drawable.ic_yes_bank,
            KOTAK_TITLE to R.drawable.ic_kotak_bank,
            MAHARASHTRA_TITLE to R.drawable.ic_maharashtra_bank,
            INDUSIND_TITLE to R.drawable.ic_indusind_bank,
            STANDARD_CHARTERED_TITLE to R.drawable.ic_standard_chartered_bank,
            SBI_TITLE to R.drawable.ic_sbi,
        )
        return hashMap
    }


    // Step 2: Function to extract the first word from full bank name
    fun getFirstWord(name: String): String {
        return name.trim().split("\\s+".toRegex()).firstOrNull() ?: ""
    }

    // Step 3: Function to find the logo based on full bank name
    fun getBankLogo(fullBankName: String): Int? {
        val firstWord = getFirstWord(fullBankName)
        val bankLogos = primaryBanksLogo()
        return bankLogos.entries.firstOrNull {
            it.key.equals(firstWord, ignoreCase = true)
        }?.value
    }


}
