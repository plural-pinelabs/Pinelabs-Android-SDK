package com.plural_pinelabs.expresscheckoutsdk.presentation.ordersummary

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.Window
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure


class TopSheetDialogFragment : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.order_summary_layout)
        dialog.window?.peekDecorView()
        dialog.window?.apply {
            val height = resources.displayMetrics.heightPixels
            val finalHeight = if ((ExpressSDKObject.getFetchData()?.cartDetails?.cart_items?.size
                    ?: 0) > 5
            ) {
                (height * 0.75).toInt()
            } else {
                LayoutParams.WRAP_CONTENT
            }
            val desiredHeight = (height * 0.75).toInt()
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, finalHeight)
            setGravity(Gravity.TOP)

            setBackgroundDrawable(Color.WHITE.toDrawable())
        }

        setupUI(dialog.findViewById(R.id.order_summary_layout_parent))

        return dialog
    }

    private fun setupUI(root: View) {
        val productsRecyclerView: RecyclerView = root.findViewById(R.id.product_recycler_view)
        val subtotalRecyclerView: RecyclerView = root.findViewById(R.id.subtotal_recycler_view)
        val subTotalDivider: View = root.findViewById(R.id.subtotal_divider)
        val closeButton: View = root.findViewById(R.id.order_close_icon)
        val totalAmountLabel: TextView = root.findViewById(R.id.total_amount_label)
        val totalAmountValue: TextView = root.findViewById(R.id.total_amount_value)
        val processingFeeLabel: TextView = root.findViewById(R.id.processing_gst_info)

        closeButton.setOnClickListener {
            dismiss()
        }
        val data = ExpressSDKObject.getFetchData()
        val cartItems = data?.cartDetails?.cart_items
        if (cartItems.isNullOrEmpty()) {
            productsRecyclerView.visibility = View.GONE
            subtotalRecyclerView.visibility = View.GONE
        } else {
            subTotalDivider.visibility = View.VISIBLE
            val productAdapter = ProductRVAdapter(requireContext(), cartItems)
            productsRecyclerView.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            productsRecyclerView.adapter = productAdapter
        }
        val valuesMap: ArrayList<Pair<String, Any?>> = arrayListOf()
        val subtotal =
            Pair(
                getString(R.string.subtotal),
                Utils.convertToRupeesWithSymobl(
                    requireContext(),
                    ExpressSDKObject.getOriginalOrderAmount()
                )
            )
        val convenienceFees =
            Pair(
                getString(R.string.convenience_fee),
                Utils.convertToRupeesWithSymobl(
                    requireContext(),
                    ExpressSDKObject.getConvenienceFee()
                )
            )
        val convenienceFeesGST =
            Pair(
                getString(R.string.convenience_fee_gst),
                Utils.convertToRupeesWithSymobl(
                    requireContext(),
                    ExpressSDKObject.getConvenienceFeeGst()
                )
            )
        val tenure = ExpressSDKObject.getSelectedTenure()
        val emiInterest = Pair(
            getString(R.string.emi_interest),
            Utils.convertToRupeesWithSymobl(
                requireContext(),
                tenure?.interest_amount?.value
            )
        )
        var listOfPairs = getMapPairs(tenure)

        if (ExpressSDKObject.getSelectedTenure() != null) {
            totalAmountLabel.text = getString(R.string.total_emi_cost)
        } else {
            totalAmountLabel.text = getString(R.string.total_amount)
        }

        var index = -1
        valuesMap.add(subtotal)
        if (tenure?.interest_amount?.amount != null && tenure.interest_amount.value > 0)
            valuesMap.add(emiInterest)
        if (convenienceFees.second.isNotEmpty() && ExpressSDKObject.getConvenienceFee() != null && ExpressSDKObject.getConvenienceFee()!! > 0)
            valuesMap.add(convenienceFees)
        if (convenienceFees.second.isNotEmpty() && ExpressSDKObject.getConvenienceFeeGst() != null && ExpressSDKObject.getConvenienceFeeGst()!! > 0)
            valuesMap.add(convenienceFeesGST)
        index = valuesMap.size
        valuesMap.addAll(listOfPairs)
        if (ExpressSDKObject.getSelectedTenure() != null) {
            totalAmountValue.text = Utils.convertToRupeesWithSymobl(
                requireContext(),
                ExpressSDKObject.getSelectedTenure()?.total_emi_amount?.value
                    ?: ExpressSDKObject.getPayableAmount()
            )
        } else
            totalAmountValue.text = Utils.convertToRupeesWithSymobl(
                requireContext(),
                ExpressSDKObject.getPayableAmount()
            )
        if ((tenure?.processing_fee_details?.amount?.value ?: 0) > 0) {
            processingFeeLabel.text = String.format(
                getString(R.string.processing_fee_info), Utils.convertToRupeesWithSymobl(
                    requireContext(),
                    tenure?.processing_fee_details?.amount?.value ?: 0
                ), ""
            )
            processingFeeLabel.visibility = View.VISIBLE
        }

        val subtotalAdapter = SubtotalRVAdapter(valuesMap, index)
        subtotalRecyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        subtotalRecyclerView.adapter = subtotalAdapter
    }

    private fun getMapPairs(tenure: Tenure?): List<Pair<String, Any?>> {

        val discountValue = if (tenure?.discount?.discount_type == "DEFERRED")
            tenure.discount.amount?.value ?: 0 else 0

        val subventionValue = if (
            tenure?.details?.getOrNull(0)?.subvention?.offer_type == "NO_COST" &&
            tenure.details[0].subvention?.subvention_type == "POST"
        ) tenure.total_subvention_amount?.value ?: 0 else 0

        val totalCashback = discountValue + subventionValue

        val emiDetails =
            listOfNotNull(
                // Conditional discounts
                if (
                    tenure?.details?.getOrNull(0)?.subvention?.offer_type == "LOW_COST" &&
                    tenure.details[0].subvention?.subvention_type == "POST"
                ) Pair(
                    "EMI Discount",
                    Utils.convertToRupeesWithSymobl(
                        requireContext(),
                        tenure.total_subvention_amount?.value
                    )
                ) else null,

                if (
                    tenure?.details?.getOrNull(0)?.subvention?.offer_type == "LOW_COST" &&
                    tenure.details[0].subvention?.subvention_type == "INSTANT"
                ) Pair(
                    "Instant Discount",
                    Utils.convertToRupeesWithSymobl(
                        requireContext(),
                        tenure.total_subvention_amount?.value
                    )
                ) else null,

                if (
                    tenure?.details?.getOrNull(0)?.subvention?.offer_type == "NO_COST" &&
                    tenure.details[0].subvention?.subvention_type != "POST"
                ) Pair(
                    "No Cost EMI Discount",
                    Utils.convertToRupeesWithSymobl(
                        requireContext(),
                        tenure.total_subvention_amount?.value
                    )
                ) else null,
                // Instant discount override
                if (
                    tenure?.discount?.discount_type == "INSTANT" &&
                    tenure.discount.amount?.value != null
                ) Pair(
                    "Instant Discount",
                    Utils.convertToRupeesWithSymobl(
                        requireContext(),
                        tenure.discount.amount.value
                    )
                ) else null,

                if (totalCashback > 0) Pair(
                    "Cashback",
                    Utils.convertToRupeesWithSymobl(requireContext(), totalCashback)
                ) else null
            )
        return emiDetails
// Dispatch or update state
    }

}

