package com.plural_pinelabs.expresscheckoutsdk.presentation.netbanking

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_IMAGES
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_MESSAGE_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.NET_BANKING
import com.plural_pinelabs.expresscheckoutsdk.common.DeviceType
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetBankingViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.TransactionMode
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.getBankLogoHashMap
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentBottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.AcquirerWisePaymentData
import com.plural_pinelabs.expresscheckoutsdk.data.model.DeviceInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.NetBank
import com.plural_pinelabs.expresscheckoutsdk.data.model.NetBankingData
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.issuerDataList
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NetBankingFragment : Fragment() {

    private lateinit var payByBankWebsite: LinearLayout
    private lateinit var conventionalParentLayout: LinearLayout
    private lateinit var conventionalSearchEt: EditText
    private lateinit var conventionalRecyclerView: RecyclerView

    private var bankList: List<NetBank> = mutableListOf()
    private var nbblBankList: List<NetBank> = arrayListOf()
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var imageLoader: ImageLoader? = null
    private lateinit var viewModel: NetBankingViewModel


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
                            bottomSheetDialog?.dismiss()
                            ExpressSDKObject.setProcessPaymentResponse(it.data)
                            redirectToACS()
                        }
                    }
                }
            }
        }
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
            if (!hashSet.contains(item.merchantPaymentCode) && bankCodeHashMap.containsKey(item.merchantPaymentCode)) {
                var image: String? = bankCodeHashMap[item.merchantPaymentCode]
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
            item.PaymentOption.forEach {
                if (!hashSet.contains(it.merchantPaymentCode) && bankCodeHashMap.containsKey(it.merchantPaymentCode)) {
                    val image: String? = bankCodeHashMap[it.merchantPaymentCode]
                    val bank = NetBank(
                        it.merchantPaymentCode,
                        it.Name,
                        image ?: "",
                        nbbl
                    )
                    finalList.add(bank)
                    it.merchantPaymentCode?.let { it1 -> hashSet.add(it1) }
                } else {
                    finalList.single() { bank -> bank.bankCode == it.merchantPaymentCode }.isNBBLBank =
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
        payByBankWebsite = view.findViewById(R.id.pay_by_bank_website_btn)
        conventionalParentLayout = view.findViewById(R.id.pay_by_bank_website_conventional_parent)
        conventionalSearchEt = view.findViewById(R.id.pay_by_bank_website_conventional_search_et)
        conventionalRecyclerView = view.findViewById(R.id.pay_by_bank_website_conventional_rv)
        setUpConventionalViews()
        payByBankWebsite.setOnClickListener {
            showAllBanksBottomSheet()
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

    private fun getItemClickListener(): ItemClickListener<NetBank> {
        return object : ItemClickListener<NetBank> {
            override fun onItemClick(position: Int, item: NetBank) {
                bottomSheetDialog?.dismiss()
                val processPaymentRequest = createProcessPaymentRequest(
                    item.bankName,
                    item.bankCode,
                    ExpressSDKObject.getAmount(),
                    ExpressSDKObject.getCurrency()
                )
                viewModel.processPayment(ExpressSDKObject.getToken(), processPaymentRequest)

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
        //val convenienceFeesData = ConvenienceFeesData(131040, 23785, 1100, 655925, 500000, 99999999, 155925, "INR")
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
                null
            )
        return processPaymentRequest;
    }
}