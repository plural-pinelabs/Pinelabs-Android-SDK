package com.plural_pinelabs.expresscheckoutsdk.presentation.ordersummary

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.R

class SubtotalRVAdapter(
    private val list: List<Pair<String, String>>,
    private val index: Int,
) : RecyclerView.Adapter<SubtotalRVAdapter.ItemViewHolder>() {


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("StringFormatInvalid", "SetTextI18n")
        fun setItem(item: Pair<String, String>, position: Int) {
            val titleTv = itemView.findViewById<android.widget.TextView>(R.id.label)
            val valueTv = itemView.findViewById<android.widget.TextView>(R.id.value)
            titleTv.text = item.first
            valueTv.text = item.second
            if (position >= index && index != -1) {
                valueTv.text = "-${item.second}"
                valueTv.setTextColor(itemView.context.getColor(R.color.green_009E54))
            } else {
                valueTv.setTextColor(itemView.context.getColor(R.color.black_text_80))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.order_summary_values_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.setItem(list[position], position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}