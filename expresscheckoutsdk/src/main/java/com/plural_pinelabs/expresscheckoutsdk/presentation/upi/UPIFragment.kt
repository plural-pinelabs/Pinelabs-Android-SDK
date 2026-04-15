package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import UpiAppsAdapter
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject.getAmount
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject.getCurrency
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.CleverTapUtil
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_ATTEMPTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_PENDING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BRAND_WALLET_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_INTENT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_INTENT_PREFIX
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_INTENT_QR
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.UPIViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.safeNavigate
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.MTAG
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentOptions
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiData
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiTransactionData
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletAddMoneyLocationInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletDetails
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class UPIFragment : Fragment() {

    private lateinit var payByAnyUPIButton: TextView
    private lateinit var upiAppsRv: RecyclerView
    private lateinit var viewModel: UPIViewModel
    private var mTransactionMode: String? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var qrBottomSheetDialog: BottomSheetDialog? = null
    private var bottomTimerSheetDialog: BottomSheetDialog? = null
    private var selectUPIPackage: String? = null
    private var flowMode: String? = null
    private var allInstalledUpiApps: List<String> = emptyList()
    private var isShowingAllUpiApps: Boolean = false

    private lateinit var payByQRButton: LinearLayout
    private lateinit var payByQRLayout: ConstraintLayout
    private var isQRPayment: Boolean = false
    private var consumedDeepLink = false
    private var isQRAllowed = false
    private var brandWalletOtpBottomSheetDialog: BottomSheetDialog? = null
    private var brandWalletOtpCountdownJob: Job? = null
    private var isBrandWalletOtpTriggerProcessPayment = false
    private var hasShownBrandWalletOtpSheet = false
    private var brandWalletOtpPaymentId: String? = null


    private lateinit var transactionLauncher: ActivityResultLauncher<Intent>

    private companion object {
        const val BRAND_WALLET_PIN_LENGTH = 6
        const val BRAND_WALLET_PIN_RESEND_SECONDS = 120
        const val COLLAPSED_UPI_APPS_COUNT = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == 0) {
                // If the result is not OK, cancel the transaction process
                cancelTransactionProcess()
                return@registerForActivityResult
            }
            if (flowMode.equals(BRAND_WALLET_ID, true)) {
                if (bottomTimerSheetDialog?.isShowing != true) {
                    showProcessPaymentTimerDialog()
                    viewModel.startCountDownTimer()
                }
                return@registerForActivityResult
            }
            showProcessPaymentTimerDialog()
            viewModel.startCountDownTimer()
            viewModel.startPolling()
        }
        flowMode = arguments?.getString("MODE", null) ?: ExpressSDKObject.getSelectedMode()
        if (!flowMode.isNullOrBlank()) {
            ExpressSDKObject.setSelectedMode(flowMode)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(
            this,
            UPIViewModelFactory(NetworkHelper(requireContext()))
        )[UPIViewModel::class.java]
        return inflater.inflate(R.layout.fragment_u_p_i, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        setUpPayByUPIApps()
        observeViewModel()
        handleConvenienceFees()
    }

    private fun setViews(view: View) {
        payByAnyUPIButton = view.findViewById(R.id.pay_by_any_upi)
        upiAppsRv = view.findViewById(R.id.upi_app_rv)
        payByQRButton = view.findViewById(R.id.pay_by_qr_btn)
        payByQRLayout = view.findViewById(R.id.pay_by_qr)

        if (flowMode.equals(BRAND_WALLET_ID, true)) {
            payByAnyUPIButton.text = getString(R.string.pay_by_existing_upi_app)
        }


        payByAnyUPIButton.setOnClickListener {
            payAction(UPI_INTENT)
        }
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        val upiPaymentMode = getUPiFLowsList().joinToString(",")
        if (!upiPaymentMode.contains("Intent", true)) {
            upiAppsRv.visibility = View.GONE
            payByAnyUPIButton.visibility = View.GONE
        }
        if (!upiPaymentMode.contains("Intent", true) && isQRAllowed) {
            payByQRLayout.visibility = View.GONE
        }

        payByQRButton.setOnClickListener {
            isQRPayment = true
            payAction(UPI_INTENT_QR)
        }
    }

    private fun setUpPayByUPIApps() {
        allInstalledUpiApps = getUpiAppsInstalledInDevice()
        isShowingAllUpiApps = false

        if (allInstalledUpiApps.isNotEmpty()) {
            payByAnyUPIButton.visibility = View.VISIBLE
            upiAppsRv.visibility = View.VISIBLE
            upiAppsRv.layoutManager = GridLayoutManager(requireContext(), 2)
            renderUpiApps()
        } else {
            upiAppsRv.visibility = View.GONE
        }
    }

    private fun renderUpiApps() {
        val displayedApps = if (!isShowingAllUpiApps && allInstalledUpiApps.size > COLLAPSED_UPI_APPS_COUNT) {
            allInstalledUpiApps.take(COLLAPSED_UPI_APPS_COUNT) + UpiAppsAdapter.MORE_APPS_ITEM
        } else {
            allInstalledUpiApps
        }

        upiAppsRv.adapter = UpiAppsAdapter(displayedApps, getItemClickListenerForUPIApp())
    }

    private fun getItemClickListenerForUPIApp(): ItemClickListener<String> {
        return object : ItemClickListener<String> {
            override fun onItemClick(position: Int, item: String) {
                if (item == UpiAppsAdapter.MORE_APPS_ITEM) {
                    isShowingAllUpiApps = true
                    renderUpiApps()
                    return
                }
                selectUPIPackage = item
                payAction(UPI_INTENT)
            }
        }
    }

    private fun getUpiAppsInstalledInDevice(): List<String> {
        return try {
            val listOfUPIPackage = getUpiAppsInstalledInDevice(ExpressSDKObject.getFetchData())
            val listOfPaymentReadyApps = mutableListOf<String>()
            listOfPaymentReadyApps.addAll(getListOfActiveUPIApps(listOfUPIPackage))
            getUpiAppsInDisplayOrder(listOfPaymentReadyApps)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun isAppUpiReady(packageName: String): Boolean {
        var appUpiReady = false
        val upiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(UPI_INTENT_PREFIX))
        val pm = requireActivity().packageManager
        val upiActivities: List<ResolveInfo> = pm.queryIntentActivities(upiIntent, 0)
        for (activity in upiActivities) {
            if (activity.activityInfo.packageName.equals(packageName, ignoreCase = true)) {
                appUpiReady = true
                break
            }
        }
        return appUpiReady
    }

    private fun getListOfActiveUPIApps(listOfUPIPackage: List<String>): List<String> {
        val finalUpiAppsList = mutableListOf<String>()
        for (app in listOfUPIPackage) {
            if (isAppUpiReady(app.lowercase())) {
                finalUpiAppsList.add(app)
            }
        }
        return finalUpiAppsList
    }

    private fun showUpiTray(deepLink: String, upiAppPackageName: String?) {
        val upiPayIntent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.data = deepLink.toUri()
        if (upiAppPackageName != null) {
            upiPayIntent.`package` = upiAppPackageName
        }
        val chooser = Intent.createChooser(upiPayIntent, getString(R.string.upi_open_with))
        if (chooser.resolveActivity(requireActivity().packageManager) != null) {
            transactionLauncher.launch(chooser)
        } else {
            cancelTransactionProcess()
            Toast.makeText(
                requireActivity(),
                "No UPI app found, please install one to continue",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun payAction(transactionMode: String?) {
        if (flowMode.equals(BRAND_WALLET_ID, true)) {
            val addMoneyResponse = ExpressSDKObject.getWalletAddMoneyResponse()
            val inquiryOrderId = addMoneyResponse?.charge_order?.order_id
            val existingDeepLink = addMoneyResponse?.charge_order?.challenge_url
                ?: addMoneyResponse?.charge_order?.payments?.firstOrNull()?.challenge_url
                ?: ExpressSDKObject.getProcessPaymentResponse()?.deep_link

            if (inquiryOrderId.isNullOrBlank()) {
                Toast.makeText(
                    requireActivity(),
                    "Unable to start inquiry. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            if (!existingDeepLink.isNullOrBlank()) {
                if (bottomTimerSheetDialog?.isShowing != true) {
                    showProcessPaymentTimerDialog()
                    viewModel.startCountDownTimer()
                }
                viewModel.startPolling(inquiryOrderId)
                showUpiTray(existingDeepLink, upiAppPackageName = selectUPIPackage)
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Unable to launch UPI app. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        mTransactionMode = transactionMode
        val paymentMode = arrayListOf(UPI_ID)
        val extra = Extra(
            payment_mode = paymentMode,
            payment_amount = getAmount(),
            payment_currency = getCurrency(),
            card_last4 = null,
            redeemable_amount = null,
            registered_mobile_number = null,
            txn_mode = null,
            device_info = null,
            risk_validation_details = null,
            dcc_status = null,
            sdk_data = Utils.createSDKData(requireActivity()),
            is_final_part_payment = true,
            location_info = WalletAddMoneyLocationInfo()
        )
        val transactionModeValue = when (transactionMode) {
            UPI_INTENT -> UPI_INTENT
            UPI_INTENT_QR -> UPI_INTENT
            else -> UPI_INTENT
        }
        val upiData = UpiData(UPI_ID, null, transactionModeValue)
        val convenienceFeesData = viewModel.selectedConvenienceFee?.let {
            Utils.getConvenienceFeesRequest(
                it
            )
        }
        val upiTxnData = UpiTransactionData(10)
        val processPaymentRequest =
            ProcessPaymentRequest(
                null,
                null,
                null,
                upiData,
                null,
                null,
                extra,
                upiTxnData,
                convenienceFeesData
            )
        initProcessPayment(processPaymentRequest)
        CleverTapUtil.sdkCheckoutContinueClicked(
            CleverTapUtil.getInstance(requireContext()),
            ExpressSDKObject.getFetchData(),
            PaymentModes.UPI.paymentModeName.toString(),
            Utils.getCartValue(),
            "not known",
            payByAnyUPIButton.text.toString()
        )
    }

    private fun initProcessPayment(processPaymentRequest: ProcessPaymentRequest) {
        viewModel.processPayment(
            token = ExpressSDKObject.getToken(),
            paymentData = processPaymentRequest
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.processPaymentResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            if (isBrandWalletOtpTriggerProcessPayment) {
                                isBrandWalletOtpTriggerProcessPayment = false
                                bottomSheetDialog?.dismiss()
                                viewModel.resetPaymentFlowResponse()
                                Toast.makeText(
                                    requireContext(),
                                    it.errorMessage ?: "Unable to send OTP.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@collect
                            }
                            cancelTransactionProcess()
                            safeNavigate(R.id.action_UPIFragment_to_successFragment)
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading)
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())
                        }

                        is BaseResult.Success<ProcessPaymentResponse> -> {
                            if (isBrandWalletOtpTriggerProcessPayment) {
                                isBrandWalletOtpTriggerProcessPayment = false
                                ExpressSDKObject.setProcessPaymentResponse(it.data)
                                brandWalletOtpPaymentId = it.data.payment_id
                                bottomSheetDialog?.dismiss()
                                viewModel.resetPaymentFlowResponse()
                                return@collect
                            }
                            bottomSheetDialog?.dismiss()
                            if (mTransactionMode == UPI_INTENT) {
                                showUpiTray(
                                    it.data.deep_link ?: "",
                                    upiAppPackageName = selectUPIPackage
                                )
                            } else if (mTransactionMode == UPI_INTENT_QR && isQRPayment) {
                                viewModel.startPolling()
                            } else {
                                showUpiTray(
                                    it.data.deep_link ?: "",
                                    upiAppPackageName = selectUPIPackage
                                )
                            }
                            bottomSheetDialog?.dismiss()
                            ExpressSDKObject.setProcessPaymentResponse(it.data)
                            viewModel.resetPaymentFlowResponse()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.transactionStatusResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            //Throw error and exit SDK
                            //TODO Pass error message and description
                            bottomSheetDialog?.dismiss()
                            qrBottomSheetDialog?.dismiss()
                            safeNavigate(R.id.action_UPIFragment_to_failureFragment)
                        }

                        is BaseResult.Loading -> {
                            // nothing to do since we already show the process payment dialog
                        }

                        is BaseResult.Success<TransactionStatusResponse> -> {
                            val status = it.data.data.status
                            when (status) {

                                PROCESSED_PENDING -> {
                                    // Do nothing, we will keep polling for the transaction status
                                    val deepLink = it.data.data.deep_link
                                    if (!deepLink.isNullOrEmpty() && !consumedDeepLink && isQRPayment) {
                                        consumedDeepLink = true
                                        //show QR
                                        showQRBottomSheet(deepLink)
                                    }
                                }


                                PROCESSED_STATUS -> {
                                    if (flowMode.equals(BRAND_WALLET_ID, true)) {
                                        startBrandWalletOtpFlowAfterUpi()
                                        viewModel.resetTransactionResponse()
                                        return@collect
                                    }
                                    cancelTransactionProcess()
                                    safeNavigate(R.id.action_UPIFragment_to_successFragment)
                                }

                                PROCESSED_ATTEMPTED -> {
                                    if (flowMode.equals(BRAND_WALLET_ID, true)) {
                                        startBrandWalletOtpFlowAfterUpi()
                                        viewModel.resetTransactionResponse()
                                        return@collect
                                    }
                                    cancelTransactionProcess()
                                    safeNavigate(R.id.action_UPIFragment_to_successFragment)

                                }

                                PROCESSED_FAILED -> {
                                    cancelTransactionProcess()
                                    safeNavigate(R.id.action_UPIFragment_to_successFragment)
                                }
                            }
                            viewModel.resetTransactionResponse()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitOtpResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            viewModel.resetSubmitOtpState()
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
                            viewModel.resetSubmitOtpState()
                            cancelTransactionProcess()
                            safeNavigate(R.id.action_UPIFragment_to_successFragment)
                        }
                    }
                }
            }
        }
    }

    private fun startBrandWalletOtpFlowAfterUpi() {
        if (hasShownBrandWalletOtpSheet) return
        hasShownBrandWalletOtpSheet = true
        viewModel.stopPolling()
        bottomTimerSheetDialog?.dismiss()
        qrBottomSheetDialog?.dismiss()
        showBrandWalletOtpBottomSheet()
    }

    private fun showBrandWalletOtpBottomSheet() {
        if (!isAdded) return
        if (brandWalletOtpBottomSheetDialog?.isShowing == true) return

        brandWalletOtpCountdownJob?.cancel()
        brandWalletOtpCountdownJob = null
        brandWalletOtpBottomSheetDialog?.dismiss()
        brandWalletOtpBottomSheetDialog = BottomSheetDialog(requireContext())
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
            viewModel.resetSubmitOtpState()
            viewModel.submitOtp(
                token = ExpressSDKObject.getToken(),
                otpRequest = OTPRequest(
                    payment_id = paymentId,
                    otp = otpInput.text?.toString().orEmpty(),
                )
            )
        }

        startPinCountdown()
        brandWalletOtpBottomSheetDialog?.setContentView(view)
        brandWalletOtpBottomSheetDialog?.setCancelable(true)
        brandWalletOtpBottomSheetDialog?.setCanceledOnTouchOutside(true)
        brandWalletOtpBottomSheetDialog?.setOnDismissListener {
            brandWalletOtpCountdownJob?.cancel()
            brandWalletOtpCountdownJob = null
            brandWalletOtpBottomSheetDialog = null
        }
        brandWalletOtpBottomSheetDialog?.show()

        isBrandWalletOtpTriggerProcessPayment = true
        brandWalletOtpPaymentId = null
        viewModel.processPayment(
            token = ExpressSDKObject.getToken(),
            paymentData = createBrandWalletOtpProcessPaymentRequest(),
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
            payment_mode = arrayListOf(BRAND_WALLET_ID),
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

    private fun getBrandWalletOrderAmount(): Int {
        val payableAmount = ExpressSDKObject.getPayableAmount() ?: ExpressSDKObject.getAmount()
        val orderAmount = if ((payableAmount ?: 0) > 0) {
            payableAmount
        } else {
            ExpressSDKObject.getAmount()
        }
        return orderAmount.coerceAtLeast(0)
    }


    private fun cancelTransactionProcess() {
        viewModel.isShowingUPIDialog = false
        bottomSheetDialog?.dismiss()
        bottomTimerSheetDialog?.dismiss()
        qrBottomSheetDialog?.dismiss()
        brandWalletOtpBottomSheetDialog?.dismiss()
        brandWalletOtpBottomSheetDialog = null
        brandWalletOtpCountdownJob?.cancel()
        brandWalletOtpCountdownJob = null
        brandWalletOtpPaymentId = null
        isBrandWalletOtpTriggerProcessPayment = false
        hasShownBrandWalletOtpSheet = false
        viewModel.stopPolling()
    }


    private fun showProcessPaymentTimerDialog() {
        viewModel.isShowingUPIDialog = true
        bottomTimerSheetDialog = BottomSheetDialog(requireContext())
        val view =
            LayoutInflater.from(requireActivity())
                .inflate(R.layout.timer_bottom_sheet_layout, null)
        val cancelPaymentTextView: TextView = view.findViewById(R.id.cancelPaymentTextView)
        val timer: TextView = view.findViewById(R.id.timerTextView)
        lifecycleScope.launch {
            viewModel.countDownTimer.collect { millisUntilFinished ->
                if (millisUntilFinished == -1L) {
                    // do nothing false trigger
                    return@collect
                } else if (millisUntilFinished == 0L) {
                    //throw error timer ended
                    cancelTransactionProcess()
                }
                val secondsRemaining = millisUntilFinished / 1000
                timer.text = String.format(
                    getString(R.string.timer_format),
                    secondsRemaining / 60,
                    secondsRemaining % 60
                )
            }
        }

        cancelPaymentTextView.setOnClickListener {
            viewModel.isShowingUPIDialog = false
            bottomTimerSheetDialog?.dismiss()
            cancelTransactionProcess()
        }
        bottomTimerSheetDialog?.setCancelable(false)
        bottomTimerSheetDialog?.setCanceledOnTouchOutside(false)
        bottomTimerSheetDialog?.setContentView(view)
        bottomTimerSheetDialog?.show() // Show the dialog first
    }

    override fun onDestroyView() {
        bottomTimerSheetDialog?.dismiss()
        qrBottomSheetDialog?.dismiss()
        brandWalletOtpBottomSheetDialog?.dismiss()
        brandWalletOtpBottomSheetDialog = null
        brandWalletOtpCountdownJob?.cancel()
        brandWalletOtpCountdownJob = null
        qrCountDownTimer?.cancel()
        super.onDestroyView()
    }

    private fun handleConvenienceFees() {
        val fetchData = ExpressSDKObject.getFetchData()
        if (fetchData?.convenienceFeesInfo.isNullOrEmpty()) {
            (requireActivity() as LandingActivity).showHideConvenienceFessMessage(false)
            return
        }
        val convenienceFeesInfo =
            fetchData?.convenienceFeesInfo?.filter { it.paymentModeType == PaymentModes.UPI.paymentModeID }
        viewModel.selectedConvenienceFee = convenienceFeesInfo?.getOrNull(0)

        if (convenienceFeesInfo.isNullOrEmpty()) {
            (requireActivity() as LandingActivity).showHideConvenienceFessMessage(false)
        } else {
            (requireActivity() as LandingActivity).showHideConvenienceFessMessage(
                true,
                viewModel.selectedConvenienceFee
            )
        }

    }

    private fun convertMapToJsonObject(yourMap: Map<*, *>): PaymentModeData {
        val gson = Gson().toJsonTree(yourMap).asJsonObject
        return Gson().fromJson(gson.toString(), PaymentModeData::class.java)

    }

    private fun getUPiFLowsList(): List<String> {
        val data = ExpressSDKObject.getFetchData()
        data?.paymentModes?.filter { paymentMode -> paymentMode.paymentModeId == PaymentModes.UPI.paymentModeID }
            ?.forEach { paymentMode ->
                when (val pm = paymentMode.paymentModeData) {
                    is LinkedTreeMap<*, *> -> {
                        val paymentModeData = convertMapToJsonObject(pm)
                        isQRAllowed =
                            paymentModeData.isMobileQRCode ?: false && paymentModeData.upi_flows?.contains(
                                "Intent"
                            ) == true
                        return paymentModeData.upi_flows ?: emptyList()
                    }
                }
            }
        return emptyList()
    }

    private fun showQRBottomSheet(deepLink: String? = null) {
        qrBottomSheetDialog = BottomSheetDialog(requireContext())
        qrBottomSheetDialog?.setCancelable(false)
        val view =
            LayoutInflater.from(requireActivity())
                .inflate(R.layout.upi_qr_layout, null)
        qrBottomSheetDialog?.setContentView(view)

        val qrImageView: ImageView = view.findViewById(R.id.qr_code_image)
        val timerText = view.findViewById<TextView>(R.id.complete_payment_timer_tv)
        val cancelPaymentBtn = view.findViewById<TextView>(R.id.cancel_qr_payment)
        cancelPaymentBtn.setOnClickListener {
            cancelTransactionProcess()
            viewModel.cancelPayment()
        }
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(deepLink, BarcodeFormat.QR_CODE, 400, 400)
            qrImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        startQRTimer(timerText)
        qrBottomSheetDialog?.show()
        qrBottomSheetDialog?.setOnDismissListener {
            cancelTransactionProcess()
        }
    }

    private var qrCountDownTimer: CountDownTimer? = null

    private fun startQRTimer(timerText: TextView) {

        qrCountDownTimer?.cancel() // cancel if already running
        val millis: Long = if (ExpressSDKObject.isSandBoxMode()) 120_000 else 600_000
        qrCountDownTimer = object : CountDownTimer(millis, 1_000) { // 60 sec, tick every 1 sec

            override fun onTick(millisUntilFinished: Long) {
                Log.d(MTAG, "Time left for QR code: $millisUntilFinished ms")
                val prefix = "QR code expires in "
                val time = Utils.formatTimeInMinutes(requireContext(), millisUntilFinished)
                val postFix = " minutes"
                val spannableBuilder = SpannableStringBuilder()

// Part 1 — normal text
                spannableBuilder.append(prefix)

// Part 2 — colored time
                val timeSpan = SpannableString(time)
                timeSpan.setSpan(
                    ForegroundColorSpan(Color.parseColor("#C96B00")),
                    0,
                    time.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannableBuilder.append(timeSpan)

                // end part

                spannableBuilder.append(postFix)

                timerText.text = spannableBuilder


            }

            override fun onFinish() {
                qrBottomSheetDialog?.dismiss()
                cancelTransactionProcess()
                safeNavigate(R.id.action_UPIFragment_to_successFragment)
            }

        }.start()
    }


    override fun onDestroy() {
        super.onDestroy()
        qrBottomSheetDialog?.dismiss()
    }


}

