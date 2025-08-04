package com.plural_pinelabs.expresscheckoutsdk.presentation.offers

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
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

    private fun setupUI(view: View) {

        val offerRecyclerView = view.findViewById<RecyclerView>(R.id.offers_recycler_view)
        val closeIcon = view.findViewById<ImageView>(R.id.x_icon)
        closeIcon.setOnClickListener {
            dismiss()
        }

        offerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = OfferRVAdapter(requireContext(), getListOfTenure(), getItemListener())
        offerRecyclerView.adapter = adapter
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


    private fun getListOfTenure(): ArrayList<OfferDetail> {
        val emiPaymentModeData = ExpressSDKObject.getEMIPaymentModeData()
        val offersList: ArrayList<OfferDetail> = arrayListOf()
        emiPaymentModeData?.offerDetails?.forEach { offerDetail ->
            offerDetail.offerTitle =
                Utils.getTitleForEMI(requireContext(), offerDetail.issuer) + " EMI"
            offersList.add(offerDetail)
            val cashbackTenure = offerDetail.tenureOffers.find { it.tenureId == "7" }
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
                    offerDetail.tenureOffers.find { it.tenureId == "7" }?.let {
                        it.discountAmount + it.cashbackAmount
                    } ?: 0
                } else {
                    offerDetail.maxSaving
                }
            saving
        }.let { ArrayList(it) }

    }


}

