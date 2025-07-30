package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TENURE_ID
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.EMIPaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration

class EMIFragment : Fragment() {
    private lateinit var bankListRecyclerView: RecyclerView
    private lateinit var emiCCPayment: TextView
    private lateinit var emiCashlessPayment: TextView
    private lateinit var emiDCPayment: TextView
    private lateinit var emiPaymentModeData: EMIPaymentModeData
    private lateinit var backButton: ImageView
    private lateinit var startingEmiTv: TextView

    private lateinit var bankLogoMap: HashMap<String, String>
    private lateinit var banKTitleToCodeMap: HashMap<String, String>
    private lateinit var bankNameKeyList: List<String>
    private var currentSelectedTab: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_e_m_i, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        processDataForEMI()
        mapBanKLogo()
        setViews(view)
        resetOrderSummary()
    }

    private fun resetOrderSummary() {
        ExpressSDKObject.setSelectedTenure(null)
        (requireActivity() as LandingActivity).showHideConvenienceFessMessage(ExpressSDKObject.getFetchData()?.convenienceFeesInfo?.isEmpty() == false)

    }

    private fun setViews(view: View) {
        bankListRecyclerView = view.findViewById(R.id.emi_bank_list_rv)
        emiCCPayment = view.findViewById(R.id.emi_cc_selector_text)
        emiDCPayment = view.findViewById(R.id.dc_emi_selector_text)
        emiCashlessPayment = view.findViewById(R.id.card_less_selector_text)
        backButton = view.findViewById(R.id.back_button)
        startingEmiTv = view.findViewById(R.id.emi_saving_text)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        emiCCPayment.setOnClickListener {
            showEMIListBasedOnSelection(Constants.EMI_CC_TYPE)
        }
        emiDCPayment.setOnClickListener {
            showEMIListBasedOnSelection(Constants.EMI_DC_TYPE)
        }
        emiCashlessPayment.setOnClickListener {
            showEMIListBasedOnSelection(Constants.EMI_CASHLESS_TYPE)
        }
        showEMIListBasedOnSelection(
            Constants.EMI_CC_TYPE
        )
        startingEmiTv.text = String.format(
            getString(R.string.emi_starting_at_x_month),
            Utils.convertInRupees(getLeastEmiAmount()?.monthly_emi_amount?.value ?: 0).toString()
        )

    }

    private fun showEMIList(listOfCCBank: List<Issuer>) {
        val maxTenureMap = Utils.getMaxDiscountTenurePerIssuer(listOfCCBank)
        val adapter = EMIBankRecyclerViewAdapter(
            requireContext(),
            listOfCCBank,
            getItemClickListener(),
            bankLogoMap,
            bankNameKeyList,
            banKTitleToCodeMap,
            maxTenureMap
        )
        bankListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        bankListRecyclerView.addItemDecoration(
            DividerItemDecoration(
                ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_divider)
            )
        )
        bankListRecyclerView.adapter = adapter
    }

    private fun showEMIListBasedOnSelection(selection: String) {
        currentSelectedTab = selection // Update the current selected tab
        when (selection) {
            Constants.EMI_CC_TYPE -> {
                showEMIList(getListOfBanks(Constants.EMI_CC_TYPE))
                emiCCPayment.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )
                emiCCPayment.setBackgroundResource(R.drawable.emi_selected_mode_bg)
                emiDCPayment.setBackgroundResource(android.R.color.transparent)
                emiCashlessPayment.setBackgroundResource(android.R.color.transparent)
                emiDCPayment.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.black_text_80)
                )
            }

            Constants.EMI_DC_TYPE -> {
                emiDCPayment.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )
                emiDCPayment.setBackgroundResource(R.drawable.emi_selected_mode_bg)
                // Handle DC type if needed
                showEMIList(getListOfBanks(Constants.EMI_DC_TYPE))
                emiDCPayment.setBackgroundResource(R.drawable.emi_selected_mode_bg)
                emiCCPayment.setBackgroundResource(android.R.color.transparent)
                emiCashlessPayment.setBackgroundResource(android.R.color.transparent)
                emiCCPayment.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.black_text_80)
                )
            }

            else -> {
                // Handle other types or default case cardless
            }
        }
    }

    private fun getListOfBanks(emiType: String): List<Issuer> {
        return emiPaymentModeData.issuers.filter { issuer ->
            issuer.issuer_type.equals(
                emiType,
                true
            ) && !(issuer.tenures.size == 1 && issuer.tenures[0].tenure_value == 0)
        }

    }

    private fun getItemClickListener(): ItemClickListener<Issuer> {
        return object : ItemClickListener<Issuer> {
            override fun onItemClick(position: Int, item: Issuer) {
                // Handle item click
                val bundle = Bundle().apply {
                    putString(TENURE_ID, item.id)
                }
                findNavController().navigate(
                    R.id.action_EMIFragment_to_tenureSelectionFragment,
                    bundle
                )
            }
        }
    }

    private fun processDataForEMI() {
        val data = ExpressSDKObject.getFetchData()
        data?.paymentModes?.filter { paymentMode -> paymentMode.paymentModeId == PaymentModes.EMI.paymentModeID }
            ?.forEach { paymentMode ->
                when (val pm = paymentMode.paymentModeData) {
                    is LinkedTreeMap<*, *> -> {
                        emiPaymentModeData = convertMapToJsonObject(pm)
                        ExpressSDKObject.setEMIPaymentModeData(emiPaymentModeData)
                    }
                }
            }
    }

    private fun convertMapToJsonObject(yourMap: Map<*, *>): EMIPaymentModeData {
        val gson = Gson().toJsonTree(yourMap).asJsonObject
        return Gson().fromJson(gson.toString(), EMIPaymentModeData::class.java)

    }

    private fun mapBanKLogo() {
        bankLogoMap = Utils.getBankLogoHashMap()
        bankNameKeyList = Utils.getListOfBanKTitle()
        banKTitleToCodeMap = Utils.bankTitleAndCodeMapper()
    }

    private fun getLeastEmiAmount(): Tenure? {
        val leastEmiTenure: Tenure? = emiPaymentModeData.issuers
            .flatMap { it.tenures }
            .filter { it.monthly_emi_amount?.value != null && it.tenure_id != "7" }
            .minByOrNull { it.monthly_emi_amount?.value!! }
        return leastEmiTenure
    }

}