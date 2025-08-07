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
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.NO_COST_EMI
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
        val valuesMap: ArrayList<Pair<String, String>> = arrayListOf()
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
        val discount = Pair(
            getDiscountLabel(tenure),
            Utils.convertToRupeesWithSymobl(
                requireContext(),
                tenure?.total_discount_amount?.value
            )
        )
        val cashback = Pair(
            getCashbackLabel(tenure),
            Utils.convertToRupeesWithSymobl(
                requireContext(),
                tenure?.total_subvention_amount?.value
            )
        )
        val emiInterest = Pair(
            getString(R.string.emi_interest),
            Utils.convertToRupeesWithSymobl(
                requireContext(),
                tenure?.interest_amount?.value
            )
        )

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
        if (convenienceFees.second.isNotEmpty() && tenure?.total_discount_amount?.value != null && tenure.total_discount_amount.value > 0) {
            valuesMap.add(discount)
            index = valuesMap.size - 1
        }
        if (convenienceFees.second.isNotEmpty() && tenure?.total_subvention_amount?.value != null && tenure.total_subvention_amount.value > 0)
            valuesMap.add(cashback)

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


        val subtotalAdapter = SubtotalRVAdapter(valuesMap, index)
        subtotalRecyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        subtotalRecyclerView.adapter = subtotalAdapter
    }

    private fun getDiscountLabel(tenure: Tenure?): String {
        return if (tenure != null && tenure.tenure_value == 7) { // INSTANT CASHBACK/DISCOUNT
            getString(R.string.no_cost_discount)
        } else
            return getString(R.string.discount)
    }

    private fun getCashbackLabel(tenure: Tenure?): String {
        return if (tenure != null && tenure.emi_type.equals(
                NO_COST_EMI,
                true
            )
        ) { // INSTANT CASHBACK/DISCOUNT
            getString(R.string.no_cost_discount)
        } else
            return getString(R.string.cashback)
    }

}

