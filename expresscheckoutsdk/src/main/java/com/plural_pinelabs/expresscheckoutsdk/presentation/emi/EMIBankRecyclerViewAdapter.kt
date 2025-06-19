package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer

class EMIBankRecyclerViewAdapter(
    private val list: List<Issuer>,
    private val emiBankSelectionCallback: ItemClickListener<Issuer>?
) :
    RecyclerView.Adapter<EMIBankRecyclerViewAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setItem(item: Issuer, position: Int) {
            val logo: ImageView = itemView.findViewById(R.id.bank_logo)
            val title: TextView = itemView.findViewById(R.id.bank_title)
            val parentItem: ConstraintLayout =
                itemView.findViewById(R.id.parent_item_layout)
            //  logo.setImageResource(item.bankImage)
            title.text = item.display_name
            parentItem.setOnClickListener {
                emiBankSelectionCallback?.onItemClick(position, item)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.emi_bank_item_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.setItem(list[position], position)
    }
}