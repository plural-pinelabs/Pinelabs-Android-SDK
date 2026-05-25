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
import com.plural_pinelabs.expresscheckoutsdk.BuildConfig
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode
import com.plural_pinelabs.expresscheckoutsdk.presentation.upi.resolveHighestUpiOfferDiscount

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
            val modeSavingTag: TextView = itemView.findViewById(R.id.payment_mode_saving_tag)
            val parentLayout: ConstraintLayout = itemView.findViewById(R.id.payment_mode_parent)
            val recyclerViewPaymentOptionData = Utils.mapPaymentModes(item)
            if (recyclerViewPaymentOptionData.paymentOption == -1 || recyclerViewPaymentOptionData.paymentImage == -1 || recyclerViewPaymentOptionData.description == -1) {
                return
            }
            modeName.text = context.getString(recyclerViewPaymentOptionData.paymentOption)
            modeImage.setImageResource(recyclerViewPaymentOptionData.paymentImage)
            modeDescription.text = context.getString(recyclerViewPaymentOptionData.description)
            bindSavingTag(item, modeSavingTag)
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

    private fun bindSavingTag(item: PaymentMode, savingTag: TextView) {
        if (!BuildConfig.ENABLE_UPI_ICB_SAVINGS_UI) {
            savingTag.visibility = View.GONE
            return
        }

        if (!item.paymentModeId.equals(PaymentModes.UPI.paymentModeID, ignoreCase = true)) {
            savingTag.visibility = View.GONE
            return
        }

        val savingsAmount = resolveHighestUpiOfferDiscount(ExpressSDKObject.getFetchData())
            ?: extractSavingsAmount(item.paymentModeData)
        savingTag.text = if (savingsAmount != null && savingsAmount > 0) {
            context.getString(
                R.string.save_rs_x,
                Utils.convertToRupeesWithSymobl(context, savingsAmount)
            )
        } else {
            context.getString(R.string.upi_default_save_chip)
        }
        savingTag.visibility = View.VISIBLE
    }

    private fun extractSavingsAmount(rawData: Any?): Int? {
        return when (rawData) {
            is Number -> rawData.toInt()
            is String -> rawData.replace("[^0-9]".toRegex(), "").toIntOrNull()
            is Map<*, *> -> {
                val prioritized = rawData.entries.firstNotNullOfOrNull { entry ->
                    val key = entry.key?.toString()?.lowercase() ?: return@firstNotNullOfOrNull null
                    if (key in SAVINGS_KEYS) {
                        extractSavingsAmount(entry.value)
                    } else {
                        null
                    }
                }
                prioritized ?: rawData.values.firstNotNullOfOrNull { value ->
                    extractSavingsAmount(value)
                }
            }

            is List<*> -> rawData.firstNotNullOfOrNull { value ->
                extractSavingsAmount(value)
            }

            else -> null
        }
    }

    private companion object {
        val SAVINGS_KEYS = setOf(
            "maxsaving",
            "max_saving",
            "saving",
            "savings",
            "cashback",
            "cashback_amount",
            "instant_cashback",
            "instantcashback",
            "discount",
            "discount_amount",
            "eligible_amount",
            "eligibleamount"
        )
    }
}
