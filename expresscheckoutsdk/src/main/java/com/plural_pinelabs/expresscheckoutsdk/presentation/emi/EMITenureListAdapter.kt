package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure

class EMITenureListAdapter(
    private val context: Context,
    private val tenures: List<Tenure>,
    private val emiBankSelectionCallback: ItemClickListener<Tenure?>?
) :
    RecyclerView.Adapter<EMITenureListAdapter.ItemViewHolder>() {

    private var selectedPosition = -1 // No selection by default


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setItem(item: Tenure, position: Int) {
            val tenureRadioButton: RadioButton = itemView.findViewById(R.id.tenure_radio_button)
            val tenureAmountTextView: TextView = itemView.findViewById(R.id.tenure_amount_text_view)
            val tenureXMonthsTextView: TextView = itemView.findViewById(R.id.tenure_x_months_view)
            val emiTypeTextView: TextView = itemView.findViewById(R.id.tenure_emi_type_text_view)
            val bestValueLabel: TextView = itemView.findViewById(R.id.best_value_tag)
            val recommendedLabel: TextView = itemView.findViewById(R.id.recommended_tag)
            val saveLayout: LinearLayout = itemView.findViewById(R.id.saving_layout)
            val saveAmountTv: TextView = itemView.findViewById(R.id.saving_text_value)

            val tenureTotalPayableTextView: TextView =
                itemView.findViewById(R.id.tenure_payable_amount_value_text_view)
            val processingFeesTextView: TextView =
                itemView.findViewById(R.id.tenure_processing_fee_text_view)


            val maxDiscount: String = item.let { tenure ->
                tenure.total_discount_amount?.value?.let {
                    Utils.convertToRupees(itemView.context, it)
                } ?: tenure.total_subvention_amount?.value?.let {
                    Utils.convertToRupees(itemView.context, it)
                }
            } ?: "error"
            saveLayout.visibility = View.GONE
            if (!maxDiscount.contains("error", true)) {
                saveLayout.visibility = View.VISIBLE
                saveAmountTv.text = maxDiscount
            } else {
                saveLayout.visibility = View.GONE
            }
            tenureAmountTextView.text =
                Utils.convertToRupees(context, item.monthly_emi_amount?.value)
            tenureXMonthsTextView.text = context.getString(
                R.string.x_3_months,
                item.tenure_value.toString()
            )
            if (item.processing_fee_details?.amount == null) {
                processingFeesTextView.visibility = View.INVISIBLE
            } else {
                processingFeesTextView.text = context.getString(
                    R.string.x_one_time_processing_fee,
                    Utils.convertToRupees(context, item.processing_fee_details.amount.value)
                )
                processingFeesTextView.visibility = View.VISIBLE
            }
            tenureTotalPayableTextView.text =
                Utils.convertToRupees(context, item.net_payment_amount?.value)
            if (item.isBestValue) {
                bestValueLabel.visibility = View.VISIBLE
            } else if (item.isRecommended) {
                recommendedLabel.visibility = View.VISIBLE
            }

            emiTypeTextView.text = item.emi_type.let {
                when (it) {
                    "STANDARD" -> {
                        emiTypeTextView.visibility = View.GONE
                        "" //return
                    }

                    "NO_COST" -> context.getString(R.string.no_cost)
                    "LOW_COST" -> {
                        val percentage = item.interest_rate_percentage
                        val text = if (percentage != null) {
                            String.format(
                                context.getString(R.string.low_interest_with_value),
                                percentage.toString()
                            )
                        } else {
                            String.format(context.getString(R.string.low_interest))
                        }
                        text
                    }

                    else -> {
                        emiTypeTextView.visibility = View.GONE
                        ""//return empty
                    }
                }
            }

            // Set the radio button state based on the selected positiondioButton.isChecked = position == selectedPosition
            tenureRadioButton.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                emiBankSelectionCallback?.onItemClick(position, item)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.tenure_item_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return tenures.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.setItem(tenures[position], position)
    }
}