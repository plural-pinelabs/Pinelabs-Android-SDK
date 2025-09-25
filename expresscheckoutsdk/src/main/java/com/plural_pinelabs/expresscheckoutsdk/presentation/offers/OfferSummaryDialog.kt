package com.plural_pinelabs.expresscheckoutsdk.presentation.offers

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetail


class OfferSummaryDialog : DialogFragment() {

    private lateinit var bankLogoMap: HashMap<String, String>
    private lateinit var banKTitleToCodeMap: HashMap<String, String>
    private lateinit var bankNameKeyList: List<String>
    private lateinit var allTv: TextView
    private lateinit var creditTv: TextView
    private lateinit var debitTv: TextView


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.offer_summary_layout)

        val window = dialog.window
        window?.apply {
            val metrics = Resources.getSystem().displayMetrics
            val screenHeight = metrics.heightPixels
            val desiredHeight = (screenHeight * 0.90).toInt()
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, desiredHeight)
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setGravity(Gravity.BOTTOM)

        }
        setupUI(dialog.findViewById(R.id.offer_summary_parent_layout))


        return dialog
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI(view: View) {

        val offerRecyclerView = view.findViewById<RecyclerView>(R.id.offers_recycler_view)
        val closeIcon = view.findViewById<ImageView>(R.id.x_icon)
        allTv = view.findViewById(R.id.all_filter_chip)
        creditTv = view.findViewById(R.id.credit_filter_chip)
        debitTv = view.findViewById(R.id.debit_filter_chip)
        mapBanKLogo()


        val filters = listOf(allTv, creditTv, debitTv)

        // Set default selection
        allTv.isSelected = true
        filterData("", offerRecyclerView)
        val offerList = getListOfTenure("")
        allTv.text = "All (${offerList.size})"
        if (offerList.count { it.type.contains("CC_bank", true) } == 0) {
            creditTv.visibility = View.GONE
        }
        if (offerList.count { it.type.contains("DC_Bank", true) } == 0) {
            debitTv.visibility = View.GONE
        }
        creditTv.text = "Credit Cards (${offerList.count { it.type.contains("CC_bank", true) }})"
        debitTv.text = "Debit Cards (${offerList.count { it.type.contains("DC_Bank", true) }})"

        filters.forEach { view ->
            view.setOnClickListener {
                filters.forEach { it.isSelected = false }
                view.isSelected = true

                when (view.id) {
                    R.id.all_filter_chip -> {
                        filterData("", offerRecyclerView)
                        allTv.isSelected = true
                        //   allTv.setBackgroundResource(R.drawable.input_field_border)
                    }

                    R.id.credit_filter_chip -> {
                        filterData("CC_bank", offerRecyclerView)
                        creditTv.isSelected = true
                    }

                    R.id.debit_filter_chip -> {
                        filterData("DC_Bank", offerRecyclerView)
                        debitTv.isSelected = true
                    }
                }
            }
        }


        closeIcon.setOnClickListener {
            dismiss()
        }
    }


    private fun filterData(type: String, offerRecyclerView: RecyclerView) {
        Log.d("Filter", "Filtering for: $type")

        offerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = OfferRVAdapter(
            requireContext(), getListOfTenure(type), getItemListener(), bankLogoMap,
            bankNameKeyList,
            banKTitleToCodeMap,
        )
        offerRecyclerView.adapter = adapter
        // Your filtering logic here
    }

    private fun getItemListener(): ItemClickListener<OfferDetail> {
        return object : ItemClickListener<OfferDetail> {
            override fun onItemClick(position: Int, item: OfferDetail) {
                //handle navigation based on item type
                ExpressSDKObject.setSelectedOfferDetail(item)
                dismiss()
                if (item.isInstantSaving) {
                    findNavController().navigate(R.id.action_paymentModeFragment_to_EMICardDetailsFragment)
                    //take directly to EMIFragment
                } else {
                    findNavController().navigate(R.id.action_paymentModeFragment_to_tenureSelectionFragment)
                }
            }
        }
    }


    private fun getListOfTenure(type: String): ArrayList<OfferDetail> {
        val emiPaymentModeData = ExpressSDKObject.getEMIPaymentModeData()
        val offersList: ArrayList<OfferDetail> = arrayListOf()
        val offerDetails = if (type.isEmpty()) {
            emiPaymentModeData?.offerDetails
        } else {
            emiPaymentModeData?.offerDetails?.filter { it.type.contains(type, true) }
        }
        offerDetails?.forEach { offerDetail ->
            offerDetail.offerTitle =
                Utils.getTitleForEMI(requireContext(), offerDetail.issuer) + " EMI"
            offersList.add(offerDetail)
            val cashbackTenure = offerDetail.tenureOffers?.find { it.tenureId == "7" }
            if (cashbackTenure != null) {
                val cashBackOfferDetail = offerDetail.copy()
                cashBackOfferDetail.offerTitle =
                    Utils.getTitleForEMI(requireContext(), offerDetail.issuer)
                cashBackOfferDetail.isInstantSaving = true
                offersList.add(cashBackOfferDetail)
            }
        }
        return offersList.sortedByDescending { offerDetail ->
            val saving =
                if (offerDetail.isInstantSaving) {
                    offerDetail.tenureOffers?.find { it.tenureId == "7" }?.let {
                        it.discountAmount + it.cashbackAmount
                    } ?: 0
                } else {
                    offerDetail.maxSaving
                }
            saving
        }.let { ArrayList(it) }

    }


    private fun mapBanKLogo() {
        bankLogoMap = Utils.getBankLogoHashMap()
        bankNameKeyList = Utils.getListOfBanKTitle()
        banKTitleToCodeMap = Utils.bankTitleAndCodeMapper()
    }


}

