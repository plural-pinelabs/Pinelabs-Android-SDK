package com.plural_pinelabs.expresscheckoutsdk.presentation.wallets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ERROR_MESSAGE_KEY
import com.plural_pinelabs.expresscheckoutsdk.common.DeviceType
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.TransactionMode
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentBottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.common.WalletViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.data.model.DeviceInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.Wallet
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletBank
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletData
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration
import kotlinx.coroutines.launch
import java.lang.reflect.Type

class WalletFragment : Fragment() {
    private lateinit var paymentModeRecyclerView: RecyclerView
    private lateinit var backBtn: ImageView

    private lateinit var viewModel: WalletViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(
            this, WalletViewModelFactory(NetworkHelper(requireContext()))
        )[WalletViewModel::class.java]
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        showListOfWallet()
        observeViewModel()
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
                                R.id.action_walletFragment_to_retryFragment, bundle
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
        findNavController().navigate(R.id.action_walletFragment_to_ACSFragment)

    }

    private fun setViews(view: View) {
        paymentModeRecyclerView = view.findViewById(R.id.wallet_list_rv)
        backBtn = view.findViewById(R.id.back_button)
        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showListOfWallet() {
        paymentModeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        paymentModeRecyclerView.addItemDecoration(
            DividerItemDecoration(
                ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_divider)
            )
        )
        val walletList = getWalletList()
        walletList?.let { list ->
            if (list.isEmpty()) {
                return // TODO handle error no wallet in there
            }
            val adapter = WalletRecyclerViewAdapter(
                list, getWalletClickListener()
            )
            paymentModeRecyclerView.adapter = adapter
        }
    }

    private fun getWalletClickListener(): ItemClickListener<WalletBank> {
        return object : ItemClickListener<WalletBank> {
            override fun onItemClick(position: Int, item: WalletBank) {
                //TODO handle click of wallet item
                val processPaymentRequest = createProcessPaymentRequest(
                    item.bankCode, ExpressSDKObject.getAmount(), ExpressSDKObject.getCurrency()
                )
                viewModel.processPayment(ExpressSDKObject.getToken(), processPaymentRequest)
            }
        }
    }

    private fun getWalletList(): List<WalletBank>? {
        val data = ExpressSDKObject.getFetchData()?.paymentModes
        data?.filter { paymentMode -> paymentMode.paymentModeId == PaymentModes.WALLET.paymentModeID }
            ?.forEach { paymentMode ->
                when (val pm = paymentMode.paymentModeData) {
                    is List<*> -> {
                        val walletData = convertMapToJsonObject(pm)
                        val walletList = mapBankList(
                            walletData
                        )
                        return walletList
                    }
                }
            }
        return null
    }

    private fun mapBankList(
        walletList: List<Wallet>?,
    ): List<WalletBank> {
        val finalWalletBankList = mutableListOf<WalletBank>()
        val localImageHashMap: HashMap<String, Int> = getLocalWalletHashMap()
        walletList?.forEachIndexed() { _, wallet ->
            val walletLogo =
                if (localImageHashMap.contains(wallet.merchantPaymentCode)) localImageHashMap[wallet.merchantPaymentCode] else R.drawable.ic_generic_wallet
            val walletBank = WalletBank(
                wallet.merchantPaymentCode,
                wallet.bankName,
                walletLogo ?: R.drawable.ic_generic_wallet
            )
            finalWalletBankList.add(walletBank)
        }
        return finalWalletBankList
    }

    private fun getLocalWalletHashMap(): HashMap<String, Int> {
        val hashMap: java.util.HashMap<String, Int> = hashMapOf()
        hashMap["PHW"] = R.drawable.wallet_phonepe
        hashMap["ATL"] = R.drawable.wallet_airtel_money
        hashMap["FRW"] = R.drawable.wallet_freecharge
        hashMap["PZ2"] = R.drawable.wallet_payzapp
        hashMap["YBW"] = R.drawable.wallet_yes_bank
        hashMap["Paytm"] = R.drawable.wallet_paytm
        hashMap["Mobiwik"] = R.drawable.wallet_mobikwik
        hashMap["JIO"] = R.drawable.wallet_jio_money
        return hashMap
    }

    private fun convertMapToJsonObject(yourMap: List<*>): List<Wallet> {
        val gson = Gson().toJsonTree(yourMap).asJsonArray
        val listType: Type = object : TypeToken<ArrayList<Wallet?>?>() {}.type
        return Gson().fromJson(gson.toString(), listType)

    }

    private fun createProcessPaymentRequest(
        payCode: String?, amount: Int, currency: String
    ): ProcessPaymentRequest {

        val paymentMode = arrayListOf(Constants.WALLET_ID)
        val walletData = WalletData(payCode)
        //val convenienceFeesData = ConvenienceFeesData(131040, 23785, 1100, 655925, 500000, 99999999, 155925, "INR")
        val deviceInfo = DeviceInfo(
            DeviceType.MOBILE.name,
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (HTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        val extras = Extra(
            paymentMode,
            amount,
            currency,
            null,
            null,
            null,
            TransactionMode.REDIRECT.name,
            deviceInfo,
            null,
            null,
            Utils.createSDKData(requireActivity())
        )
        val processPaymentRequest = ProcessPaymentRequest(
            null, null, null, null, walletData, null, extras, null, null
        )
        return processPaymentRequest;
    }
}
