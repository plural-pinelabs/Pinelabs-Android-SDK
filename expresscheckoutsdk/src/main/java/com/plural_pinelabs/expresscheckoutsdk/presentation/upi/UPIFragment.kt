package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import UpiAppsAdapter
import android.content.Intent
import android.content.pm.ResolveInfo
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

        payByAnyUPIButton.setOnClickListener {
            payAction(null, UPI_INTENT)
            // Handle the click event for the "Pay by Any UPI" button
        }
        verifyContinueButton.setOnClickListener {
            payAction(upiIdEt.text.toString(), UPI_COLLECT)
            // TODO Handle the click event for the "Verify and Continue" button
        }
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
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
        upiIdEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val isValidUPI = UPI_REGEX.matches(s.toString())

                verifyContinueButton.isEnabled = isValidUPI
                if (!verifyContinueButton.isEnabled) {
                    //TODO show error message
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
        if (null != upiPayIntent.resolveActivity(requireActivity().packageManager)) {
            val chooser = Intent.createChooser(upiPayIntent, getString(R.string.upi_open_with))
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
                            //Throw error and exit SDK
                            //TODO Pass error message and description
                            cancelTransactionProcess()
                            findNavController().navigate(R.id.action_UPIFragment_to_failureFragment)
                        }

                        is BaseResult.Loading -> {
                            //show the process dialog payment
                            if (it.isLoading)
                            //show the process dialog payment
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
                                    bottomSheetDialog?.dismiss()
                                    transactionStatusJob?.cancel()
                                    findNavController().navigate(R.id.action_UPIFragment_to_successFragment)
                                }

                                PROCESSED_ATTEMPTED -> {
                                    cancelTransactionProcess()
                                    // TODO ATTEMPTED OR FAILED Handle the scenario for retry or failure
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
        bottomSheetDialog = Utils.showProcessPaymentDialog(requireContext())
        bottomSheetDialog?.show()
        transactionStatusJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                // Your task logic here
                viewModel.getTransactionStatus(token)
                delay(UPI_TRANSACTION_STATUS_INTERVAL) // Wait for 5 seconds
            }
        }
    }

}