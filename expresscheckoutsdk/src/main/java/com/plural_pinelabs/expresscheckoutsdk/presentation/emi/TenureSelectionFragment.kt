package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.data.model.EMIPaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration

class TenureSelectionFragment : Fragment() {

    private lateinit var goBackButton: ImageView
    private lateinit var tenureListRecyclerView: RecyclerView
    private var selectedIssuerId: String? = null
    private var issuer: Issuer? = null
    private var emiPaymentModeData: EMIPaymentModeData? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tenure_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emiPaymentModeData = ExpressSDKObject.getEMIPaymentModeData()
        selectedIssuerId = arguments?.getString("selected_issuer_id")
        selectedIssuerId?.let {
            emiPaymentModeData?.issuers?.filter {
                it.id == selectedIssuerId
            }?.let { issuerList ->
                if (issuerList.isNotEmpty()) {
                    issuer = issuerList[0]
                }
            }
        }
        setViews(view)
        showListOfTenure()
    }

    private fun setViews(view: View) {
        goBackButton = view.findViewById(R.id.back_button)
        tenureListRecyclerView = view.findViewById(R.id.tenure_list_rv)
    }

    private fun showListOfTenure() {
        tenureListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val list = issuer?.tenures?.sortedBy { it.tenure_value }
        list?.let {
            if (it.isEmpty()) {
                return // TODO handle error no tenure in there
            }
            val filteredList = it.filter { tenure ->
                tenure.tenure_value != 0 || !tenure.name.contains("No EMI", true)
            }
            val adapter = EMITenureListAdapter(
                requireContext(), filteredList, getWalletClickListener()
            )
            tenureListRecyclerView.adapter = adapter
        }
    }

    private fun getWalletClickListener(): ItemClickListener<Tenure> {
        return object : ItemClickListener<Tenure> {
            override fun onItemClick(position: Int, item: Tenure) {
                //TODO handle click of wallet item
            }
        }
    }
}