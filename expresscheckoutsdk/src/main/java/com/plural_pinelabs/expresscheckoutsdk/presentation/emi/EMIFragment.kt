package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.plural_pinelabs.expresscheckoutsdk.data.model.EMIPaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration

class EMIFragment : Fragment() {
    private lateinit var bankListRecyclerView: RecyclerView
    private lateinit var emiPaymentModeData: EMIPaymentModeData

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
        setViews(view)
    }

    private fun setViews(view: View) {
        bankListRecyclerView = view.findViewById(R.id.emi_bank_list_rv)
        //TODO manage selection of all payment modes
        showEMIListBasedOnSelection(
            Constants.EMI_CC_TYPE
        )
    }

    private fun showEMIList(listOfCCBank: List<Issuer>) {
        val adapter = EMIBankRecyclerViewAdapter(listOfCCBank, getItemClickListener())
        bankListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        bankListRecyclerView.addItemDecoration(
            DividerItemDecoration(
                ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_divider)
            )
        )
        bankListRecyclerView.adapter = adapter
    }

    private fun showEMIListBasedOnSelection(selection: String) {
        when (selection) {
            Constants.EMI_CC_TYPE -> {
                showEMIList(getListOfCCBanks(Constants.EMI_CC_TYPE))
            }

            Constants.EMI_DC_TYPE -> {
                // Handle DC type if needed
                showEMIList(getListOfCCBanks(Constants.EMI_DC_TYPE))
            }

            else -> {
                // Handle other types or default case cardless
            }
        }
        val listOfCCBank = getListOfCCBanks(Constants.EMI_CC_TYPE)
    }

    private fun getListOfCCBanks(emiCcType: String): List<Issuer> {
        return emiPaymentModeData.issuers.filter { issuer ->
            issuer.issuer_type.equals(emiCcType, true)
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

    private fun mapBanKLogo(){

    }

}