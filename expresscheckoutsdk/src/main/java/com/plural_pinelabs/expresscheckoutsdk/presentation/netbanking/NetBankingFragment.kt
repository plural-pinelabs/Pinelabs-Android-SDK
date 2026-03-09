package com.plural_pinelabs.expresscheckoutsdk.presentation.netbanking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.CleverTapUtil
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_IMAGES
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DEFAULT_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_MESSAGE_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.NET_BANKING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_ATTEMPTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_PENDING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.DeviceType
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetBankingViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.TimerManager
import com.plural_pinelabs.expresscheckoutsdk.common.TransactionMode
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.getBankLogoHashMap
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentBottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.AcquirerWisePaymentData
import com.plural_pinelabs.expresscheckoutsdk.data.model.ConvenienceFeesInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.DeviceInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.NetBank
import com.plural_pinelabs.expresscheckoutsdk.data.model.NetBankingData
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.issuerDataList
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NetBankingFragment : Fragment() {
    private lateinit var conventionalParentLayout: LinearLayout
    private lateinit var conventionalSearchEt: EditText
    private lateinit var conventionalRecyclerView: RecyclerView

    private lateinit var payByAnyBankLayout: ConstraintLayout
    private lateinit var payByQRLayout: ConstraintLayout
    private lateinit var payByNBLayout: ConstraintLayout

    private lateinit var payByAnyBankButton: LinearLayout
    private lateinit var payByQRButton: LinearLayout
    private lateinit var payByBankWebsite: LinearLayout
    private lateinit var nbblParentLayout: LinearLayout


    private var bankList: List<NetBank> = mutableListOf()

    private var isNBBLEnabled = false
    private var nbblBankList: List<NetBank> = arrayListOf()
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var imageLoader: ImageLoader? = null
    private lateinit var viewModel: NetBankingViewModel
    private var convenienceFeesData: ConvenienceFeesInfo? = null
    private var isAnyBankApp: Boolean = false
    private var isQRPayment: Boolean = false
    private var bottomTimerSheetDialog: BottomSheetDialog? = null
    private var consumedDeepLink = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(
            this, NetBankingViewModelFactory(NetworkHelper(requireContext()))
        )[NetBankingViewModel::class.java]
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_net_banking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setNBList()
        setViews(view)
        handleConvenienceFees()
    }

    private fun observeViewModel() {
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
                                R.id.action_netBankingFragment_to_successFragment,
                            )
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading) bottomSheetDialog =
                                showProcessPaymentBottomSheetDialog(requireContext())
                        }

                        is BaseResult.Success<ProcessPaymentResponse> -> {
                            if (isAnyBankApp) {
                                showProcessPaymentTimerDialog()
                                viewModel.startPolling()
                            } else if (isQRPayment) {
                                //do enquiry
                                viewModel.startPolling()
                            } else {
                                bottomSheetDialog?.dismiss()
                                ExpressSDKObject.setProcessPaymentResponse(it.data)
                                redirectToACS()
                            }
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
                            findNavController().navigate(R.id.action_netBankingFragment_to_successFragment)
                        }

                        is BaseResult.Loading -> {
                            // nothing to do since we already show the process payment dialog
                        }

                        is BaseResult.Success<TransactionStatusResponse> -> {
                            bottomSheetDialog?.dismiss()
                            val status = it.data.data.status
                            when (status) {
                                PROCESSED_PENDING -> {
                                    // Do nothing, we will keep polling for the transaction status
                                    val deepLink = it.data.data.deep_link
                                    if (!deepLink.isNullOrEmpty() && !consumedDeepLink) {
                                        consumedDeepLink = true
                                        if (isAnyBankApp) {
                                            //show intent
                                            val intent = Intent(Intent.ACTION_VIEW)
                                            intent.setData(Uri.parse(deepLink)) // Replace with your actual deep link
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            requireContext().startActivity(intent)
                                        } else {
                                            //show QR
                                            showQRBottomSheet(deepLink)
                                        }
                                    }

                                }

                                PROCESSED_STATUS -> {
                                    cancelTransactionProcess()
                                    findNavController().navigate(R.id.action_netBankingFragment_to_successFragment)
                                }

                                PROCESSED_ATTEMPTED -> {
                                    cancelTransactionProcess()
                                    findNavController().navigate(R.id.action_netBankingFragment_to_successFragment)
                                }

                                PROCESSED_FAILED -> {
                                    cancelTransactionProcess()
                                    findNavController().navigate(R.id.action_netBankingFragment_to_successFragment)
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
        bottomSheetDialog?.dismiss()
        viewModel.stopPolling()
    }

    private fun redirectToACS(
    ) {
        bottomSheetDialog?.dismiss()
        findNavController().navigate(R.id.action_netBankingFragment_to_ACSFragment)

    }

    private fun setNBList() {
        val data = ExpressSDKObject.getFetchData()
        data?.paymentModes?.filter { paymentMode -> paymentMode.paymentModeId == PaymentModes.NET_BANKING.paymentModeID }
            ?.forEach { paymentMode ->
                when (val pm = paymentMode.paymentModeData) {
                    is LinkedTreeMap<*, *> -> {
                        val paymentModeData = convertMapToJsonObject(pm)
                        bankList = getBankListFromPaymentModeData(
                            paymentModeData.IssersUIDataList,
                            paymentModeData.acquirerWisePaymentData
                        )
                        nbblBankList = bankList.filter { it.isNBBLBank }
                    }
                }
            }
    }

    private fun getBankListFromPaymentModeData(
        issuerDataList: List<issuerDataList>?,
        acquirerWisePaymentData: List<AcquirerWisePaymentData>?
    ): List<NetBank> {
        val finalList = mutableListOf<NetBank>()
        val hashSet = mutableSetOf<String>()
        val bankCodeHashMap = getBankLogoHashMap()
        preLoadBankLogos(bankCodeHashMap)
        issuerDataList?.forEach { item ->
            if (!hashSet.contains(item.merchantPaymentCode)) {
                var image: String? =
                    bankCodeHashMap[item.merchantPaymentCode] ?: bankCodeHashMap[DEFAULT_BANK_CODE]
                image?.let {
                    image = BASE_IMAGES + it
                }
                val bank = NetBank(
                    item.merchantPaymentCode,
                    item.bankName,
                    image ?: "",
                    false
                )
                finalList.add(bank)
                item.merchantPaymentCode?.let { hashSet.add(it) }
            }
        }

        acquirerWisePaymentData?.forEach { item ->
            val nbbl = item.isNbbl
            //to keep nbbl always disable for now for merchant
//            if (nbbl) {
//                isNBBLEnabled = true
//            }

            item.PaymentOption.forEach {
                if (!hashSet.contains(it.merchantPaymentCode)) {
                    val image: String? = bankCodeHashMap[it.merchantPaymentCode]
                        ?: bankCodeHashMap[DEFAULT_BANK_CODE]
                    val bank = NetBank(
                        it.merchantPaymentCode,
                        it.Name,
                        image ?: "",
                        nbbl
                    )
                    finalList.add(bank)
                    it.merchantPaymentCode?.let { it1 -> hashSet.add(it1) }
                } else {
                    finalList.singleOrNull { bank -> bank.bankCode == it.merchantPaymentCode }?.isNBBLBank =
                        nbbl
                }
            }

        }
        return finalList.toList()
    }


    private fun convertMapToJsonObject(yourMap: Map<*, *>): PaymentModeData {
        val gson = Gson().toJsonTree(yourMap).asJsonObject
        return Gson().fromJson(gson.toString(), PaymentModeData::class.java)

    }

    private fun setViews(view: View) {
        conventionalParentLayout = view.findViewById(R.id.pay_by_bank_website_conventional_parent)
        conventionalSearchEt = view.findViewById(R.id.pay_by_bank_website_conventional_search_et)
        conventionalRecyclerView = view.findViewById(R.id.pay_by_bank_website_conventional_rv)
        nbblParentLayout = view.findViewById(R.id.nbbl_parent_ll)
        payByAnyBankLayout = view.findViewById(R.id.pay_by_any_bank)
        payByQRLayout = view.findViewById(R.id.pay_by_qr)
        payByNBLayout = view.findViewById(R.id.pay_by_bank_website)
        payByAnyBankButton = view.findViewById(R.id.pay_by_any_bank_btn)
        payByQRButton = view.findViewById(R.id.pay_by_qr_btn)
        payByBankWebsite = view.findViewById(R.id.pay_by_bank_website_btn)


        setUpConventionalViews()
        payByBankWebsite.setOnClickListener {
            showAllBanksBottomSheet()
        }

        payByAnyBankButton.setOnClickListener {
            val processPaymentRequest = createProcessPaymentRequest(
                null, null,
                ExpressSDKObject.getAmount(),
                ExpressSDKObject.getCurrency(),
                txnMode = "INTENT",
                ExpressSDKObject.getPhoneNumber()
            )
            isAnyBankApp = true
            isQRPayment = false
            viewModel.processPayment(ExpressSDKObject.getToken(), processPaymentRequest)

        }

        payByQRButton.setOnClickListener {
            val processPaymentRequest = createProcessPaymentRequest(
                null, null,
                ExpressSDKObject.getAmount(),
                ExpressSDKObject.getCurrency(),
                txnMode = "QR",
                ExpressSDKObject.getPhoneNumber()
            )
            isQRPayment = true
            isAnyBankApp = false
            viewModel.processPayment(ExpressSDKObject.getToken(), processPaymentRequest)

        }

        conventionalSearchEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                conventionalSearchEt.setBackgroundResource(R.drawable.black_field_border)
            } else {
                conventionalSearchEt.setBackgroundResource(R.drawable.input_field_border)

            }
        }

        view.findViewById<ImageView>(R.id.back_button)
            .setOnClickListener { findNavController().popBackStack() }

        showHideConventionalNBLayout(isNBBLEnabled)
    }


    private fun setUpConventionalViews() {
        val nbSelectBankAdapter = NBSelectBankAdapter(bankList, getItemClickListener(), imageLoader)
        conventionalSearchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                filter(text = s.toString(), nbSelectBankAdapter)
            }
        })
        setBankRecyclerView(conventionalRecyclerView, nbSelectBankAdapter)
    }


    private fun showAllBanksBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        val view =
            LayoutInflater.from(requireActivity())
                .inflate(R.layout.net_banking_bottomsheet_layout, null)
        bottomSheetDialog?.setContentView(view)

        val btnClose: ImageView = view.findViewById(R.id.cancel_btn)
        btnClose.setOnClickListener {
            bottomSheetDialog?.dismiss()
        }
        val nbSelectBankAdapter = NBSelectBankAdapter(bankList, getItemClickListener(), imageLoader)
        val edtSearchBank: EditText = view.findViewById(R.id.edt_search_banks)
        edtSearchBank.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                //TODO filter based on input
                filter(text = s.toString(), nbSelectBankAdapter)
            }
        })
        val recyclerMoreBanks: RecyclerView = view.findViewById(R.id.recycler_net_all_banks)
        setBankRecyclerView(recyclerMoreBanks, nbSelectBankAdapter)
        bottomSheetDialog?.show()
    }

    private fun showQRBottomSheet(deepLink: String? = null) {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        val view =
            LayoutInflater.from(requireActivity())
                .inflate(R.layout.nb_qr_bottomsheet_layout, null)
        bottomSheetDialog?.setContentView(view)

        val btnClose: ImageView = view.findViewById(R.id.cancel_btn)
        val qrImageView: ImageView = view.findViewById(R.id.qr_code_image)
        val timerText = view.findViewById<TextView>(R.id.complete_payment_timer_tv)
        btnClose.setOnClickListener {
            bottomSheetDialog?.dismiss()
        }
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(deepLink, BarcodeFormat.QR_CODE, 400, 400)
            qrImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        startQRTimer(timerText)
        bottomSheetDialog?.show()
    }

    private fun getItemClickListener(): ItemClickListener<NetBank> {
        return object : ItemClickListener<NetBank> {
            override fun onItemClick(position: Int, item: NetBank) {
                bottomSheetDialog?.dismiss()
                isAnyBankApp = false
                isQRPayment = false
                val processPaymentRequest = createProcessPaymentRequest(
                    item.bankName,
                    item.bankCode,
                    ExpressSDKObject.getAmount(),
                    ExpressSDKObject.getCurrency()
                )
                viewModel.processPayment(ExpressSDKObject.getToken(), processPaymentRequest)

                CleverTapUtil.sdkCheckoutContinueClicked(
                    CleverTapUtil.getInstance(requireContext()),
                    ExpressSDKObject.getFetchData(),
                    PaymentModes.NET_BANKING.paymentModeName.toString(),
                    Utils.getCartValue(),
                    "not known",
                    "${item.bankName} ${item.bankCode}"
                )

            }
        }
    }

    private fun setBankRecyclerView(
        recyclerMoreBanks: RecyclerView,
        nbSelectBankAdapter: NBSelectBankAdapter
    ) {
        recyclerMoreBanks.layoutManager = LinearLayoutManager(requireContext())
        recyclerMoreBanks.addItemDecoration(
            DividerItemDecoration(
                ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_divider)
            )
        )
        recyclerMoreBanks.adapter = nbSelectBankAdapter
    }

    private fun filter(text: String, nbSelectBankAdapter: NBSelectBankAdapter) {
        val filteredList = mutableListOf<NetBank>()

        bankList.let { bankList ->
            for (item in bankList) {
                if (item.bankName?.lowercase()?.contains(text.lowercase()) == true) {
                    filteredList.add(item)
                }
            }
        }
        if (text.isEmpty()) {
            nbSelectBankAdapter.updateListWithNewItems(bankList)
        } else {
            nbSelectBankAdapter.updateListWithNewItems(filteredList.toList())
        }
    }

    private fun preLoadBankLogos(hashMap: HashMap<String, String>) {
        // This function can be used to preload bank logos if needed
        // Currently, it is not being used in the code

        imageLoader = ImageLoader.Builder(requireContext())
            .components {
                add(SvgDecoder.Factory())
            }
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            hashMap.forEach {
                val request = ImageRequest.Builder(requireContext())
                    .data(BASE_IMAGES + it.value)
                    .build()
                imageLoader?.enqueue(request) // Preloads into cache
            }
        }

    }


    private fun createProcessPaymentRequest(
        bankName: String?,
        payCode: String?,
        amount: Int,
        currency: String,
        txnMode: String = TransactionMode.REDIRECT.name,
        mobileNumber: String? = null
    ): ProcessPaymentRequest {


        val paymentMode = arrayListOf(NET_BANKING)
        val netBankingData = NetBankingData(payCode)
        val convenienceFeesDataObj =
            convenienceFeesData?.let { Utils.getConvenienceFeesRequest(it) }
        val deviceInfo = DeviceInfo(
            DeviceType.MOBILE.name,
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            null, null, null, null, null, null,
            null, null, null, null, null
        )
        val extras = Extra(
            paymentMode,
            amount/*655925*/,
            currency,
            null,
            null,
            mobileNumber,
            txnMode,
            deviceInfo,
            null,
            null,
            Utils.createSDKData(requireActivity())
        )
        val processPaymentRequest =
            ProcessPaymentRequest(
                null,
                null,
                null,
                null,
                null,
                netBankingData,
                extras,
                null,
                convenienceFeesDataObj
            )
        return processPaymentRequest;
    }

    private fun handleConvenienceFees() {
        val fetchData = ExpressSDKObject.getFetchData()
        if (fetchData?.convenienceFeesInfo.isNullOrEmpty()) {
            (requireActivity() as LandingActivity).showHideConvenienceFessMessage(false)
            return
        }
        val convenienceFeesInfo =
            fetchData?.convenienceFeesInfo?.filter { it.paymentModeType == PaymentModes.NET_BANKING.paymentModeID }

        if (convenienceFeesInfo.isNullOrEmpty()) {
            (requireActivity() as LandingActivity).showHideConvenienceFessMessage(false)
        } else {
            (requireActivity() as LandingActivity).showHideConvenienceFessMessage(
                true,
                convenienceFeesInfo[0]
            )
            convenienceFeesData = convenienceFeesInfo[0]
        }

    }

    private fun startQRTimer(timerText: TextView) {
        val resendTimer = TimerManager
        resendTimer.startTimer(10000) // 10 seconds for demo, adjust as needed
        resendTimer.timeLeft.observe(viewLifecycleOwner) { timeLeft ->
            if (timeLeft > 0) {

                timerText.text = getString(
                    R.string.complete_the_payment_within, Utils.formatTimeInMinutes(
                        requireContext(),
                        timeLeft
                    )
                )

            } else {
                findNavController().navigate(R.id.action_netBankingFragment_to_successFragment)
            }
        }
    }

    private fun showProcessPaymentTimerDialog() {
        bottomTimerSheetDialog = BottomSheetDialog(requireContext())
        val view =
            LayoutInflater.from(requireActivity())
                .inflate(R.layout.timer_bottom_sheet_layout, null)
        val cancelPaymentTextView: TextView = view.findViewById(R.id.cancelPaymentTextView)
        cancelPaymentTextView.text = getString(R.string.try_another_nb_app)
        val timer: TextView = view.findViewById(R.id.timerTextView)
        viewModel.startCountDownTimer()
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

    private fun showHideConventionalNBLayout(show: Boolean) {
        if (!show) {
            conventionalParentLayout.visibility = View.VISIBLE
            payByAnyBankLayout.visibility= View.GONE
            payByQRLayout.visibility= View.GONE
            payByNBLayout.visibility =View.GONE
        } else {
            conventionalParentLayout.visibility = View.GONE
            nbblParentLayout.visibility = View.VISIBLE
        }
    }

}