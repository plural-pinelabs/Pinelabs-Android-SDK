package com.plural_pinelabs.expresscheckoutsdk.presentation.landing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode

class PaymentModeRecyclerViewAdapter(
    private val context: Context,
    private val paymentModeDataList: List<PaymentMode>,
    checkIfPBPEnabled: Boolean, // TODO handle once the PBP features comes
    private val paymentModeSelectionCallback: ItemClickListener<PaymentMode>?
) :
    RecyclerView.Adapter<PaymentModeRecyclerViewAdapter.PaymentModeViewHolder>() {

    inner class PaymentModeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setItem(item: PaymentMode, position: Int) {
            val modeImage: ImageView = itemView.findViewById(R.id.payment_icon)
            val modeName: TextView = itemView.findViewById(R.id.payment_mode)
            val modeDescription: TextView = itemView.findViewById(R.id.payment_mode_description)
            val parentLayout: ConstraintLayout = itemView.findViewById(R.id.payment_mode_parent)
            val recyclerViewPaymentOptionData = Utils.mapPaymentModes(item)
            if (recyclerViewPaymentOptionData.paymentOption == -1 || recyclerViewPaymentOptionData.paymentImage == -1 || recyclerViewPaymentOptionData.description == -1) {
                return
            }
            modeName.text = context.getString(recyclerViewPaymentOptionData.paymentOption)
            modeImage.setImageResource(recyclerViewPaymentOptionData.paymentImage)
            modeDescription.text = context.getString(recyclerViewPaymentOptionData.description)
            parentLayout.backgroundTintList =
                AppCompatResources.getColorStateList(context, R.color.colorPrimary)
            parentLayout.setOnClickListener {
                paymentModeSelectionCallback?.onItemClick(position = position, item)

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentModeViewHolder {
        return PaymentModeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.payment_item_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return paymentModeDataList.size
    }

    override fun onBindViewHolder(holder: PaymentModeViewHolder, position: Int) {
        holder.setItem(paymentModeDataList[position], position)
    }
}