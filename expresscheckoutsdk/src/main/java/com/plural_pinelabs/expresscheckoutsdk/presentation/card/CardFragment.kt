package com.plural_pinelabs.expresscheckoutsdk.presentation.card

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import com.plural_pinelabs.expresscheckoutsdk.common.CardFragmentViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.Constants
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BROWSER_ACCEPT_ALL
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BROWSER_USER_AGENT_ANDROID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_MESSAGE_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.cardIcons
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.cardTypes
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardData
import com.plural_pinelabs.expresscheckoutsdk.data.model.DeviceInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class CardFragment : Fragment() {
    private var isNativeOTP = false
    private var isPBPEnabled = false // TODO to configure this from the server
    private var isDCCEnabled = false // TODO to configure this from the server
    private var isSavedCardEnabled = false // TODO to configure this from the server

    private var binData: CardBinMetaDataResponse? = null

    private lateinit var cardEditText: EditText
    private lateinit var expiryEditText: EditText
    private lateinit var cvvEditText: EditText
    private lateinit var cardHolderText: EditText
    private lateinit var payBtn: Button
    private lateinit var cardErrorText: TextView
    private lateinit var cardHolderErrorText: TextView
    private lateinit var saveCardCheckbox: CheckBox
    private lateinit var backBtn: ImageView
    private lateinit var savedCardParentLayout: ConstraintLayout

    //PBP view
    private lateinit var pbpRedeemDefaultParentLayout: LinearLayout
    private lateinit var pbpRedeemInfoParentLayout: LinearLayout
    private lateinit var pbpCheckPointLayout: LinearLayout
    private lateinit var pbpCheckPointsButton: TextView
    private lateinit var pbpCheckPointsProgressLayout: LinearLayout
    private lateinit var pbpCheckPointProgressBar: ProgressBar
    private lateinit var pbpRedeemPointsParentLayout: ConstraintLayout
    private lateinit var pbpRedeemPointsBankIcon: ImageView
    private lateinit var pbpRedeemPointsTitlePointText: TextView
    private lateinit var pbpRedeemPointsTitleAmountText: TextView
    private lateinit var pbpRedeemPointsCheckBox: CheckBox
    private lateinit var pbpRedeemPointsErrorParentLayout: ConstraintLayout
    private lateinit var pbpRedeemPointsErrorTitleText: TextView
    private lateinit var pbpRedeemPointsErrorDescriptionText: TextView

    private var cardNumber: String = ""
    private var formattedCardNumber: String? = null

    private var isFormatting = false
    private var cursorPosition = 0
    private var isCardValid = false
    private var isExpiryValid = false
    private var isCVVValid = false
    private var isCardHolderNameValid: Boolean = false
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var mPBPBottomSheetDialog: BottomSheetDialog? = null
    private var isPBPNumberValid = false
    private var redeemableAmount: Int = 0 // TODO to configure this from the server
    private var isPBPChecked = false // TODO to configure this from the server


    private lateinit var viewModel: CardFragmentViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(
            this,
            CardFragmentViewModelFactory(NetworkHelper(requireContext()))
        )[CardFragmentViewModel::class.java]
        return inflater.inflate(R.layout.fragment_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isPBPEnabled = checkIfPBPIsEnabled()
        setFlags(ExpressSDKObject.getFetchData())
        setupViews(view)
        observeViewModel()
        setCardFocusListener()
        setUpCardNumberValidation()
        setupCardHolderNameValidation(cardHolderText)
        setupCVVValidation(cvvEditText)
        setupExpiryValidation(expiryEditText)
        showPBPView(1)
    }

    private fun showPBPView(viewType: Int) {
        when (viewType) {
            1 -> {


            }

        }

    }

    private fun setFlags(fetchData: FetchResponseDTO?) {
        fetchData?.merchantInfo?.featureFlags?.let {
            isNativeOTP = it.isNativeOTPEnabled ?: false
            isDCCEnabled = it.isDCCEnabled ?: false
            isSavedCardEnabled = it.isSavedCardEnabled ?: false
        }
    }

    private fun setupViews(view: View) {
        cardEditText = view.findViewById(R.id.card_number_et)
        expiryEditText = view.findViewById(R.id.expiry_date_et)
        cvvEditText = view.findViewById(R.id.cvv_et)
        payBtn = view.findViewById(R.id.continue_btn)
        cardHolderText = view.findViewById(R.id.full_name_et)
        cardHolderErrorText = view.findViewById(R.id.error_message_card_holder_name)
        cardErrorText = view.findViewById(R.id.error_message_card_details)
        saveCardCheckbox = view.findViewById(R.id.save_card_checkbox)
        backBtn = view.findViewById(R.id.back_button)
        savedCardParentLayout = view.findViewById(R.id.saved_card_parent_layout)
        if (isSavedCardEnabled) {
            savedCardParentLayout.visibility = View.VISIBLE
        }

        //PBP Views
        pbpRedeemDefaultParentLayout = view.findViewById(R.id.redeem_points_parent_layout)
        pbpRedeemInfoParentLayout = view.findViewById(R.id.redeem_points_info_layout)
        pbpCheckPointLayout = view.findViewById(R.id.redeem_points_check_point_layout)
        pbpCheckPointsButton = view.findViewById(R.id.check_for_points_text)
        pbpCheckPointsProgressLayout = view.findViewById(R.id.checking_points_progress_layout)
        pbpCheckPointProgressBar = view.findViewById(R.id.checking_points_progress_bar)
        pbpRedeemPointsParentLayout = view.findViewById(R.id.redeem_points_redeem_button_layout)
        pbpRedeemPointsBankIcon = view.findViewById(R.id.redeem_points_icon)
        pbpRedeemPointsTitlePointText = view.findViewById(R.id.redeem_card_points_text)
        pbpRedeemPointsTitleAmountText = view.findViewById(R.id.redeem_points_instant_discount)
        pbpRedeemPointsCheckBox = view.findViewById(R.id.redeem_points_checkbox)
        pbpRedeemPointsErrorParentLayout =
            view.findViewById(R.id.redeem_points_redeem_error_layout)
        pbpRedeemPointsErrorTitleText =
            view.findViewById(R.id.redeem_error_title_text)
        pbpRedeemPointsErrorDescriptionText =
            view.findViewById(R.id.redeem_error_description_text)
        pbpRedeemPointsCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            isPBPChecked = isChecked
            if (isChecked) {
                // TODO Show the redeemable amount
                //add the amount to the total amount
            } else {
                // replace with the original amount
            }
        }

        pbpRedeemDefaultParentLayout.visibility = View.GONE
        pbpRedeemPointsParentLayout.visibility = View.GONE
        pbpRedeemPointsErrorParentLayout.visibility = View.GONE


        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        setUpAmount()
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
                viewModel.otpRequestResult.collect { result ->
                    when (result) {
                        is BaseResult.Loading -> {
                        }

                        is BaseResult.Success -> {
                            bottomSheetDialog?.dismiss()
                            navigateToNativeOTP(
                                result.data.meta_data?.resend_after,
                                result.data.next?.getOrNull(0)
                            )
                        }

                        is BaseResult.Error -> {
                            bottomSheetDialog?.dismiss()
                            redirectToACS()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.metaDataResult.collect { result ->
                    when (result) {
                        is BaseResult.Error -> {
                            result.errorCode.let { exception ->
                                Log.e("Error", exception)
                            }

                        }

                        is BaseResult.Success<CardBinMetaDataResponse> -> {
                            result.data.let { it ->
                                binData = it
                                setCardBrandIcon(
                                    cardEditText,
                                    it.card_payment_details[0].card_network
                                )
                                isNativeOTP = it.card_payment_details[0].is_native_otp_supported
                                // TODO Procss the data for DCC
                                Log.d("Success", " Meta Data fetched successfully")

                            }
                        }

                        is BaseResult.Loading -> {

                        }
                    }
                }
            }
        }
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
                            findNavController().navigate(
                                R.id.action_cardFragment_to_failureFragment,
                                bundle
                            )
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading)
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())
                        }

                        is BaseResult.Success<ProcessPaymentResponse> -> {
                            ExpressSDKObject.setProcessPaymentResponse(it.data)
                            if (isNativeOTP) {
                                callNativeRequestOTP()
                            } else {
                                redirectToACS()
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
                        if (validCard(cardNumber)) {
                            isCardValid = true
                            hideCardDetailsError()
                        } else {
                            showCardDetailsError("CardNumber")
                        }
                    }
                }
            }
        }
    }

    private fun navigateToNativeOTP(resendAfter: String?, otpId: String?) {
        val bundle = Bundle().apply {
            putString("resend_after", resendAfter)
            putString("otp_id", otpId)
        }
        bottomSheetDialog?.dismiss()
        findNavController().navigate(R.id.action_cardFragment_to_nativeOTPFragment, bundle)
    }

    private fun redirectToACS(
    ) {
        bottomSheetDialog?.dismiss()
        findNavController().navigate(R.id.action_cardFragment_to_ACSFragment)

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
                    if (cursorPosition + (formatted.length - unformatted.length) < formatted.length)
                        cursorPosition + (formatted.length - unformatted.length)
                    else
                        formatted.length
                )

                formattedCardNumber?.let { cardEditText.setSelection(it.length) }
                if (cardNumber.length >= 12) {
                    //call metadata api for all input after 12
                    viewModel.getCardBinMetaData(
                        token = ExpressSDKObject.getToken() ?: "",
                        cardNumber = cardNumber
                    )
                }
                isFormatting = false

            }

        })
    }


    private fun setCardBrandIcon(etCardNumber: EditText, cardType: String?) {
        val iconResId = cardIcons[cardType]
        if (iconResId != null) {
            etCardNumber.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(requireContext(), iconResId),
                null
            )
        } else {
            etCardNumber.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }

    private fun setupCardHolderNameValidation(etCardHolderName: EditText) {
        etCardHolderName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val name = s?.toString()?.trim()
                val filteredName = name?.filter { it.isLetter() || it.isWhitespace() }
                if (name != filteredName) {
                    etCardHolderName.setText(filteredName)
                    etCardHolderName.setSelection(filteredName?.length ?: 0)
                }
                isCardHolderNameValid = !filteredName.isNullOrEmpty()
                if (!isCardHolderNameValid) {
                    showCardDetailsError("CardHolderName")
                } else {
                    hideCardHolderNameError()
                }
            }
        })
    }

    private fun setupExpiryValidation(
        etExpiry: EditText,
    ) {
        etExpiry.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val input = s.toString().replace(Regex("[^\\d]"), "")
                val formattedInput = when {
                    input.length <= 2 -> input
                    input.length <= 4 -> "${input.substring(0, 2)}/${input.substring(2)}"
                    else -> "${input.substring(0, 2)}/${input.substring(2, 4)}"
                }

                etExpiry.setText(formattedInput)
                etExpiry.setSelection(formattedInput.length)

                isEditing = false
            }
        })

        etExpiry.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val parts = etExpiry.text.toString().split("/")
                if (parts.size == 2) {
                    val month = parts[0].toIntOrNull()
                    val year = parts[1].toIntOrNull()?.plus(2000)

                    if (month == null || year == null || month !in 1..12) {
                        showCardDetailsError("Expiry")
                    } else {
                        val calendar = Calendar.getInstance()
                        val currentYear = calendar.get(Calendar.YEAR)
                        val currentMonth = calendar.get(Calendar.MONTH) + 1

                        if (year < currentYear || (year == currentYear && month < currentMonth)) {
                            showCardDetailsError("Expiry")
                        } else {
                            isExpiryValid = true
                        }
                    }
                } else {
                    showCardDetailsError("Expiry")
                }
            }
        }
    }

    private fun setupCVVValidation(etCVV: EditText) {
        etCVV.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
            }
        })

        etCVV.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val cvv = etCVV.text.toString()
                if (cvv.isEmpty() || cvv.length < 3) {
                    showCardDetailsError("CVV")
                } else {
                    isCVVValid = true
                    hideCardDetailsError()
                }
            }
        }
    }

    private fun validCard(cardNumber: String): Boolean {
        // Luhn Algorithm to validate the card number
        var sum = 0
        var alternate = false
        for (i in cardNumber.length - 1 downTo 0) {
            var n = cardNumber[i].digitToInt()
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n -= 9
                }
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
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

    private fun hideCardHolderNameError() {
        cardHolderErrorText.visibility = View.GONE
    }

    private fun showCardDetailsError(errorType: String) {
        when (errorType) {
            "CardNumber" -> {
                isCardValid = false
                cardErrorText.text = getString(R.string.please_enter_a_valid_card_number)
                cardErrorText.visibility = View.VISIBLE
            }

            "Expiry" -> {
                isExpiryValid = false
                cardErrorText.text = getString(R.string.please_enter_a_valid_expiry)
                cardErrorText.visibility = View.VISIBLE
            }

            "CVV" -> {
                isCVVValid = false
                cardErrorText.text = getString(R.string.please_enter_a_valid_cvv)
                cardErrorText.visibility = View.VISIBLE
            }

            "CardHolderName" -> {
                isCardHolderNameValid = false
                cardHolderErrorText.text = getString(R.string.please_enter_a_valid_card_holder_name)
                cardHolderErrorText.visibility = View.VISIBLE
            }
        }
    }

    private fun validateAllFields() {
        if (isCardValid && isExpiryValid && isCVVValid && isCardHolderNameValid) {

            if (saveCardCheckbox.isChecked) {
                navigateToSaveCardOTPFragment()
            } else {
                initProcessPayment()
            }
        } else {
            if (!isCardValid) {
                showCardDetailsError("CardNumber")
            }
            if (!isExpiryValid) {
                showCardDetailsError("Expiry")
            }
            if (!isCVVValid) {
                showCardDetailsError("CVV")
            }
            if (!isCardHolderNameValid) {
                showCardDetailsError("CardHolderName")
            }
        }
    }

    private fun initProcessPayment(shouldSaveCard: Boolean = false) {
        val createProcessPaymentRequest = createProcessPaymentRequest(shouldSaveCard)
        viewModel.processPayment(
            token = ExpressSDKObject.getToken(),
            paymentData = createProcessPaymentRequest
        )
    }

    private fun observeSaveCardCallbackResponse() {

        val navController = findNavController()
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

        savedStateHandle?.getLiveData<Boolean>("success")?.observe(viewLifecycleOwner) { result ->
            savedStateHandle.remove<Boolean>("success")
            initProcessPayment(result)
        }

    }


    private fun createProcessPaymentRequest(shouldSaveCard: Boolean): ProcessPaymentRequest {

        val cardNumber = cardEditText.text.toString().filter { !it.isWhitespace() }
        val cvv = cvvEditText.text.toString()
        val cardHolderName = cardHolderText.text.toString()
        val cardExpiry = expiryEditText.text.toString()
        val cardExpiryMonth = cardExpiry.split("/")[0]
        val cardExpiryYear = "20" + cardExpiry.split("/")[1]
        val paymentData = ExpressSDKObject.getFetchData()?.paymentData
        if (paymentData == null) {
            //TODO notify of payment failure
            findNavController().navigate(R.id.action_cardFragment_to_failureFragment)
        }
        var amount = paymentData?.originalTxnAmount?.amount
        val currency = paymentData?.originalTxnAmount?.currency


        val paymentMode = arrayListOf<String>()
        paymentMode.add(Constants.CREDIT_DEBIT_ID)
        if (isPBPChecked) {
            paymentMode.add(Constants.PAY_BY_POINTS_ID)
            amount = amount!! - redeemableAmount
        }
        val last4 = cardNumber.substring(cardNumber.length - 4, cardNumber.length)

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        val pixelFormat = requireActivity().windowManager.defaultDisplay.pixelFormat
//        var dccStatus: String? =
//            if (isDCCEnabled && binData?.is_international_card == true && binData?.is_currency_supported == false)
//                DCC_STATUS.FORCE_OPT_OUT.toString()
//            else null

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
            true, true, Utils.getDeviceId(requireActivity()),
            Utils.getLocalIpAddress().toString()
        )

        val cardDataExtra =
            Extra(
                paymentMode,
                amount,
                currency,
                last4,
                null, //TODO redeemableAmount pass this from reward points api
                null
                /*mobileNumber!!.filter { !mobileNumber!!.isEmpty() }*/,
                null,
                deviceInfo,
                null,
                null,// dccstatus pass this from dcc api call
                Utils.createSDKData(requireActivity())
            )
        val cardData =
            CardData(
                cardNumber,
                cvv,
                cardHolderName,
                cardExpiryYear,
                cardExpiryMonth,
                isNativeOTP,
                shouldSaveCard
            )
        val processPaymentRequest =
            ProcessPaymentRequest(
                null,
                null,
                cardData,
                upi_data = null,
                null,
                null,
                cardDataExtra,
                null,
                null
            )
        return processPaymentRequest
    }

    private fun showCheckPointsBottomSheetDialog() {
        mPBPBottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.pbp_bottom_sheetl_layout, null)
        val phoneNumberEt = view.findViewById<EditText>(R.id.phone_number_et)
        val checkPointsBtn = view.findViewById<Button>(R.id.check_points_btn)

        phoneNumberEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                isPBPNumberValid = s?.let {
                    it.length == 10 && Utils.isValidPhoneNumber(it.toString())
                } ?: false
            }
        })
        checkPointsBtn.setOnClickListener {
            if (isPBPNumberValid) {
                //TODO handle the number pass it back and init the API call for the check pbp points
            }
        }
        mPBPBottomSheetDialog?.setCancelable(false)
        mPBPBottomSheetDialog?.setCanceledOnTouchOutside(false)
        mPBPBottomSheetDialog?.setContentView(view)
        mPBPBottomSheetDialog?.show() // Show the dialog first
    }

    private fun checkIfPBPIsEnabled(): Boolean {
        val paymentModes = ExpressSDKObject.getFetchData()?.paymentModes?.filter {
            it.paymentModeId == Constants.PAY_BY_POINTS_ID
        }
        return !paymentModes.isNullOrEmpty()
    }


    private fun callNativeRequestOTP() {
        val paymentId = ExpressSDKObject.getProcessPaymentResponse()?.payment_id ?: ""
        val otpRequest = OTPRequest(payment_id = paymentId)
        viewModel.generateOTP(ExpressSDKObject.getToken(), otpRequest)
    }

    private fun navigateToSaveCardOTPFragment() {
        observeSaveCardCallbackResponse()
        val last4Digits = cardNumber.substring(
            formattedCardNumber?.length?.minus(
                4
            ) ?: 0
        )
        val bundle = Bundle()
        bundle.putString("last4Digits", last4Digits)
        findNavController().navigate(
            R.id.action_cardFragment_to_saveCardOTPFragment,
            bundle
        )
    }

}