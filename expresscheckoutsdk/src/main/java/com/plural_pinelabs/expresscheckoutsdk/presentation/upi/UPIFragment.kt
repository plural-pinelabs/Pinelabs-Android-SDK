package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import UpiAppsAdapter
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject.getAmount
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject.getCurrency
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BHIM_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CRED_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.GPAY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAYTM
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PHONEPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_ATTEMPTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_PENDING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_COLLECT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_INTENT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_INTENT_PREFIX
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.UPIViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiData
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiTransactionData
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import kotlinx.coroutines.launch


class UPIFragment : Fragment() {

    private lateinit var payByAnyUPIButton: TextView
    private lateinit var upiAppsRv: RecyclerView
    private lateinit var upiIdEt: EditText
    private lateinit var verifyContinueButton: Button
    private lateinit var errorinfoTextView: TextView
    private val UPI_REGEX = Regex("^[\\w.\\-]+@[a-zA-Z0-9]{2,}$")
    private lateinit var viewModel: UPIViewModel
    private var mTransactionMode: String? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bottomVPASheetDialog: BottomSheetDialog? = null
    private var bottomTimerSheetDialog: BottomSheetDialog? = null
    private var selectUPIPackage: String? = null


    private lateinit var transactionLauncher: ActivityResultLauncher<Intent>

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
            showProcessPaymentTimerDialog()
            viewModel.startCountDownTimer()
            viewModel.startPolling()
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
        setupUPIIdValidation()
        observeViewModel()
        handleConvenienceFees()
    }

    private fun setViews(view: View) {
        payByAnyUPIButton = view.findViewById(R.id.pay_by_any_upi)
        upiAppsRv = view.findViewById(R.id.upi_app_rv)
        verifyContinueButton = view.findViewById(R.id.verify_and_continueButton)
        upiIdEt = view.findViewById(R.id.upi_id_edittext)
        errorinfoTextView = view.findViewById(R.id.error_upi_id)

        payByAnyUPIButton.setOnClickListener {
            payAction(null, UPI_INTENT)
        }
        verifyContinueButton.setOnClickListener {
            payAction(upiIdEt.text.toString(), UPI_COLLECT)
        }
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        val upiPaymentMode = getUPiFLowsList().joinToString(",")
        if (!upiPaymentMode.contains("Intent", true)) {
            upiAppsRv.visibility = View.GONE
            payByAnyUPIButton.visibility = View.GONE
        }
        if (!upiPaymentMode.contains("Collect", true))
            upiIdEt.visibility = View.GONE
    }

    private fun setUpPayByUPIApps() {
        val installedUPIApps = getUpiAppsInstalledInDevice()
        if (installedUPIApps.isNotEmpty()) {
            payByAnyUPIButton.visibility = View.VISIBLE
            upiAppsRv.visibility = View.VISIBLE
            upiAppsRv.layoutManager = GridLayoutManager(requireContext(), 2)
            upiAppsRv.adapter = UpiAppsAdapter(installedUPIApps, getItemClickListenerForUPIApp())

        } else {
            upiAppsRv.visibility = View.GONE
        }
    }

    private fun getItemClickListenerForUPIApp(): ItemClickListener<String> {
        return object : ItemClickListener<String> {
            override fun onItemClick(position: Int, item: String) {
                selectUPIPackage = item
                payAction(null, UPI_INTENT)
            }
        }
    }

    private fun getUpiAppsInstalledInDevice(): List<String> {
        val listOfUPIPackage = listOf(
            GPAY, // Google Pay
            PHONEPE, // PhonePe
            PAYTM,
            CRED_UPI,
            BHIM_UPI// Paytm
        )
        val listOfPaymentReadyApps = mutableListOf<String>()
        listOfPaymentReadyApps.addAll(getListOfActiveUPIApps(listOfUPIPackage))
        return listOfPaymentReadyApps
        // return listOfUPIPackage // only for testing purposes
    }

    private fun isAppUpiReady(packageName: String): Boolean {
        var appUpiReady = false
        val upiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(UPI_INTENT_PREFIX))
        val pm = requireActivity().packageManager
        val upiActivities: List<ResolveInfo> = pm.queryIntentActivities(upiIntent, 0)
        for (a in upiActivities) {
            if (a.activityInfo.packageName == packageName) appUpiReady = true
        }
        return appUpiReady
    }

    private fun getListOfActiveUPIApps(listOfUPIPackage: List<String>): List<String> {
        try { //Keeping a try-catch block to avoid crashes if no UPI apps are installed
            val finalUpiAppsList = mutableListOf<String>()
            val upiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(UPI_INTENT_PREFIX))
            val pm = requireActivity().packageManager
            val upiActivities: List<ResolveInfo> = pm.queryIntentActivities(upiIntent, 0)
            val packageNamesOfAllInstalledApps = mutableListOf<String>()
//            for (app in upiActivities) {
//                packageNamesOfAllInstalledApps.add(app.activityInfo.packageName.lowercase())
//            }
            for (app in listOfUPIPackage) {
                if (isAppUpiReady(app.lowercase())) {
                    packageNamesOfAllInstalledApps.add(app.lowercase())
                }
                if (packageNamesOfAllInstalledApps.contains(app.lowercase())) {
                    finalUpiAppsList.add(app)
                }
            }
            return finalUpiAppsList
        } catch (_: Exception) {
            // If no UPI apps are installed, return an empty list
            return emptyList()
        }
    }

    private fun setupUPIIdValidation() {
        upiIdEt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                errorinfoTextView.visibility = View.GONE
            }
        }
        upiIdEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val isValidUPI = UPI_REGEX.matches(s.toString())
                Utils.handleCTAEnableDisable(requireContext(), isValidUPI, verifyContinueButton)
                if (!isValidUPI) {
                    errorinfoTextView.visibility = View.VISIBLE
                } else {
                    errorinfoTextView.visibility = View.GONE
                }
            }
        })
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

    private fun payAction(
        vpa: String? = null,
        transactionMode: String?,
    ) {
        mTransactionMode = transactionMode
        val paymentMode = arrayListOf(UPI_ID)
        val extra = Extra(
            paymentMode,
            getAmount(),
            getCurrency(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Utils.createSDKData(requireActivity())
        )
        val upiData = UpiData(UPI_ID, vpa, transactionMode)
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.processPaymentResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            cancelTransactionProcess()
                            findNavController().navigate(R.id.action_UPIFragment_to_successFragment)
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading)
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())
                        }

                        is BaseResult.Success<ProcessPaymentResponse> -> {
                            bottomSheetDialog?.dismiss()
                            if (mTransactionMode == UPI_INTENT) {
                                showUpiTray(
                                    it.data.deep_link ?: "",
                                    upiAppPackageName = selectUPIPackage
                                )
                            } else {
                                showProcessPaymentVPADialog()
                                viewModel.startPolling()
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
                            findNavController().navigate(R.id.action_UPIFragment_to_failureFragment)
                        }

                        is BaseResult.Loading -> {
                            // nothing to do since we already show the process payment dialog
                        }

                        is BaseResult.Success<TransactionStatusResponse> -> {
                            val status = it.data.data.status
                            when (status) {

                                PROCESSED_PENDING -> {
                                    // Do nothing, we will keep polling for the transaction status
                                }

                                PROCESSED_STATUS -> {
                                    cancelTransactionProcess()
                                    findNavController().navigate(R.id.action_UPIFragment_to_successFragment)
                                }

                                PROCESSED_ATTEMPTED -> {
                                    cancelTransactionProcess()
                                    if (it.data.data.is_retry_available)
                                        findNavController().navigate(R.id.action_successFragment_to_retryFragment)
                                    else
                                        findNavController().navigate(R.id.action_successFragment_to_failureFragment)
                                }

                                PROCESSED_FAILED -> {
                                    cancelTransactionProcess()
                                    findNavController().navigate(R.id.action_UPIFragment_to_successFragment)
                                }
                            }
                            viewModel.resetTransactionResponse()
                        }
                    }
                }
            }
        }
    }


    private fun cancelTransactionProcess() {
        viewModel.isShowingVPADialog = false
        viewModel.isShowingUPIDialog = false
        bottomSheetDialog?.dismiss()
        bottomTimerSheetDialog?.dismiss()
        bottomVPASheetDialog?.dismiss()
        viewModel.stopPolling()
    }


    private fun showProcessPaymentVPADialog() {
        viewModel.isShowingVPADialog = true
        bottomVPASheetDialog = BottomSheetDialog(requireContext())
        val view =
            LayoutInflater.from(requireActivity())
                .inflate(R.layout.upi_vpa_process_payment_bottom_sheet, null)
        val cancelPaymentTextView: TextView = view.findViewById(R.id.cancelPaymentTextView)
        val vpaId: TextView = view.findViewById(R.id.vpaId)
        vpaId.text = upiIdEt.text.toString()
        cancelPaymentTextView.setOnClickListener {
            viewModel.isShowingVPADialog = false
            bottomVPASheetDialog?.dismiss()
            cancelTransactionProcess()
        }
        bottomVPASheetDialog?.setCancelable(false)
        bottomVPASheetDialog?.setCanceledOnTouchOutside(false)
        bottomVPASheetDialog?.setContentView(view)
        bottomVPASheetDialog?.show() // Show the dialog first
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
        bottomVPASheetDialog?.dismiss()
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
                        return paymentModeData.upi_flows ?: emptyList()
                    }
                }
            }
        return emptyList()
    }

}