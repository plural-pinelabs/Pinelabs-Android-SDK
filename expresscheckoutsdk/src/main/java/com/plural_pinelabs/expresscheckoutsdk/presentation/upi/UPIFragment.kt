package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import UpiAppsAdapter
import android.app.Dialog
import android.content.Intent
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.Editable
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
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
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.plural_pinelabs.expresscheckoutsdk.BuildConfig
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject.getAmount
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject.getCurrency
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BHIM_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CRED_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.GPAY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_ATTEMPTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_PENDING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BRAND_WALLET_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.NAVI_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAYTM
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PHONEPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SUPERMONEY_UPI
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
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentOptions
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiFetchVpaRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiFetchVpaRequestPayer
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiFetchVpaRequestPaymentOption
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiFetchVpaRequestUpiDetails
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiData
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiOfferValidateData
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiOfferValidateRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiTransactionData
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletAddMoneyLocationInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletDetails
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletResetOtpResponse
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import com.plural_pinelabs.expresscheckoutsdk.presentation.offers.OfferSummaryDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class UPIFragment : Fragment() {

    private lateinit var payByAnyUPIButton: TextView
    private lateinit var upiNoAppsAvailableText: TextView
    private lateinit var upiAppsRv: RecyclerView
    private lateinit var upiSavingsZoneSubtitle: TextView
    private lateinit var upiSavingsZoneContainer: ConstraintLayout
    private lateinit var upiSavingsZoneCta: View
    private lateinit var upiSavingsZoneAvailCta: TextView
    private lateinit var upiSavingsZoneOfferHint: TextView
    private lateinit var upiSavingsZoneAppsMoreCount: TextView
    private lateinit var viewModel: UPIViewModel
    private var mTransactionMode: String? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var qrBottomSheetDialog: BottomSheetDialog? = null
    private var bottomTimerSheetDialog: BottomSheetDialog? = null
    private var paymentTimerCollectJob: Job? = null
    private var upiIcbStatusBottomSheetDialog: BottomSheetDialog? = null
    private var upiIcbStatusFetchJob: Job? = null
    private var selectUPIPackage: String? = null
    private var usertappedapp: String? = null
    private var upiIcbStatusMobileOverride: String? = null
    private var flowMode: String? = null
    private var allInstalledUpiApps: List<String> = emptyList()
    private var isShowingAllUpiApps: Boolean = false
    private var isUpiOfferApplied: Boolean = false

    private lateinit var payByQRButton: LinearLayout
    private lateinit var payByQRLayout: ConstraintLayout
    private var isQRPayment: Boolean = false
    private var consumedDeepLink = false
    private var isQRAllowed = false
    private var isUpiIntentFlowEnabled = false
    private var brandWalletOtpBottomSheetDialog: BottomSheetDialog? = null
    private var brandWalletOtpCountdownJob: Job? = null
    private var isBrandWalletOtpTriggerProcessPayment = false
    private var hasShownBrandWalletOtpSheet = false
    private var brandWalletOtpPaymentId: String? = null
    private var brandWalletOtpResendTextView: TextView? = null
    private var brandWalletOtpStartCountdown: (() -> Unit)? = null
    private var brandWalletOtpErrorTextView: TextView? = null
    private var brandWalletOtpDraftInput: String = ""
    private var brandWalletOtpInlineErrorMessage: String? = null
    private var shouldRestoreBrandWalletOtpSheet: Boolean = false
    private var shouldRestoreBrandWalletOtpTriggerProcessPayment: Boolean = true
    private var lastUpiCtaClickTimestampMs: Long = 0L


    private lateinit var transactionLauncher: ActivityResultLauncher<Intent>

    private enum class IcbStatusState {
        LOADING,
        LINKED_VPA_FOUND,
        LINKED_VPA_FOUND_BHIM,
        NOT_ELIGIBLE,
        NO_LINKED_VPA,
    }

    private data class IcbTpapApp(
        val packageName: String,
        val displayName: String,
        @DrawableRes val iconRes: Int,
        val isBhimApp: Boolean = false,
    )

    private data class LinkedVpaStatusResult(
        val state: IcbStatusState,
        val resolvedMobile: String?,
        val resolvedVpa: String?,
        val targetTpapApp: IcbTpapApp?,
    )

    private companion object {
        const val BRAND_WALLET_PIN_LENGTH = 6
        const val BRAND_WALLET_PIN_RESEND_SECONDS = 120
        const val COLLAPSED_UPI_APPS_COUNT = 3
        const val KEY_BRAND_WALLET_OTP_SHEET_VISIBLE = "brand_wallet_otp_sheet_visible"
        const val KEY_BRAND_WALLET_OTP_DRAFT = "brand_wallet_otp_draft"
        const val KEY_BRAND_WALLET_OTP_INLINE_ERROR = "brand_wallet_otp_inline_error"
        const val KEY_BRAND_WALLET_OTP_PAYMENT_ID = "brand_wallet_otp_payment_id"
        const val KEY_BRAND_WALLET_OTP_TRIGGER_PROCESS_PAYMENT =
            "brand_wallet_otp_trigger_process_payment"
        const val KEY_BRAND_WALLET_HAS_SHOWN_OTP_SHEET = "brand_wallet_has_shown_otp_sheet"
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(
            KEY_BRAND_WALLET_OTP_SHEET_VISIBLE,
            brandWalletOtpBottomSheetDialog?.isShowing == true || shouldRestoreBrandWalletOtpSheet
        )
        outState.putString(KEY_BRAND_WALLET_OTP_DRAFT, brandWalletOtpDraftInput)
        outState.putString(KEY_BRAND_WALLET_OTP_INLINE_ERROR, brandWalletOtpInlineErrorMessage)
        outState.putString(KEY_BRAND_WALLET_OTP_PAYMENT_ID, brandWalletOtpPaymentId)
        outState.putBoolean(
            KEY_BRAND_WALLET_OTP_TRIGGER_PROCESS_PAYMENT,
            shouldRestoreBrandWalletOtpTriggerProcessPayment
        )
        outState.putBoolean(KEY_BRAND_WALLET_HAS_SHOWN_OTP_SHEET, hasShownBrandWalletOtpSheet)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreBrandWalletOtpSavedState(savedInstanceState)
        registerUpiOfferStatusResultListener()
        setViews(view)
        setUpPayByUPIApps()
        observeViewModel()
        handleConvenienceFees()
        if (shouldRestoreBrandWalletOtpSheet) {
            view.post {
                showBrandWalletOtpBottomSheet(
                    triggerProcessPayment = shouldRestoreBrandWalletOtpTriggerProcessPayment
                )
            }
        }
    }

    override fun onStop() {
        dismissProcessingOverlay()
        super.onStop()
    }

    private fun restoreBrandWalletOtpSavedState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            shouldRestoreBrandWalletOtpSheet = false
            shouldRestoreBrandWalletOtpTriggerProcessPayment = true
            return
        }

        shouldRestoreBrandWalletOtpSheet =
            savedInstanceState.getBoolean(KEY_BRAND_WALLET_OTP_SHEET_VISIBLE, false)
        brandWalletOtpDraftInput =
            savedInstanceState.getString(KEY_BRAND_WALLET_OTP_DRAFT).orEmpty()
        brandWalletOtpInlineErrorMessage =
            savedInstanceState.getString(KEY_BRAND_WALLET_OTP_INLINE_ERROR)
        brandWalletOtpPaymentId = savedInstanceState.getString(KEY_BRAND_WALLET_OTP_PAYMENT_ID)
        shouldRestoreBrandWalletOtpTriggerProcessPayment = savedInstanceState.getBoolean(
            KEY_BRAND_WALLET_OTP_TRIGGER_PROCESS_PAYMENT,
            true
        )
        hasShownBrandWalletOtpSheet =
            savedInstanceState.getBoolean(KEY_BRAND_WALLET_HAS_SHOWN_OTP_SHEET, false)
    }

    private fun registerUpiOfferStatusResultListener() {
        parentFragmentManager.setFragmentResultListener(
            OfferSummaryDialog.RESULT_KEY_UPI_ICB_ACTION,
            viewLifecycleOwner,
        ) { _, bundle ->
            val shouldShowStatusSheet = bundle.getBoolean(
                OfferSummaryDialog.RESULT_KEY_SHOW_UPI_ICB_STATUS_SHEET,
                bundle.getBoolean(OfferSummaryDialog.RESULT_KEY_TRIGGER_PAYMENT, false),
            )
            if (!shouldShowStatusSheet) {
                return@setFragmentResultListener
            }
            showUpiIcbStatusBottomSheet()
        }
    }

    private fun dismissProcessingOverlay() {
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
    }

    private fun exitBrandWalletUpiFlow() {
        if (!isAdded) return
        dismissProcessingOverlay()
        bottomTimerSheetDialog?.dismiss()
        qrBottomSheetDialog?.dismiss()
        upiIcbStatusBottomSheetDialog?.dismiss()
        brandWalletOtpBottomSheetDialog?.dismiss()
        findNavController().popBackStack()
    }

    private fun setViews(view: View) {
        payByAnyUPIButton = view.findViewById(R.id.pay_by_any_upi)
        upiNoAppsAvailableText = view.findViewById(R.id.upi_no_apps_available_text)
        upiAppsRv = view.findViewById(R.id.upi_app_rv)
        payByQRButton = view.findViewById(R.id.pay_by_qr_btn)
        payByQRLayout = view.findViewById(R.id.pay_by_qr)
        upiSavingsZoneSubtitle = view.findViewById(R.id.upi_savings_zone_subtitle)
        upiSavingsZoneContainer = view.findViewById(R.id.upi_savings_zone_container)
        upiSavingsZoneCta = view.findViewById(R.id.upi_savings_zone_cta)
        upiSavingsZoneAvailCta = view.findViewById(R.id.upi_savings_zone_avail_cta)
        upiSavingsZoneOfferHint = view.findViewById(R.id.upi_savings_zone_offer_hint)
        upiSavingsZoneAppsMoreCount = view.findViewById(R.id.upi_savings_zone_apps_more_count)

        val isBrandWalletFlow = flowMode.equals(BRAND_WALLET_ID, true)
        if (isBrandWalletFlow) {
            payByAnyUPIButton.text = getString(R.string.pay_by_existing_upi_app)
        }

        if (!BuildConfig.ENABLE_UPI_ICB_SAVINGS_UI || isBrandWalletFlow) {
            upiSavingsZoneContainer.visibility = View.GONE
        } else {
            upiSavingsZoneContainer.visibility = View.VISIBLE
            bindSavingsZoneSubtitle()
            applySavingsZoneOfferState()
            updateSavingsZoneAppsBadge()
            val openOfferDialog = View.OnClickListener {
                val offersDialog = OfferSummaryDialog.newInstance(showUpiTag = true)
                offersDialog.show(parentFragmentManager, "UpiOfferSummaryDialog")
            }
            upiSavingsZoneCta.setOnClickListener(openOfferDialog)
            upiSavingsZoneAvailCta.setOnClickListener {
                applyTopUpiSavingsZoneOffer()
            }
        }


        payByAnyUPIButton.setOnClickListener {
            if (!isUpiIntentFlowEnabled || allInstalledUpiApps.isEmpty()) {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.upi_no_apps_found_to_continue),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (shouldIgnoreRapidUpiCtaClick()) return@setOnClickListener
            isQRPayment = false
            usertappedapp = "upi intent"
            payAction(UPI_INTENT)
        }
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        val upiFlows = getUPiFLowsList()
        isUpiIntentFlowEnabled = upiFlows.any { it.equals("Intent", true) }
        if (!isUpiIntentFlowEnabled) {
            upiAppsRv.visibility = View.GONE
            payByAnyUPIButton.visibility = View.GONE
            upiNoAppsAvailableText.visibility = View.GONE
        }
        if (!isQRAllowed) {
            payByQRLayout.visibility = View.GONE
        } else {
            payByQRLayout.visibility = View.VISIBLE
        }

        payByQRButton.setOnClickListener {
            if (shouldIgnoreRapidUpiCtaClick()) return@setOnClickListener
            isQRPayment = true
            usertappedapp = "upi qr"
            payAction(UPI_INTENT_QR)
        }
    }

    private fun shouldIgnoreRapidUpiCtaClick(): Boolean {
        val now = SystemClock.elapsedRealtime()
        val shouldIgnore = now - lastUpiCtaClickTimestampMs < 500
        if (!shouldIgnore) {
            lastUpiCtaClickTimestampMs = now
        }
        return shouldIgnore
    }

    private fun bindSavingsZoneSubtitle() {
        if (!::upiSavingsZoneSubtitle.isInitialized) return
        val highestDiscount = resolveHighestUpiOfferDiscount(ExpressSDKObject.getFetchData())
        val subtitleText = if (highestDiscount != null) {
            getString(
                R.string.upi_savings_zone_subtitle_dynamic,
                Utils.convertToRupeesWithSymobl(requireContext(), highestDiscount)
            )
        } else {
            getString(R.string.upi_savings_zone_subtitle)
        }
        val amountMatch = Regex("₹\\s?[\\d,]+(?:\\.\\d+)?").find(subtitleText)
        if (amountMatch == null) {
            upiSavingsZoneSubtitle.text = subtitleText
            return
        }

        val spannableSubtitle = SpannableString(subtitleText)
        spannableSubtitle.setSpan(
            StyleSpan(Typeface.BOLD),
            amountMatch.range.first,
            amountMatch.range.last + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        upiSavingsZoneSubtitle.text = spannableSubtitle
    }

    private fun applySavingsZoneOfferState() {
        if (!::upiSavingsZoneOfferHint.isInitialized || !::upiSavingsZoneAvailCta.isInitialized) {
            return
        }

        if (isUpiOfferApplied) {
            upiSavingsZoneOfferHint.text = getString(R.string.upi_savings_zone_offer_applied_hint)
            upiSavingsZoneAvailCta.text = getString(R.string.upi_savings_zone_applied)
        } else {
            upiSavingsZoneOfferHint.text = getString(R.string.upi_savings_zone_auto_applied_hint)
            upiSavingsZoneAvailCta.text = getString(R.string.upi_savings_zone_avail)
        }
    }

    private fun applyTopUpiSavingsZoneOffer() {
        val topOffer = resolveUpiOfferDetails(ExpressSDKObject.getFetchData()).firstOrNull()
        if (topOffer == null) {
            val offersDialog = OfferSummaryDialog.newInstance(showUpiTag = true)
            offersDialog.show(parentFragmentManager, "UpiOfferSummaryDialog")
            return
        }

        ExpressSDKObject.setSelectedOfferDetail(topOffer)
        isUpiOfferApplied = true
        applySavingsZoneOfferState()
        showUpiIcbStatusBottomSheet()
    }

    private fun showUpiIcbStatusBottomSheet() {
        if (!isAdded) return

        upiIcbStatusBottomSheetDialog?.dismiss()
        val sheetDialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.upi_icb_status_bottom_sheet_layout, null)
        sheetDialog.setContentView(sheetView)
        sheetDialog.setOnDismissListener {
            upiIcbStatusFetchJob?.cancel()
            upiIcbStatusFetchJob = null
            upiIcbStatusBottomSheetDialog = null
        }
        sheetDialog.show()
        upiIcbStatusBottomSheetDialog = sheetDialog

        upiIcbStatusMobileOverride = normalizeMobileNumber(resolveCustomerMobile())
        startLinkedVpaStatusFetch(sheetView)
    }

    private fun startLinkedVpaStatusFetch(sheetView: View) {
        val requestedMobile = upiIcbStatusMobileOverride ?: normalizeMobileNumber(resolveCustomerMobile())

        bindIcbBottomSheetState(
            sheetView = sheetView,
            statusResult = LinkedVpaStatusResult(
                state = IcbStatusState.LOADING,
                resolvedMobile = requestedMobile,
                resolvedVpa = null,
                targetTpapApp = null,
            ),
        )

        upiIcbStatusFetchJob?.cancel()
        upiIcbStatusFetchJob = viewLifecycleOwner.lifecycleScope.launch {
            val statusResult = fetchLinkedVpaStatus(requestedMobile)
            if (!isAdded || upiIcbStatusBottomSheetDialog?.isShowing != true) return@launch
            bindIcbBottomSheetState(sheetView, statusResult)
        }
    }

    private suspend fun fetchLinkedVpaStatus(requestedMobileNumber: String?): LinkedVpaStatusResult {
        val resolvedMobile = requestedMobileNumber ?: normalizeMobileNumber(resolveCustomerMobile())
        if (!isValidMobileNumber(resolvedMobile.orEmpty())) {
            return LinkedVpaStatusResult(
                state = IcbStatusState.NO_LINKED_VPA,
                resolvedMobile = resolvedMobile,
                resolvedVpa = null,
                targetTpapApp = null,
            )
        }

        return when (
            val apiResult = viewModel.fetchUpiVpa(
                token = ExpressSDKObject.getToken(),
                request = createUpiFetchVpaRequest(resolvedMobile.orEmpty()),
            )
        ) {
            is BaseResult.Success -> {
                val responseData = apiResult.data.data
                val paymentOptionMetadata = responseData?.payment_option_metadata
                val fetchedVpa = listOf(
                    paymentOptionMetadata?.upi_payment_option_data?.fetched_vpa,
                    paymentOptionMetadata?.upi_payment_option_data?.vpa,
                    paymentOptionMetadata?.upi_data?.fetched_vpa,
                    paymentOptionMetadata?.upi_data?.vpa,
                ).firstNotNullOfOrNull { value ->
                    value?.trim()?.takeIf { it.isNotEmpty() }
                }

                if (fetchedVpa.isNullOrBlank()) {
                    return LinkedVpaStatusResult(
                        state = IcbStatusState.NO_LINKED_VPA,
                        resolvedMobile = resolvedMobile,
                        resolvedVpa = null,
                        targetTpapApp = null,
                    )
                }

                val targetTpapApp = resolveTpapTargetForVpa(fetchedVpa)

                if (targetTpapApp?.isBhimApp == true) {
                    LinkedVpaStatusResult(
                        state = IcbStatusState.LINKED_VPA_FOUND_BHIM,
                        resolvedMobile = resolvedMobile,
                        resolvedVpa = fetchedVpa,
                        targetTpapApp = targetTpapApp,
                    )
                } else {
                    LinkedVpaStatusResult(
                        state = IcbStatusState.LINKED_VPA_FOUND,
                        resolvedMobile = resolvedMobile,
                        resolvedVpa = fetchedVpa,
                        targetTpapApp = targetTpapApp,
                    )
                }
            }

            is BaseResult.Error -> {
                LinkedVpaStatusResult(
                    state = IcbStatusState.NO_LINKED_VPA,
                    resolvedMobile = resolvedMobile,
                    resolvedVpa = null,
                    targetTpapApp = null,
                )
            }

            is BaseResult.Loading -> {
                LinkedVpaStatusResult(
                    state = IcbStatusState.NO_LINKED_VPA,
                    resolvedMobile = resolvedMobile,
                    resolvedVpa = null,
                    targetTpapApp = null,
                )
            }
        }
    }

    private fun createUpiFetchVpaRequest(mobileNumber: String): UpiFetchVpaRequest {
        return UpiFetchVpaRequest(
            payment_method = PaymentModes.UPI.paymentModeID,
            payment_option = UpiFetchVpaRequestPaymentOption(
                upi_details = UpiFetchVpaRequestUpiDetails(
                    txn_mode = "COLLECT",
                    payer = UpiFetchVpaRequestPayer(
                        phone_number = mobileNumber,
                        fetch_vpa = true,
                    ),
                ),
            ),
        )
    }
    private fun bindIcbBottomSheetState(
        sheetView: View,
        statusResult: LinkedVpaStatusResult,
    ) {
        val statusIcon = sheetView.findViewById<ImageView>(R.id.icb_status_icon)
        val statusTitle = sheetView.findViewById<TextView>(R.id.icb_status_title)
        val statusSubtitle = sheetView.findViewById<TextView>(R.id.icb_status_subtitle)
        val statusBannerText = sheetView.findViewById<TextView>(R.id.icb_status_banner_text)
        val statusFetchingText = sheetView.findViewById<TextView>(R.id.icb_status_fetching_text)
        val statusFoundText = sheetView.findViewById<TextView>(R.id.icb_status_found_text)
        val statusFetchingLoader = sheetView.findViewById<View>(R.id.icb_status_fetching_loader)
        val statusFetchingDoneIcon =
            sheetView.findViewById<ImageView>(R.id.icb_status_fetching_done_icon)
        val statusFoundIcon = sheetView.findViewById<ImageView>(R.id.icb_status_found_icon)
        val statusFoundRow = sheetView.findViewById<View>(R.id.icb_status_found_row)
        val statusBhimNoticeRow = sheetView.findViewById<View>(R.id.icb_status_bhim_notice_row)
        val closeIcon = sheetView.findViewById<ImageView>(R.id.icb_status_close_icon)
        val continueCta = sheetView.findViewById<View>(R.id.icb_status_continue_cta)
        val continueCtaText = sheetView.findViewById<TextView>(R.id.icb_status_continue_cta_text)
        val continueCtaLogo = sheetView.findViewById<ImageView>(R.id.icb_status_continue_cta_logo)

        val resolvedMobile = statusResult.resolvedMobile ?: getString(R.string.upi_icb_status_unknown_mobile)
        val resolvedVpa = statusResult.resolvedVpa
        val resolvedTpapApp = statusResult.targetTpapApp
            ?.takeIf { !it.isBhimApp && isAppUpiReady(it.packageName) }

        statusIcon.setImageResource(R.drawable.ic_upi_logo)
        statusTitle.text = getString(R.string.upi_icb_status_pending_title)
        statusSubtitle.text = getString(R.string.upi_icb_status_pending_subtitle)
        statusBannerText.text =
            if (statusResult.state == IcbStatusState.NO_LINKED_VPA || statusResult.state == IcbStatusState.NOT_ELIGIBLE) {
                getString(R.string.upi_icb_status_pending_banner)
            } else {
                getString(R.string.upi_icb_status_success_banner)
            }
        statusFoundRow.visibility = View.VISIBLE
        statusFetchingText.text = getString(
            R.string.upi_icb_status_fetching_vpa,
            resolvedMobile,
        )

        closeIcon.setOnClickListener {
            upiIcbStatusBottomSheetDialog?.dismiss()
        }

        when (statusResult.state) {
            IcbStatusState.LOADING -> {
                statusFetchingLoader.visibility = View.VISIBLE
                statusFetchingDoneIcon.visibility = View.GONE
                statusFoundIcon.setImageResource(R.drawable.ic_upi_icb_step_unselected)
                statusFoundText.text = buildFoundVpaText(vpa = null, showValue = false)
                statusBhimNoticeRow.visibility = View.GONE
                setContinueCtaState(
                    continueCta = continueCta,
                    continueCtaText = continueCtaText,
                    continueCtaLogo = continueCtaLogo,
                    enabled = false,
                    label = getString(R.string.upi_icb_status_continue_cta),
                    logoRes = null,
                )
                continueCta.setOnClickListener(null)
            }

            IcbStatusState.LINKED_VPA_FOUND -> {
                if (!isUpiOfferApplied) {
                    isUpiOfferApplied = true
                    applySavingsZoneOfferState()
                }
                statusFetchingLoader.visibility = View.GONE
                statusFetchingDoneIcon.visibility = View.VISIBLE
                statusFoundIcon.setImageResource(R.drawable.tick)
                statusFoundText.text = buildFoundVpaText(resolvedVpa)
                statusBhimNoticeRow.visibility = View.GONE

                val ctaLabel = if (!resolvedVpa.isNullOrBlank()) {
                    getString(R.string.upi_icb_status_continue_with_vpa, resolvedVpa)
                } else if (resolvedTpapApp != null) {
                    getString(
                        R.string.upi_icb_status_continue_with_tpap,
                        resolvedTpapApp.displayName,
                    )
                } else {
                    getString(R.string.upi_icb_status_continue_cta)
                }
                setContinueCtaState(
                    continueCta = continueCta,
                    continueCtaText = continueCtaText,
                    continueCtaLogo = continueCtaLogo,
                    enabled = true,
                    label = ctaLabel,
                    logoRes = if (resolvedVpa.isNullOrBlank()) resolvedTpapApp?.iconRes else null,
                )

                continueCta.setOnClickListener {
                    startIcbUpiPayment(
                        targetPackage = resolvedTpapApp?.packageName,
                        resolvedVpa = resolvedVpa,
                        resolvedMobile = resolvedMobile,
                    )
                }
            }

            IcbStatusState.LINKED_VPA_FOUND_BHIM -> {
                if (!isUpiOfferApplied) {
                    isUpiOfferApplied = true
                    applySavingsZoneOfferState()
                }
                statusFetchingLoader.visibility = View.GONE
                statusFetchingDoneIcon.visibility = View.VISIBLE
                statusFoundIcon.setImageResource(R.drawable.tick)
                statusFoundText.text = buildFoundVpaText(resolvedVpa)
                statusBhimNoticeRow.visibility = View.VISIBLE
                setContinueCtaState(
                    continueCta = continueCta,
                    continueCtaText = continueCtaText,
                    continueCtaLogo = continueCtaLogo,
                    enabled = true,
                    label = if (!resolvedVpa.isNullOrBlank()) {
                        getString(R.string.upi_icb_status_continue_with_vpa, resolvedVpa)
                    } else {
                        getString(R.string.upi_icb_status_continue_cta)
                    },
                    logoRes = null,
                )

                continueCta.setOnClickListener {
                    // BHIM or unknown-handle flows should route to standard pay-by-any-app intent.
                    startIcbUpiPayment(
                        targetPackage = null,
                        resolvedVpa = resolvedVpa,
                        resolvedMobile = resolvedMobile,
                    )
                }
            }

            IcbStatusState.NO_LINKED_VPA -> {
                statusFetchingLoader.visibility = View.GONE
                statusFetchingDoneIcon.visibility = View.VISIBLE
                statusFoundIcon.setImageResource(R.drawable.ic_upi_icb_step_unselected)
                statusFoundText.text = getString(R.string.upi_icb_status_no_vpa_found)
                statusBhimNoticeRow.visibility = View.GONE
                setContinueCtaState(
                    continueCta = continueCta,
                    continueCtaText = continueCtaText,
                    continueCtaLogo = continueCtaLogo,
                    enabled = true,
                    label = getString(R.string.upi_icb_status_change_mobile_cta),
                    logoRes = null,
                )
                continueCta.setOnClickListener {
                    showEditMobileDialog(
                        initialMobile = resolvedMobile,
                        onContinue = { updatedMobile ->
                            upiIcbStatusMobileOverride = updatedMobile
                            startLinkedVpaStatusFetch(sheetView)
                        },
                    )
                }
            }

            IcbStatusState.NOT_ELIGIBLE -> {
                statusFetchingLoader.visibility = View.GONE
                statusFetchingDoneIcon.visibility = View.VISIBLE
                if (resolvedVpa.isNullOrBlank()) {
                    statusFoundIcon.setImageResource(R.drawable.ic_upi_icb_step_unselected)
                    statusFoundText.text = getString(R.string.upi_icb_status_no_vpa_found)
                } else {
                    statusFoundIcon.setImageResource(R.drawable.tick)
                    statusFoundText.text = buildFoundVpaText(resolvedVpa)
                }
                statusBhimNoticeRow.visibility = View.GONE
                setContinueCtaState(
                    continueCta = continueCta,
                    continueCtaText = continueCtaText,
                    continueCtaLogo = continueCtaLogo,
                    enabled = true,
                    label = getString(R.string.upi_icb_status_change_mobile_cta),
                    logoRes = null,
                )
                continueCta.setOnClickListener {
                    showEditMobileDialog(
                        initialMobile = resolvedMobile,
                        onContinue = { updatedMobile ->
                            upiIcbStatusMobileOverride = updatedMobile
                            startLinkedVpaStatusFetch(sheetView)
                        },
                    )
                }
            }
        }
    }

    private fun showEditMobileDialog(initialMobile: String?, onContinue: (String) -> Unit) {
        if (!isAdded) return

        val editDialog = Dialog(requireContext())
        editDialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        editDialog.setContentView(R.layout.upi_icb_edit_mobile_dialog_layout)
        editDialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (Resources.getSystem().displayMetrics.widthPixels * 0.92f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setGravity(android.view.Gravity.CENTER)
        }

        val closeIcon = editDialog.findViewById<ImageView>(R.id.icb_edit_mobile_close_icon)
        val mobileEditText = editDialog.findViewById<EditText>(R.id.icb_edit_mobile_number_input)
        val continueCta = editDialog.findViewById<View>(R.id.icb_edit_mobile_continue_cta)
        val continueCtaText = editDialog.findViewById<TextView>(R.id.icb_edit_mobile_continue_text)

        mobileEditText.filters = arrayOf(InputFilter.LengthFilter(10))
        val defaultMobile = normalizeMobileNumber(initialMobile) ?: ""
        mobileEditText.setText(defaultMobile)
        mobileEditText.setSelection(mobileEditText.text?.length ?: 0)

        val refreshContinueState = {
            val currentMobile = mobileEditText.text?.toString().orEmpty().trim()
            val isValid = isValidMobileNumber(currentMobile)
            setEditMobileContinueState(continueCta, continueCtaText, isValid)
        }

        refreshContinueState()

        mobileEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                refreshContinueState()
            }
        })

        closeIcon.setOnClickListener {
            editDialog.dismiss()
        }

        continueCta.setOnClickListener {
            val updatedMobile = mobileEditText.text?.toString().orEmpty().trim()
            if (!isValidMobileNumber(updatedMobile)) {
                return@setOnClickListener
            }
            onContinue(updatedMobile)
            editDialog.dismiss()
        }

        editDialog.show()
    }

    private fun setEditMobileContinueState(
        continueCta: View,
        continueCtaText: TextView,
        enabled: Boolean,
    ) {
        continueCta.isEnabled = enabled
        continueCta.isClickable = enabled
        continueCta.background = requireContext().getDrawable(
            if (enabled) {
                R.drawable.upi_icb_floating_ok_cta_bg
            } else {
                R.drawable.upi_icb_status_cta_disabled_bg
            }
        )
        continueCtaText.setTextColor(
            if (enabled) {
                Color.WHITE
            } else {
                Color.parseColor("#C0C9D2")
            }
        )
    }

    private fun isValidMobileNumber(number: String): Boolean {
        return number.length == 10 && Utils.isValidPhoneNumber(number)
    }

    private fun normalizeMobileNumber(number: String?): String? {
        val digits = number
            ?.filter { it.isDigit() }
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        return if (digits.length > 10) digits.takeLast(10) else digits
    }

    private fun startIcbUpiPayment(
        targetPackage: String?,
        resolvedVpa: String?,
        resolvedMobile: String?,
    ) {
        selectUPIPackage = targetPackage?.takeIf { isAppUpiReady(it) }
        usertappedapp = "vpa"

        viewLifecycleOwner.lifecycleScope.launch {
            if (!isAdded) return@launch

            val validateRequest = createUpiOfferValidateRequest(
                resolvedVpa = resolvedVpa,
                resolvedMobile = resolvedMobile,
            )

            if (validateRequest == null) {
                Toast.makeText(
                    requireContext(),
                    "Unable to validate UPI offer. Please try again.",
                    Toast.LENGTH_SHORT,
                ).show()
                return@launch
            }

            when (
                val validateResult = viewModel.validateUpiOffer(
                    token = ExpressSDKObject.getToken(),
                    request = validateRequest,
                )
            ) {
                is BaseResult.Success -> {
                    if (validateResult.data.code.equals("ELIGIBLE", true)) {
                        if (!isUpiOfferApplied) {
                            isUpiOfferApplied = true
                            applySavingsZoneOfferState()
                        }
                        upiIcbStatusBottomSheetDialog?.dismiss()
                        payAction(UPI_INTENT, validateRequest.upi_data)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            validateResult.data.message,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }

                is BaseResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        validateResult.errorMessage
                            ?: "Unable to validate UPI offer. Please try again.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                is BaseResult.Loading -> Unit
            }
        }
    }

    private fun createUpiOfferValidateRequest(
        resolvedVpa: String?,
        resolvedMobile: String?,
    ): UpiOfferValidateRequest? {
        val offerData = resolveSelectedUpiOfferData() ?: return null
        val paymentAmount = resolveOfferPaymentAmount(offerData) ?: return null
        val currency = getCurrency()
        val orderAmount = ExpressSDKObject.getOriginalOrderAmount().takeIf { it > 0 } ?: paymentAmount

        return UpiOfferValidateRequest(
            upi_data = UpiOfferValidateData(
                upi_option = UPI_ID,
                txn_mode = UPI_INTENT,
                vpa = resolvedVpa?.trim(),
                registered_mobile_number = normalizeMobileNumber(resolvedMobile ?: resolveCustomerMobile()),
                offer_data = offerData,
            ),
            extras = Extra(
                payment_mode = arrayListOf(UPI_ID),
                payment_amount = paymentAmount,
                payment_currency = currency,
                card_last4 = null,
                redeemable_amount = null,
                registered_mobile_number = null,
                txn_mode = null,
                device_info = null,
                risk_validation_details = null,
                dcc_status = null,
                sdk_data = null,
                order_amount = orderAmount,
                language = null,
                is_final_part_payment = null,
                location_info = null,
                order_currency = currency,
            ),
        )
    }

    private fun resolveSelectedUpiOfferData(): Any? {
        val rawOfferData = resolveRawUpiOfferData() ?: return null
        val selectedOffer = ExpressSDKObject.getSelectedOfferDetail() ?: return null
        val entities = (rawOfferData["entities"] as? List<*>)
            ?.mapNotNull { it as? Map<*, *> }
            .orEmpty()
        if (entities.isEmpty()) return null

        val selectedEntity = selectOfferEntity(
            entities = entities,
            selectedEntityId = selectedOffer.issuerId,
            selectedName = selectedOffer.name,
        ) ?: return null

        val selectedTenure = selectOfferTenure(
            entity = selectedEntity,
            selectedTenureId = selectedOffer.tenureOffers?.firstOrNull()?.tenureId,
        ) ?: return null

        val entityMap = selectedEntity.asStringKeyMap().toMutableMap()
        val tenureMap = selectedTenure.asStringKeyMap().toMutableMap()
        val selectedOfferRanking = resolveSelectedOfferRanking(selectedOffer)
        selectRankedOffer(selectedTenure, selectedOfferRanking)?.let { rankedOffer ->
            tenureMap["offers"] = listOf(rankedOffer.asStringKeyMap())
        }
        entityMap["tenures"] = listOf(tenureMap)

        return mapOf("entities" to listOf(entityMap))
    }

    private fun resolveRawUpiOfferData(): Map<*, *>? {
        val upiModeData = ExpressSDKObject.getFetchData()
            ?.paymentModes
            ?.firstOrNull { it.paymentModeId.equals(PaymentModes.UPI.paymentModeID, true) }
            ?.paymentModeData as? Map<*, *>
            ?: return null

        return (upiModeData["offerData"] ?: upiModeData["offer_data"]) as? Map<*, *>
    }

    private fun selectOfferEntity(
        entities: List<Map<*, *>>,
        selectedEntityId: String?,
        selectedName: String?,
    ): Map<*, *>? {
        selectedEntityId?.let { entityId ->
            entities.firstOrNull {
                it["entity_id"]?.toString()?.equals(entityId, true) == true
            }?.let { return it }
        }

        selectedName?.let { offerName ->
            entities.firstOrNull {
                it["entity_name"]?.toString()?.equals(offerName, true) == true ||
                    it["entity_display_name"]?.toString()?.equals(offerName, true) == true
            }?.let { return it }
        }

        return entities.firstOrNull()
    }

    private fun selectOfferTenure(
        entity: Map<*, *>,
        selectedTenureId: String?,
    ): Map<*, *>? {
        val tenures = (entity["tenures"] as? List<*>)
            ?.mapNotNull { it as? Map<*, *> }
            .orEmpty()
        if (tenures.isEmpty()) return null

        return selectedTenureId?.let { tenureId ->
            tenures.firstOrNull {
                it["tenure_id"]?.toString()?.equals(tenureId, true) == true
            }
        } ?: tenures.firstOrNull()
    }

    private fun resolveSelectedOfferRanking(selectedOffer: com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetail): Int? {
        val programType = selectedOffer.tenureOffers
            ?.firstOrNull()
            ?.offers
            ?.firstOrNull()
            ?.programType
            ?: return null

        return programType
            .substringAfter("offer_ranking_", missingDelimiterValue = "")
            .toIntOrNull()
    }

    private fun selectRankedOffer(
        tenure: Map<*, *>,
        selectedRanking: Int?,
    ): Map<*, *>? {
        val offers = (tenure["offers"] as? List<*>)
            ?.mapNotNull { it as? Map<*, *> }
            .orEmpty()
        if (offers.isEmpty()) return null

        selectedRanking?.let { ranking ->
            offers.firstOrNull {
                it["offer_ranking"].toIntValue() == ranking
            }?.let { return it }
        }

        return offers.minByOrNull {
            it["offer_ranking"].toIntValue() ?: Int.MAX_VALUE
        }
    }

    private fun Any?.toIntValue(): Int? {
        return when (this) {
            is Number -> this.toInt()
            is String -> this.toIntOrNull()
            else -> null
        }
    }

    private fun Map<*, *>.asStringKeyMap(): Map<String, Any?> {
        val parsedMap = LinkedHashMap<String, Any?>()
        for ((key, value) in this) {
            if (key is String) {
                parsedMap[key] = value
            }
        }
        return parsedMap
    }

    private fun resolveOfferPaymentAmount(offerData: Any?): Int? {
        val offer = (((offerData as? Map<*, *>)?.get("entities") as? List<*>)
            ?.mapNotNull { it as? Map<*, *> }
            ?.firstOrNull()
            ?.get("tenures") as? List<*>)
            ?.mapNotNull { it as? Map<*, *> }
            ?.firstOrNull()
            ?.get("offers")
            ?.let { it as? List<*> }
            ?.mapNotNull { it as? Map<*, *> }
            ?.firstOrNull()
            ?: return null

        return offer["auth_amount"].extractAmountValue()
            ?: offer["loan_amount"].extractAmountValue()
            ?: offer["net_payment_amount"].extractAmountValue()
    }

    private fun Any?.extractAmountValue(): Int? {
        val amountMap = this as? Map<*, *> ?: return null
        return amountMap["value"].toIntValue()
    }

    private fun setContinueCtaState(
        continueCta: View,
        continueCtaText: TextView,
        continueCtaLogo: ImageView,
        enabled: Boolean,
        label: String,
        @DrawableRes logoRes: Int?,
    ) {
        continueCta.isEnabled = enabled
        continueCta.isClickable = enabled
        continueCta.isFocusable = enabled
        continueCta.background = requireContext().getDrawable(
            if (enabled) {
                R.drawable.upi_icb_floating_ok_cta_bg
            } else {
                R.drawable.upi_icb_status_cta_disabled_bg
            }
        )

        continueCtaText.text = label
        continueCtaText.setTextColor(
            if (enabled) {
                Color.WHITE
            } else {
                Color.parseColor("#C0C9D2")
            }
        )

        if (enabled && logoRes != null) {
            continueCtaLogo.setImageResource(logoRes)
            continueCtaLogo.visibility = View.VISIBLE
        } else {
            continueCtaLogo.visibility = View.GONE
        }
    }

    private fun buildFoundVpaText(vpa: String?, showValue: Boolean = true): CharSequence {
        val prefix = getString(R.string.upi_icb_status_found_vpa_prefix)
        if (!showValue) {
            return prefix
        }

        val value = vpa ?: getString(R.string.upi_icb_status_unknown_vpa)
        return SpannableStringBuilder().apply {
            append(prefix)
            val start = length
            append(value)
            setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            setSpan(
                ForegroundColorSpan(Color.parseColor("#003323")),
                start,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    private fun resolveCustomerMobile(): String? {
        val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
        return upiIcbStatusMobileOverride
            ?: customerInfo?.mobile_number
            ?: customerInfo?.mobileNo
            ?: customerInfo?.mobileNumber
    }

    private fun resolveTpapTargetForVpa(vpa: String?): IcbTpapApp? {
        val normalizedHandle = normalizeVpaHandle(vpa)
            ?: return null

        resolveTpapTargetFromMapper(normalizedHandle)?.let { return it }
        return resolveTpapTargetFromFallback(normalizedHandle)
    }

    private fun resolveTpapTargetFromMapper(normalizedHandle: String): IcbTpapApp? {
        val vpaMapper = ExpressSDKObject.getFetchData()?.vpaMapper ?: return null
        val mappedAppName = vpaMapper.entries
            .firstOrNull { entry ->
                entry.value.any { mapperHandle ->
                    normalizeVpaHandle(mapperHandle) == normalizedHandle
                }
            }
            ?.key
            ?: return null

        return mapAppNameToTpapTarget(mappedAppName)
    }

    private fun resolveTpapTargetFromFallback(normalizedHandle: String): IcbTpapApp? {
        return when (normalizedHandle) {
            "upi" -> IcbTpapApp(
                packageName = BHIM_UPI,
                displayName = getString(R.string.upi_app_bhim_upi).trim(),
                iconRes = R.drawable.ic_bhim_upi,
                isBhimApp = true,
            )

            "ybl", "ibl", "axl" -> IcbTpapApp(
                packageName = PHONEPE,
                displayName = getString(R.string.upi_app_phonepe),
                iconRes = R.drawable.ic_phone_pe,
            )

            "okaxis", "okhdfcbank", "oksbi", "okicici", "okbizaxis", "okaxisc", "axisb" -> IcbTpapApp(
                packageName = GPAY,
                displayName = getString(R.string.upi_app_google_pay),
                iconRes = R.drawable.ic_gpay_upi,
            )

            "paytm", "ptaxis", "ptyes", "pthdfc", "pticici", "ptsbi", "ptaxisb" -> IcbTpapApp(
                packageName = PAYTM,
                displayName = getString(R.string.upi_app_paytm),
                iconRes = R.drawable.ic_paytm_upi,
            )

            "cred" -> IcbTpapApp(
                packageName = CRED_UPI,
                displayName = getString(R.string.upi_app_cred),
                iconRes = R.drawable.ic_cred_upi,
            )

            "navi" -> IcbTpapApp(
                packageName = NAVI_UPI,
                displayName = getString(R.string.upi_app_navi),
                iconRes = R.drawable.ic_navi_upi,
            )

            "supermoney", "super.money", "superm" -> IcbTpapApp(
                packageName = SUPERMONEY_UPI,
                displayName = getString(R.string.upi_app_supermoney),
                iconRes = R.drawable.ic_supermoney_upi,
            )

            else -> null
        }
    }

    private fun mapAppNameToTpapTarget(appName: String): IcbTpapApp? {
        return when (appName.trim().lowercase()) {
            "phonepe", "phone pe" -> IcbTpapApp(
                packageName = PHONEPE,
                displayName = getString(R.string.upi_app_phonepe),
                iconRes = R.drawable.ic_phone_pe,
            )

            "gpay", "google pay", "googlepay" -> IcbTpapApp(
                packageName = GPAY,
                displayName = getString(R.string.upi_app_google_pay),
                iconRes = R.drawable.ic_gpay_upi,
            )

            "paytm" -> IcbTpapApp(
                packageName = PAYTM,
                displayName = getString(R.string.upi_app_paytm),
                iconRes = R.drawable.ic_paytm_upi,
            )

            "cred" -> IcbTpapApp(
                packageName = CRED_UPI,
                displayName = getString(R.string.upi_app_cred),
                iconRes = R.drawable.ic_cred_upi,
            )

            "navi" -> IcbTpapApp(
                packageName = NAVI_UPI,
                displayName = getString(R.string.upi_app_navi),
                iconRes = R.drawable.ic_navi_upi,
            )

            "supermoney", "super money" -> IcbTpapApp(
                packageName = SUPERMONEY_UPI,
                displayName = getString(R.string.upi_app_supermoney),
                iconRes = R.drawable.ic_supermoney_upi,
            )

            "bhim", "upi" -> IcbTpapApp(
                packageName = BHIM_UPI,
                displayName = getString(R.string.upi_app_bhim_upi).trim(),
                iconRes = R.drawable.ic_bhim_upi,
                isBhimApp = true,
            )

            else -> null
        }
    }

    private fun normalizeVpaHandle(value: String?): String? {
        val handle = value
            ?.substringAfter('@', missingDelimiterValue = value)
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase()
            ?.removePrefix("@")
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return handle
    }

    private fun setUpPayByUPIApps() {
        if (!isUpiIntentFlowEnabled) {
            payByAnyUPIButton.visibility = View.GONE
            upiAppsRv.visibility = View.GONE
            upiNoAppsAvailableText.visibility = View.GONE
            return
        }

        allInstalledUpiApps = getUpiAppsInstalledInDevice()
        isShowingAllUpiApps = false
        updateSavingsZoneAppsBadge()
        if (!allInstalledUpiApps.contains(selectUPIPackage)) {
            selectUPIPackage = null
        }

        if (allInstalledUpiApps.isNotEmpty()) {
            payByAnyUPIButton.visibility = View.VISIBLE
            upiAppsRv.visibility = View.VISIBLE
            upiNoAppsAvailableText.visibility = View.GONE
            upiAppsRv.layoutManager = GridLayoutManager(requireContext(), 2)
            renderUpiApps()
        } else {
            upiAppsRv.visibility = View.GONE
            upiNoAppsAvailableText.visibility = View.VISIBLE
            payByAnyUPIButton.visibility =
                if (flowMode.equals(BRAND_WALLET_ID, true)) View.GONE else View.VISIBLE
            selectUPIPackage = null
        }
    }

    private fun updateSavingsZoneAppsBadge() {
        if (!::upiSavingsZoneAppsMoreCount.isInitialized) return
        if (allInstalledUpiApps.isEmpty()) {
            upiSavingsZoneAppsMoreCount.text = getString(R.string.upi_savings_zone_apps_more_default)
            return
        }
        val remainingAppsCount = (allInstalledUpiApps.size - 1).coerceAtLeast(1)
        upiSavingsZoneAppsMoreCount.text = getString(
            R.string.upi_savings_zone_apps_more_count,
            remainingAppsCount
        )
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
                usertappedapp = resolveTappedUpiAppName(item)
                payAction(UPI_INTENT)
            }
        }
    }

    private fun resolveTappedUpiAppName(packageName: String): String {
        return when (packageName.trim().lowercase()) {
            GPAY -> "google pay"
            PHONEPE -> "phone pe"
            PAYTM -> "paytm"
            BHIM_UPI -> "bhim upi"
            CRED_UPI -> "cred"
            NAVI_UPI -> "navi"
            SUPERMONEY_UPI -> "supermoney"
            else -> packageName.substringAfterLast('.')
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

    private fun showUpiTray(deepLink: String, upiAppPackageName: String?): Boolean {
        if (deepLink.isBlank()) {
            Toast.makeText(
                requireActivity(),
                "Unable to launch UPI app. Please try again.",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val upiPayIntent = Intent(Intent.ACTION_VIEW, deepLink.toUri())
        val targetPackage = upiAppPackageName?.takeIf { it.isNotBlank() }
        if (targetPackage != null) {
            upiPayIntent.`package` = targetPackage
        }

        val canHandleIntent = requireActivity()
            .packageManager
            .queryIntentActivities(upiPayIntent, 0)
            .isNotEmpty()

        if (!canHandleIntent) {
            Toast.makeText(
                requireActivity(),
                getString(R.string.upi_no_apps_found_to_continue),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val launchIntent = if (targetPackage == null) {
            Intent.createChooser(upiPayIntent, getString(R.string.upi_open_with))
        } else {
            upiPayIntent
        }

        return runCatching {
            transactionLauncher.launch(launchIntent)
        }.fold(
            onSuccess = { true },
            onFailure = {
                Toast.makeText(
                    requireActivity(),
                    "Unable to launch UPI app. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
        )
    }

    private fun payAction(
        transactionMode: String?,
        validatedUpiData: UpiOfferValidateData? = null,
    ) {
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
                val didLaunchUpiApp =
                    showUpiTray(existingDeepLink, upiAppPackageName = selectUPIPackage)
                if (!didLaunchUpiApp) {
                    exitBrandWalletUpiFlow()
                    return
                }
                if (bottomTimerSheetDialog?.isShowing != true) {
                    showProcessPaymentTimerDialog()
                    viewModel.startCountDownTimer()
                }
                viewModel.startPolling(inquiryOrderId)
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
        val transactionModeValue = when (transactionMode) {
            UPI_INTENT -> UPI_INTENT
            UPI_INTENT_QR -> UPI_INTENT
            else -> UPI_INTENT
        }
        val paymentAmount = (
            resolveOfferPaymentAmount(validatedUpiData?.offer_data)
                ?: getAmount().takeIf { it > 0 }
                ?: ExpressSDKObject.getOriginalOrderAmount().takeIf { it > 0 }
            ) ?: getAmount()
        val currency = getCurrency()
        val orderAmount = ExpressSDKObject.getOriginalOrderAmount().takeIf { it > 0 } ?: paymentAmount
        val resolvedOrderId = ExpressSDKObject.getFetchData()?.transactionInfo?.orderId
        val resolvedRegisteredMobile = normalizeMobileNumber(
            validatedUpiData?.registered_mobile_number ?: resolveCustomerMobile()
        )

        val paymentMode = arrayListOf(UPI_ID)
        val extra = Extra(
            payment_mode = paymentMode,
            payment_amount = paymentAmount,
            payment_currency = currency,
            card_last4 = null,
            redeemable_amount = null,
            registered_mobile_number = null,
            txn_mode = null,
            device_info = null,
            risk_validation_details = null,
            dcc_status = null,
                sdk_data = Utils.createSDKData(requireActivity(), usertappedapp),
            order_amount = orderAmount,
            is_final_part_payment = true,
            location_info = WalletAddMoneyLocationInfo(),
            order_currency = currency,
            order_id = resolvedOrderId,
        )
        val upiData = UpiData(
            upi_option = validatedUpiData?.upi_option ?: UPI_ID,
            vpa = validatedUpiData?.vpa,
            txn_mode = validatedUpiData?.txn_mode ?: transactionModeValue,
            registered_mobile_number = if (validatedUpiData != null) resolvedRegisteredMobile else null,
            offer_data = validatedUpiData?.offer_data,
        )
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
    }

    private fun initProcessPayment(processPaymentRequest: ProcessPaymentRequest) {
        viewModel.processPayment(
            token = ExpressSDKObject.getToken(),
            paymentData = processPaymentRequest
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.processPaymentResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            if (isBrandWalletOtpTriggerProcessPayment) {
                                isBrandWalletOtpTriggerProcessPayment = false
                                bottomSheetDialog?.dismiss()
                                viewModel.resetPaymentFlowResponse()
                                showBrandWalletOtpInlineError()
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
                                shouldRestoreBrandWalletOtpTriggerProcessPayment = false
                                ExpressSDKObject.setProcessPaymentResponse(it.data)
                                brandWalletOtpPaymentId = it.data.payment_id
                                bottomSheetDialog?.dismiss()
                                viewModel.resetPaymentFlowResponse()
                                return@collect
                            }
                            bottomSheetDialog?.dismiss()
                            val didLaunchUpiApp =
                                if (mTransactionMode == UPI_INTENT) {
                                    showUpiTray(
                                        it.data.deep_link ?: "",
                                        upiAppPackageName = selectUPIPackage
                                    )
                                } else if (mTransactionMode == UPI_INTENT_QR && isQRPayment) {
                                    viewModel.startPolling()
                                    true
                                } else {
                                    showUpiTray(
                                        it.data.deep_link ?: "",
                                        upiAppPackageName = selectUPIPackage
                                    )
                                }

                            if (!didLaunchUpiApp) {
                                if (flowMode.equals(BRAND_WALLET_ID, true)) {
                                    exitBrandWalletUpiFlow()
                                } else {
                                    cancelTransactionProcess()
                                }
                                viewModel.resetPaymentFlowResponse()
                                return@collect
                            }
                            bottomSheetDialog?.dismiss()
                            ExpressSDKObject.setProcessPaymentResponse(it.data)
                            viewModel.resetPaymentFlowResponse()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitOtpResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            viewModel.resetSubmitOtpState()
                            bottomSheetDialog?.dismiss()
                            showBrandWalletOtpInlineError()
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading) {
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())
                            }
                        }

                        is BaseResult.Success<OTPResponse> -> {
                            viewModel.resetSubmitOtpState()
                            brandWalletOtpDraftInput = ""
                            clearBrandWalletOtpInlineError()
                            cancelTransactionProcess()
                            safeNavigate(R.id.action_UPIFragment_to_successFragment)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resetWalletOtpResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            viewModel.resetWalletOtpState()
                            bottomSheetDialog?.dismiss()
                            brandWalletOtpResendTextView?.text =
                                getString(R.string.brand_wallet_add_money_resend_pin)
                            brandWalletOtpResendTextView?.isEnabled = true
                            showBrandWalletOtpInlineError()
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading) {
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())
                            }
                        }

                        is BaseResult.Success<WalletResetOtpResponse> -> {
                            viewModel.resetWalletOtpState()
                            bottomSheetDialog?.dismiss()
                            if (it.data.success == true) {
                                clearBrandWalletOtpInlineError()
                                brandWalletOtpStartCountdown?.invoke()
                            } else {
                                brandWalletOtpResendTextView?.text =
                                    getString(R.string.brand_wallet_add_money_resend_pin)
                                brandWalletOtpResendTextView?.isEnabled = true
                                showBrandWalletOtpInlineError()
                            }
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

    private fun showBrandWalletOtpInlineError() {
        if (brandWalletOtpBottomSheetDialog?.isShowing != true) return
        val message = getString(R.string.brand_wallet_otp_inline_error)
        brandWalletOtpInlineErrorMessage = message
        brandWalletOtpErrorTextView?.text = message
        brandWalletOtpErrorTextView?.visibility = View.VISIBLE
    }

    private fun clearBrandWalletOtpInlineError() {
        brandWalletOtpInlineErrorMessage = null
        brandWalletOtpErrorTextView?.text = ""
        brandWalletOtpErrorTextView?.visibility = View.GONE
    }

    private fun showBrandWalletOtpBottomSheet(triggerProcessPayment: Boolean = true) {
        if (!isAdded) return
        if (brandWalletOtpBottomSheetDialog?.isShowing == true) return

        shouldRestoreBrandWalletOtpSheet = true
        shouldRestoreBrandWalletOtpTriggerProcessPayment = triggerProcessPayment
        hasShownBrandWalletOtpSheet = true

        brandWalletOtpCountdownJob?.cancel()
        brandWalletOtpCountdownJob = null
        brandWalletOtpBottomSheetDialog?.dismiss()
        brandWalletOtpBottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.brand_wallet_add_money_otp_bottom_sheet, null)

        val otpInput = view.findViewById<EditText>(R.id.brand_wallet_add_money_otp_input)
        val resendText = view.findViewById<TextView>(R.id.brand_wallet_add_money_otp_resend)
        val errorText = view.findViewById<TextView>(R.id.brand_wallet_add_money_otp_error)
        val ctaButton = view.findViewById<Button>(R.id.brand_wallet_add_money_otp_cta)
        ctaButton.text = if (triggerProcessPayment) {
            getString(R.string.brand_wallet_verify_and_add)
        } else {
            getString(R.string.brand_wallet_verify_and_pay)
        }

        brandWalletOtpErrorTextView = errorText
        otpInput.filters = arrayOf(InputFilter.LengthFilter(BRAND_WALLET_PIN_LENGTH))
        if (brandWalletOtpDraftInput.isNotBlank()) {
            otpInput.setText(brandWalletOtpDraftInput)
            otpInput.setSelection(brandWalletOtpDraftInput.length)
        }
        if (brandWalletOtpInlineErrorMessage.isNullOrBlank()) {
            clearBrandWalletOtpInlineError()
        } else {
            showBrandWalletOtpInlineError()
        }
        Utils.handleCTAEnableDisable(
            requireContext(),
            (otpInput.text?.length ?: 0) == BRAND_WALLET_PIN_LENGTH,
            ctaButton
        )

        otpInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                brandWalletOtpDraftInput = s?.toString().orEmpty()
                clearBrandWalletOtpInlineError()
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

        brandWalletOtpResendTextView = resendText
        brandWalletOtpStartCountdown = { startPinCountdown() }

        resendText.setOnClickListener {
            if (!resendText.isEnabled) return@setOnClickListener
            val customerId = getBrandWalletCustomerId()
            if (customerId.isNullOrBlank()) {
                showBrandWalletOtpInlineError()
                return@setOnClickListener
            }
            otpInput.text?.clear()
            brandWalletOtpDraftInput = ""
            clearBrandWalletOtpInlineError()
            Utils.handleCTAEnableDisable(requireContext(), false, ctaButton)
            resendText.isEnabled = false
            viewModel.resetWalletOtpState()
            viewModel.resetWalletOtp(
                token = ExpressSDKObject.getToken(),
                customerId = customerId,
            )
        }
        ctaButton.setOnClickListener {
            if ((otpInput.text?.length ?: 0) != BRAND_WALLET_PIN_LENGTH) {
                return@setOnClickListener
            }
            val paymentId =
                brandWalletOtpPaymentId ?: ExpressSDKObject.getProcessPaymentResponse()?.payment_id
            if (paymentId.isNullOrBlank()) {
                showBrandWalletOtpInlineError()
                return@setOnClickListener
            }
            clearBrandWalletOtpInlineError()
            brandWalletOtpDraftInput = otpInput.text?.toString().orEmpty()
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
            brandWalletOtpResendTextView = null
            brandWalletOtpStartCountdown = null
            brandWalletOtpErrorTextView = null
            shouldRestoreBrandWalletOtpSheet = false
            shouldRestoreBrandWalletOtpTriggerProcessPayment = true
            brandWalletOtpDraftInput = ""
            brandWalletOtpInlineErrorMessage = null
            brandWalletOtpBottomSheetDialog = null
        }
        brandWalletOtpBottomSheetDialog?.show()

        if (triggerProcessPayment) {
            isBrandWalletOtpTriggerProcessPayment = true
            brandWalletOtpPaymentId = null
            viewModel.processPayment(
                token = ExpressSDKObject.getToken(),
                paymentData = createBrandWalletOtpProcessPaymentRequest(),
            )
        }
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

    private fun getBrandWalletCustomerId(): String? {
        val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
        return customerInfo?.customer_id ?: customerInfo?.customerId
    }


    private fun cancelTransactionProcess() {
        viewModel.isShowingUPIDialog = false
        viewModel.stopCountDownTimer()
        paymentTimerCollectJob?.cancel()
        paymentTimerCollectJob = null
        dismissProcessingOverlay()
        bottomTimerSheetDialog?.dismiss()
        qrBottomSheetDialog?.dismiss()
        upiIcbStatusBottomSheetDialog?.dismiss()
        upiIcbStatusBottomSheetDialog = null
        upiIcbStatusFetchJob?.cancel()
        upiIcbStatusFetchJob = null
        brandWalletOtpBottomSheetDialog?.dismiss()
        brandWalletOtpBottomSheetDialog = null
        brandWalletOtpCountdownJob?.cancel()
        brandWalletOtpCountdownJob = null
        brandWalletOtpPaymentId = null
        brandWalletOtpResendTextView = null
        brandWalletOtpStartCountdown = null
        brandWalletOtpErrorTextView = null
        brandWalletOtpDraftInput = ""
        brandWalletOtpInlineErrorMessage = null
        shouldRestoreBrandWalletOtpSheet = false
        shouldRestoreBrandWalletOtpTriggerProcessPayment = true
        isBrandWalletOtpTriggerProcessPayment = false
        hasShownBrandWalletOtpSheet = false
        upiIcbStatusMobileOverride = null
        viewModel.stopPolling()
    }


    private fun showProcessPaymentTimerDialog() {
        viewModel.isShowingUPIDialog = true
        paymentTimerCollectJob?.cancel()
        val sheetDialog = BottomSheetDialog(requireContext())
        bottomTimerSheetDialog = sheetDialog
        val view =
            LayoutInflater.from(requireActivity())
                .inflate(R.layout.timer_bottom_sheet_layout, null)
        val cancelPaymentTextView: TextView = view.findViewById(R.id.cancelPaymentTextView)
        val timer: TextView = view.findViewById(R.id.timerTextView)
        val circularProgressBar: ProgressBar = view.findViewById(R.id.circularProgressBar)
        circularProgressBar.max = UPIViewModel.PAYMENT_TIMER_TOTAL_MILLIS.toInt()
        updatePaymentTimerProgress(circularProgressBar, UPIViewModel.PAYMENT_TIMER_TOTAL_MILLIS)
        val timerCollectJob = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.countDownTimer.collect { millisUntilFinished ->
                if (millisUntilFinished == -1L) {
                    // do nothing false trigger
                    return@collect
                }
                updatePaymentTimerProgress(circularProgressBar, millisUntilFinished)
                val secondsRemaining = millisUntilFinished / 1000
                timer.text = String.format(
                    getString(R.string.timer_format),
                    secondsRemaining / 60,
                    secondsRemaining % 60
                )
                if (millisUntilFinished == 0L) {
                    //throw error timer ended
                    cancelTransactionProcess()
                }
            }
        }
        paymentTimerCollectJob = timerCollectJob

        cancelPaymentTextView.setOnClickListener {
            viewModel.isShowingUPIDialog = false
            cancelTransactionProcess()
        }
        sheetDialog.setOnDismissListener {
            if (bottomTimerSheetDialog === sheetDialog) {
                bottomTimerSheetDialog = null
                viewModel.isShowingUPIDialog = false
                viewModel.stopCountDownTimer()
            }
            if (paymentTimerCollectJob === timerCollectJob) {
                timerCollectJob.cancel()
                paymentTimerCollectJob = null
            }
        }
        sheetDialog.setCancelable(false)
        sheetDialog.setCanceledOnTouchOutside(false)
        sheetDialog.setContentView(view)
        sheetDialog.show() // Show the dialog first
    }

    private fun updatePaymentTimerProgress(progressBar: ProgressBar, millisUntilFinished: Long) {
        val remainingMillis = millisUntilFinished.coerceIn(0L, UPIViewModel.PAYMENT_TIMER_TOTAL_MILLIS)
        progressBar.progress = remainingMillis.toInt()
    }

    override fun onDestroyView() {
        dismissProcessingOverlay()
        viewModel.stopCountDownTimer()
        paymentTimerCollectJob?.cancel()
        paymentTimerCollectJob = null
        bottomTimerSheetDialog?.dismiss()
        qrBottomSheetDialog?.dismiss()
        upiIcbStatusBottomSheetDialog?.dismiss()
        upiIcbStatusBottomSheetDialog = null
        upiIcbStatusFetchJob?.cancel()
        upiIcbStatusFetchJob = null
        upiIcbStatusMobileOverride = null
        brandWalletOtpBottomSheetDialog?.dismiss()
        brandWalletOtpBottomSheetDialog = null
        brandWalletOtpCountdownJob?.cancel()
        brandWalletOtpCountdownJob = null
        brandWalletOtpResendTextView = null
        brandWalletOtpStartCountdown = null
        brandWalletOtpErrorTextView = null
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

    private fun getUPiFLowsList(): List<String> {
        val paymentModeData = resolveUpiPaymentModeData(ExpressSDKObject.getFetchData())
        val upiFlows = paymentModeData?.upi_flows ?: emptyList()
        isQRAllowed = (paymentModeData?.isMobileQRCode == true) && upiFlows.contains("Intent")
        return upiFlows
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

