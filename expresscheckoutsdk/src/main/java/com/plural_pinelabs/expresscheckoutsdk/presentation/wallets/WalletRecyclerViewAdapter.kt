package com.plural_pinelabs.expresscheckoutsdk.presentation.wallets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletBank

class WalletRecyclerViewAdapter(
    private val walletList: List<WalletBank>,
    private val walletSelectionCallback: ItemClickListener<WalletBank>?,
) :
    RecyclerView.Adapter<WalletRecyclerViewAdapter.WalletViewHolder>() {

    inner class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setItem(item: WalletBank, position: Int) {
            val walletLogo: ImageView = itemView.findViewById(R.id.wallet_logo)
            val walletTitle: TextView = itemView.findViewById(R.id.wallet_title)
            val walletParentItem: ConstraintLayout =
                itemView.findViewById(R.id.wallet_parent_item_layout)
            walletLogo.setImageResource(item.bankImage)
            walletTitle.text = item.bankName
            walletParentItem.setOnClickListener {
                walletSelectionCallback?.onItemClick(position, item)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        return WalletViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.wallet_item_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return walletList.size
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        holder.setItem(walletList[position], position)
    }
}