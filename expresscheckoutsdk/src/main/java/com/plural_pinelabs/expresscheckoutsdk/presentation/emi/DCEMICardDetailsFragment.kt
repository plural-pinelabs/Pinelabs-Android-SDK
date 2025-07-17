package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.CardFragmentViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.Constants
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_IMAGES
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BROWSER_ACCEPT_ALL
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BROWSER_USER_AGENT_ANDROID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_MESSAGE_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TENURE_ID
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.MTAG
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.cardIcons
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.cardTypes
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardData
import com.plural_pinelabs.expresscheckoutsdk.data.model.DeviceInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.EmiData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetails
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferEligibilityResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure
import com.plural_pinelabs.expresscheckoutsdk.presentation.card.CardFragmentViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class DCEMICardDetailsFragment : Fragment() {

    private lateinit var cardEditText: EditText
    private lateinit var payBtn: Button
    private lateinit var cardErrorText: TextView
    private lateinit var phoneNumberErrorText: TextView
    private lateinit var backBtn: ImageView
    private lateinit var phoneNumberText: EditText
    private lateinit var logo: ImageView
    private lateinit var issuerTitleTv: TextView
    private lateinit var emiPerMonthAmount: TextView
    private lateinit var emiForXMonthTv: TextView

    private var binData: CardBinMetaDataResponse? = null
    private var cardNumber: String = ""
    private var formattedCardNumber: String? = null
    private var cursorPosition = 0
    private var isCardValid = false
    private var isPhoneValid = false
    private var bottomSheetDialog: BottomSheetDialog? = null
    private lateinit var bankLogoMap: HashMap<String, String>
    private lateinit var banKTitleToCodeMap: HashMap<String, String>
    private lateinit var bankNameKeyList: List<String>
    private lateinit var viewModel: CardFragmentViewModel //TODO if required later move it to the its own view model or make this view model as common for two fr

    private var issuerId: String? = null
    private var tenureId: String? = null
    private var issuer: Issuer? = null
    private var selectedTenure: Tenure? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tenureId = arguments?.getString(TENURE_ID)
        issuerId = arguments?.getString(Constants.ISSUE_ID)
        viewModel = ViewModelProvider(
            this, CardFragmentViewModelFactory(NetworkHelper(requireContext()))
        )[CardFragmentViewModel::class.java]
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_d_c_e_m_i_card_details, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapBanKLogo()
        setEMIIssuer()
        setupViews(view)
        observeViewModel()
        setCardFocusListener()
        setUpCardNumberValidation()
        setUpPhoneNumberValidation()
    }

    private fun setEMIIssuer() {
        val emiPaymentModeData = ExpressSDKObject.getEMIPaymentModeData()
        issuerId?.let {
            emiPaymentModeData?.issuers?.filter {
                it.id == issuerId
            }?.let { issuerList ->
                if (issuerList.isNotEmpty()) {
                    issuer = issuerList[0]
                }
            }
        }
        tenureId?.let {
            issuer?.let { it1 ->
                selectedTenure = it1.tenures.find { it.tenure_id == tenureId }
            }
        }
    }

    private fun setupViews(view: View) {
        cardEditText = view.findViewById(R.id.card_number_et)
        payBtn = view.findViewById(R.id.continue_btn)
        cardErrorText = view.findViewById(R.id.error_message_card_details)
        backBtn = view.findViewById(R.id.back_button)
        phoneNumberText = view.findViewById(R.id.editTextMobileNumber)
        phoneNumberErrorText = view.findViewById(R.id.error_message_registered_number)
        logo = view.findViewById(R.id.logo)
        issuerTitleTv = view.findViewById(R.id.issuer_title)
        emiPerMonthAmount = view.findViewById(R.id.emi_per_month_value)
        emiForXMonthTv = view.findViewById(R.id.for_x_month_tv)
        loadBankLogo()
        issuerTitleTv.text = Utils.getTitleForEMI(requireContext(), issuer)
        emiPerMonthAmount.text =
            Utils.convertToRupeesWithSymobl(
                requireContext(),
                selectedTenure?.monthly_emi_amount?.value
            )
        emiForXMonthTv.text =
            String.format(getString(R.string.for_x_months), selectedTenure?.tenure_value.toString())


        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        setUpAmount()
    }

    private fun loadBankLogo() {
        issuer?.let { item ->
            val imageTitle =
                bankNameKeyList.find {
                    it.contains(
                        item.display_name.removeSuffix(" BANK"),
                        ignoreCase = true
                    )
                }

            if (imageTitle != null) {
                val imageUrl = BASE_IMAGES + bankLogoMap[banKTitleToCodeMap[imageTitle]]
                val imageLoader = ImageLoader.Builder(requireContext())
                    .components {
                        add(SvgDecoder.Factory())
                    }
                    .crossfade(true)
                    .build()
                val request = ImageRequest.Builder(requireContext())
                    .data(imageUrl)
                    .target(logo)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
                imageLoader.enqueue(request)
            }

        }
    }

    private fun setUpAmount() {
        payBtn.text = getString(
            R.string.pay_amount_text,
            getString(R.string.rupee_symbol),
            Utils.convertInRupees(ExpressSDKObject.getAmount())
        )
        payBtn.setOnClickListener {
            validateAllFields()
        }
    }

    private fun observeViewModel() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.metaDataResult.collect { result ->
                    when (result) {
                        is BaseResult.Error -> {
                            result.errorCode.let { exception ->
                                Log.e(MTAG, exception)
                            }

                        }

                        is BaseResult.Success<CardBinMetaDataResponse> -> {
                            result.data.let { it ->
                                binData = it
                                setCardBrandIcon(
                                    cardEditText, it.card_payment_details[0].card_network
                                )
                                Log.d(MTAG, " Meta Data fetched successfully")
                            }
                        }

                        is BaseResult.Loading -> {

                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.processPaymentResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            val bundle = Bundle()
                            bundle.putString(ERROR_KEY, it.errorCode)
                            bundle.putString(ERROR_MESSAGE_KEY, it.errorMessage)
                            bottomSheetDialog?.dismiss()
                            findNavController().navigate(
                                R.id.action_DCEMICardDetailsFragment_to_successFragment, bundle
                            )
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading) bottomSheetDialog =
                                showProcessPaymentDialog(requireContext())
                        }

                        is BaseResult.Success<ProcessPaymentResponse> -> {
                            ExpressSDKObject.setProcessPaymentResponse(it.data)
                            redirectToACS()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.validateOfferResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            showCardDetailsError("OfferEligibility")
                            Log.e("Error", it.errorMessage ?: "Unknown error")
                        }

                        is BaseResult.Loading -> {
                        }

                        is BaseResult.Success<OfferEligibilityResponse> -> {
                            if (it.data.code.equals("ELIGIBLE", true)) {
                                // If the offer is eligible, proceed with payment
                                //enable the payment button
                                initProcessPayment(false)
                            } else {
                                // If not eligible, show error or handle accordingly
                                showCardDetailsError("OfferEligibility")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setCardFocusListener() {
        cardEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val cardNumber = cardEditText.text.toString().replace(" ", "")
                if (cardNumber.length >= 19) {
                    showCardDetailsError("CardNumber")
                } else if (cardNumber.length < 4) {
                    showCardDetailsError("CardNumber")
                    cardEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                } else if (cardNumber.length < 12) {
                    showCardDetailsError("CardNumber")
                } else {
                    val cardType = validateCardType(cardNumber)
                    if (cardType.isNullOrEmpty()) {
                        showCardDetailsError("CardNumber")
                    } else {
                        isCardValid = true
                        hideCardDetailsError()
                    }
                }
            }
        }
    }

    private fun redirectToACS(
    ) {
        bottomSheetDialog?.dismiss()
        findNavController().navigate(R.id.action_EMICardDetailsFragment_to_ACSFragment)

    }

    private fun setUpCardNumberValidation() {
        cardEditText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!isFormatting) {
                    cursorPosition = cardEditText.selectionStart
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                cardNumber = s?.toString()?.replace(Regex("\\s+"), "") ?: ""
                if (isFormatting) return
                isFormatting = true
                val unformatted = s.toString().replace(" ", "")
                val formatted = unformatted.chunked(4).joinToString(" ")

                cardEditText.setText(formatted)
                formattedCardNumber = formatted
                cardEditText.setSelection(
                    if (cursorPosition + (formatted.length - unformatted.length) < formatted.length) cursorPosition + (formatted.length - unformatted.length)
                    else formatted.length
                )

                formattedCardNumber?.let { cardEditText.setSelection(it.length) }
                if (cardNumber.length >= 12) {
                    //call metadata api for all input after 12
                    viewModel.getCardBinMetaData(
                        token = ExpressSDKObject.getToken() ?: "", cardNumber = cardNumber
                    )
                }
                isFormatting = false

            }

        })
    }

    private fun setUpPhoneNumberValidation() {
        phoneNumberText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text change
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed during text change
            }

            override fun afterTextChanged(s: Editable?) {
                val phoneNumber = s.toString()
                if (phoneNumber.length < 10 || !Utils.isValidPhoneNumber(phoneNumber)) {
                    showCardDetailsError("Phone")
                } else {
                    isPhoneValid = true
                    phoneNumberErrorText.visibility = View.GONE
                }
            }
        })
    }


    private fun setCardBrandIcon(etCardNumber: EditText, cardType: String?) {
        val iconResId = cardIcons[cardType]
        if (iconResId != null) {
            etCardNumber.setCompoundDrawablesWithIntrinsicBounds(
                null, null, ContextCompat.getDrawable(requireContext(), iconResId), null
            )
        } else {
            etCardNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    private fun validateCardType(cardNumber: String): String? {
        for ((cardType, regex) in cardTypes) {
            if (regex.matches(cardNumber)) {
                return cardType
            }
        }
        return null
    }


    private fun hideCardDetailsError() {
        cardErrorText.visibility = View.GONE
    }


    private fun showCardDetailsError(errorType: String) {
        when (errorType) {
            "CardNumber" -> {
                isCardValid = false
                cardErrorText.text = getString(R.string.please_enter_a_valid_card_number)
                cardErrorText.visibility = View.VISIBLE
            }

            "OfferEligibility" -> {
                isCardValid = false
                cardErrorText.text = "Card is not eligible for the selected offer"
                // cardErrorText.text = getString(R.string.offer_not_eligible) TODO get string from garima
                cardErrorText.visibility = View.VISIBLE
            }

            "Phone" -> {
                isPhoneValid = false
                phoneNumberErrorText.text = "Enter a valid number"
                phoneNumberErrorText.visibility = View.VISIBLE

            }

            else -> {
                // Handle other error types if needed
                Log.e("EMICardDetailsFragment", "Unknown error type: $errorType")
            }
        }
    }

    private fun validateAllFields() {
        if (isCardValid && isPhoneValid) {
            createValidateOfferRequest()
        } else {
            if (!isCardValid) {
                showCardDetailsError("CardNumber")
            } else if (!isPhoneValid) {
                showCardDetailsError("Phone")
            } else {
                // Handle other validation errors if needed
                Log.e("EMICardDetailsFragment", "Validation failed for card or phone number")
            }
        }
    }

    private fun initProcessPayment(shouldSaveCard: Boolean = false) {
        val createProcessPaymentRequest = createProcessPaymentRequest(shouldSaveCard)
        viewModel.processPayment(
            token = ExpressSDKObject.getToken(), paymentData = createProcessPaymentRequest
        )
    }

    private fun createValidateOfferRequest() {
        val createProcessPaymentRequest = createProcessPaymentRequest(false)
        viewModel.validateOffer(
            token = ExpressSDKObject.getToken(),
            createProcessPaymentRequest
        )
    }

    private fun createProcessPaymentRequest(shouldSaveCard: Boolean): ProcessPaymentRequest {

        val cardNumber = cardEditText.text.toString().filter { !it.isWhitespace() }

        var cardExpiryMonth = ""
        var cardExpiryYear = ""
        val offerDetails =
            ExpressSDKObject.getEMIPaymentModeData()?.offerDetails?.find { it.issuerId == issuerId }
        val offerTenure = offerDetails?.tenureOffers?.find { it.tenureId == tenureId }


        val paymentData = ExpressSDKObject.getFetchData()?.paymentData
        if (paymentData == null) {
            //TODO notify of payment failure
            findNavController().navigate(R.id.action_EMICardDetailsFragment_to_failureFragment)
        }
        val amount = issuer?.tenures?.find { it.tenure_id == tenureId }?.loan_amount?.value
            ?: 0
        val currency = paymentData?.originalTxnAmount?.currency


        val paymentMode = arrayListOf<String>()
        paymentMode.add(Constants.DEBIT_EMI_ID)
        val last4 = cardNumber.substring(cardNumber.length - 4, cardNumber.length)

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        val pixelFormat = requireActivity().windowManager.defaultDisplay.pixelFormat

        val screenSize: String = width.toString() + "x" + height.toString()
        val deviceInfo = DeviceInfo(
            null,
            BROWSER_USER_AGENT_ANDROID,
            BROWSER_ACCEPT_ALL,
            Locale.getDefault().language,
            height.toString(),
            width.toString(),
            Utils.getTimeOffset().toString(),
            screenSize,
            Utils.getColorDepth(pixelFormat).toString(),
            true,
            true,
            Utils.getDeviceId(requireActivity()),
            Utils.getLocalIpAddress().toString()
        )

        val cardDataExtra = Extra(
            paymentMode,
            amount,
            currency,
            last4,
            null, //TODO redeemableAmount pass this from reward points api
            phoneNumberText.text.toString(),
            null,
            deviceInfo,
            null,
            null,// dccstatus pass this from dcc api call
            Utils.createSDKData(requireActivity()),
            order_amount = ExpressSDKObject.getAmount()
        )
        val cardData = CardData(
            cardNumber,
            "",
            "",
            cardExpiryYear,
            cardExpiryMonth,
            false,
            shouldSaveCard
        )
        val emiData = EmiData(
            OfferDetails(
                id = issuer?.id,
                name = issuer?.name,
                display_name = issuer?.display_name,
                issuer_type = issuer?.issuer_type,
                priority = issuer?.priority,
                issuer_data = issuer?.issuer_data,
                label = offerTenure?.offerLabel,
                subventionType = offerTenure?.emiType,
                isMultiCartEmi = false,
                issuerName = issuer?.name ?: "",
                isSplitEmi = false,
                tenure = issuer?.tenures?.find { it.tenure_id == tenureId }
            )

        )
        val processPaymentRequest = ProcessPaymentRequest(
            null,
            null,
            cardData,
            upi_data = null,
            null,
            null,
            cardDataExtra,
            null,
            null,
            emi_data = emiData
        )
        return processPaymentRequest
    }

    private fun mapBanKLogo() {
        bankLogoMap = Utils.getBankLogoHashMap()
        bankNameKeyList = Utils.getListOfBanKTitle()
        banKTitleToCodeMap = Utils.bankTitleAndCodeMapper()
    }

}