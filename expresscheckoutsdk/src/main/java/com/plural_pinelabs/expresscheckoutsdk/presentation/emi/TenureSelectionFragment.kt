package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_IMAGES
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ISSUE_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TENURE_ID
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.EMIPaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure

class TenureSelectionFragment : Fragment() {

    private lateinit var goBackButton: ImageView
    private lateinit var tenureListRecyclerView: RecyclerView
    private lateinit var emiPayingInMonthsTv: TextView
    private lateinit var logo: ImageView

    private var selectedIssuerId: String? = null
    private var issuer: Issuer? = null
    private var emiPaymentModeData: EMIPaymentModeData? = null
    private var selectedTenure: Tenure? = null
    private lateinit var bankLogoMap: HashMap<String, String>
    private lateinit var banKTitleToCodeMap: HashMap<String, String>
    private lateinit var bankNameKeyList: List<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tenure_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapBanKLogo()
        setEMIIssuer()
        setViews(view)
        showListOfTenure()
    }

    private fun setEMIIssuer() {
        emiPaymentModeData = ExpressSDKObject.getEMIPaymentModeData()
        selectedIssuerId = arguments?.getString(TENURE_ID)
        selectedIssuerId?.let {
            emiPaymentModeData?.issuers?.filter {
                it.id == selectedIssuerId
            }?.let { issuerList ->
                if (issuerList.isNotEmpty()) {
                    issuer = issuerList[0]
                }
            }
        }
    }

    private fun setViews(view: View) {
        goBackButton = view.findViewById(R.id.back_button)
        tenureListRecyclerView = view.findViewById(R.id.tenure_list_rv)
        emiPayingInMonthsTv = view.findViewById(R.id.emi_info_text)
        logo = view.findViewById(R.id.logo)
        loadBankLogo()
        view.findViewById<Button>(R.id.continue_btn).setOnClickListener {
            handleContinueButtonClick()
        }
    }

    private fun loadBankLogo() {
        issuer?.let { item ->
            val imageTitle =
                bankNameKeyList.find {
                    it.contains(
                        item.display_name.removeSuffix(" BANK"),
                        ignoreCase = true
                    )
                }

            if (imageTitle != null) {
                val imageUrl = BASE_IMAGES + bankLogoMap[banKTitleToCodeMap[imageTitle]]
                val imageLoader = ImageLoader.Builder(requireContext())
                    .components {
                        add(SvgDecoder.Factory())
                    }
                    .crossfade(true)
                    .build()
                val request = ImageRequest.Builder(requireContext())
                    .data(imageUrl)
                    .target(logo)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
                imageLoader.enqueue(request)
            }

        }
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
                requireContext(), filteredList, getTenureClickListener()
            )
            tenureListRecyclerView.adapter = adapter
        }
    }

    private fun getTenureClickListener(): ItemClickListener<Tenure?> {
        return object : ItemClickListener<Tenure?> {
            override fun onItemClick(position: Int, item: Tenure?) {
                //TODO handle click of wallet item
                selectedTenure = item
            }
        }
    }

    private fun handleContinueButtonClick() {
        if (selectedTenure == null) {
            // TODO handle error no tenure selected
            return
        }
        val bundle = Bundle()
        bundle.putString(ISSUE_ID, selectedIssuerId)
        bundle.putString(TENURE_ID, selectedTenure?.tenure_id)
        findNavController().navigate(
            R.id.action_tenureSelectionFragment_to_EMICardDetailsFragment,
            bundle
        )
    }

    private fun mapBanKLogo() {
        bankLogoMap = Utils.getBankLogoHashMap()
        bankNameKeyList = Utils.getListOfBanKTitle()
        banKTitleToCodeMap = Utils.bankTitleAndCodeMapper()
    }
}