package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import UpiAppsAdapter
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_TRANSACTION_STATUS_INTERVAL
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.UPIViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiData
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiTransactionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class UPIFragment : Fragment() {

    private lateinit var payByAnyUPIButton: TextView
    private lateinit var upiAppsRv: RecyclerView
    private lateinit var upiIdEt: EditText
    private lateinit var verifyContinueButton: Button
    private lateinit var errorinfoTextView: TextView
    private val UPI_REGEX = Regex("^[\\w.]{1,}-?[\\w.]{0,}-?[\\w.]{1,}@[a-zA-Z]{2,}$")
    private lateinit var viewModel: UPIViewModel
    private var mTransactionMode: String? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var transactionStatusJob: Job? = null
    private var selectUPIPackage: String? = null


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

    }

    private fun setUpPayByUPIApps() {
        val installedUPIApps = getUpiAppsInstalledInDevice()
        if (installedUPIApps.isNotEmpty()) {
            payByAnyUPIButton.visibility = View.VISIBLE
            upiAppsRv.visibility = View.VISIBLE
            upiAppsRv.layoutManager = GridLayoutManager(requireContext(), 2)
            upiAppsRv.adapter = UpiAppsAdapter(installedUPIApps, getItemClickListenerForUPIApp())

        } else {
            payByAnyUPIButton.visibility = View.GONE
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
        //  listOfPaymentReadyApps.addAll(getListOfActiveUPIApps(listOfUPIPackage)) TODO uncomment this before going QA
        //  return listOfPaymentReadyApps
        return listOfUPIPackage
    }

    private fun getListOfActiveUPIApps(listOfUPIPackage: List<String>): List<String> {
        try { //Keeping a try-catch block to avoid crashes if no UPI apps are installed
            val upiIntent = Intent(Intent.ACTION_VIEW, UPI_INTENT_PREFIX.toUri())
            val finalUpiAppsList = mutableListOf<String>()
            val pm = requireActivity().packageManager
            val upiActivities: List<ResolveInfo> = pm.queryIntentActivities(upiIntent, 0)
            val packageNamesOfAllInstalledApps = mutableListOf<String>()
            for (app in upiActivities) {
                packageNamesOfAllInstalledApps.add(app.activityInfo.packageName.lowercase())
            }
            for (app in listOfUPIPackage) {
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
            startActivity(chooser)
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
                null
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
                            if (mTransactionMode == UPI_COLLECT) {
                                //hit the transaction status api
                                getTransactionStatus(ExpressSDKObject.getToken())
                            } else if (mTransactionMode == UPI_INTENT) {
                                showUpiTray(
                                    it.data.deep_link ?: "",
                                    upiAppPackageName = selectUPIPackage
                                )
                            }
                            bottomSheetDialog?.dismiss()
                            ExpressSDKObject.setProcessPaymentResponse(it.data)
                            getTransactionStatus(ExpressSDKObject.getToken())

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
                                    findNavController().navigate(R.id.action_UPIFragment_to_retryFragment)
                                }

                                PROCESSED_FAILED -> {
                                    cancelTransactionProcess()
                                    findNavController().navigate(R.id.action_UPIFragment_to_failureFragment)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun cancelTransactionProcess() {
        bottomSheetDialog?.dismiss()
        transactionStatusJob?.cancel()
    }


    private fun getTransactionStatus(token: String?) {
        if (mTransactionMode == UPI_INTENT) {
            showProcessPaymentDialog()
        } else {
            showProcessPaymentVPADialog()
        }
        transactionStatusJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                // Your task logic here
                viewModel.getTransactionStatus(token)
                delay(UPI_TRANSACTION_STATUS_INTERVAL) // Wait for 5 seconds
            }
        }
    }

    private fun showProcessPaymentDialog() {
        val view =
            LayoutInflater.from(requireActivity()).inflate(R.layout.timer_bottom_sheet_layout, null)
        val cancelPaymentTextView: TextView = view.findViewById(R.id.cancelPaymentTextView)

        cancelPaymentTextView.setOnClickListener {
            bottomSheetDialog?.dismiss()
            cancelTransactionProcess()
            //TODO cancel payment retry mechnaism
        }

        val circularProgressBar: ProgressBar = view.findViewById(R.id.circularProgressBar)
        //TODO set the color of the progress bar and text view based on the palette
        // if (palette != null) {
//            circularProgressBar.progressTintList =
//                ColorStateList.valueOf(Color.parseColor(palette?.C900))
//            cancelPaymentTextView.setTextColor(Color.parseColor(palette?.C900))
        //  }
        val timerTextView: TextView = view.findViewById(R.id.timerTextView)

        circularProgressBar.progress = 0
        val totalTime = 600000L
        val interval = 1000L

        val countDownTimer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerTextView.text = String.format(
                    getString(R.string.timer_format),
                    secondsRemaining / 60,
                    secondsRemaining % 60
                )
                val progressPercentage = (millisUntilFinished * 100 / totalTime).toInt()
                circularProgressBar.progress = progressPercentage
            }

            override fun onFinish() {
                timerTextView.text = getString(R.string.timer_format)
                circularProgressBar.progress = 0
                bottomSheetDialog?.dismiss()
                cancelTransactionProcess()
                //TODO throw error
            }
        }.start()
        bottomSheetDialog?.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
//                val layoutParams = it.layoutParams
//                val displayMetrics = Resources.getSystem().displayMetrics
//                val screenHeight = displayMetrics.heightPixels
//                layoutParams.height = (screenHeight * 0.95).toInt()
//                it.layoutParams = layoutParams
//                behavior.expandedOffset =
//                    (screenHeight * 0.05).toInt() // Set expanded offset to 15% of screen height
//                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
                behavior.isFitToContents = false
                behavior.skipCollapsed = false
            }
            bottomSheetDialog?.setCancelable(false)
            bottomSheetDialog?.setCanceledOnTouchOutside(false)
            bottomSheetDialog?.setContentView(view)
            bottomSheetDialog?.show() // Show the dialog first
        }
    }

    private fun showProcessPaymentVPADialog() {
        val view =
            LayoutInflater.from(requireActivity())
                .inflate(R.layout.upi_vpa_process_payment_bottom_sheet, null)
        val cancelPaymentTextView: TextView = view.findViewById(R.id.cancelPaymentTextView)
        val vpaId: TextView = view.findViewById(R.id.vpaId)
        vpaId.text = upiIdEt.text.toString()


        bottomSheetDialog?.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
//                val layoutParams = it.layoutParams
//                val displayMetrics = Resources.getSystem().displayMetrics
//                val screenHeight = displayMetrics.heightPixels
//                layoutParams.height = (screenHeight * 0.95).toInt()
//                it.layoutParams = layoutParams
//                behavior.expandedOffset =
//                    (screenHeight * 0.05).toInt() // Set expanded offset to 15% of screen height
//                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
                behavior.isFitToContents = false
                behavior.skipCollapsed = false
            }
        }
        cancelPaymentTextView.setOnClickListener {
            bottomSheetDialog?.dismiss()
            cancelTransactionProcess()
            //TODO cancel payment retry mechnaism
        }
        bottomSheetDialog?.setCancelable(false)
        bottomSheetDialog?.setCanceledOnTouchOutside(false)
        bottomSheetDialog?.setContentView(view)
        bottomSheetDialog?.show() // Show the dialog first
    }
}