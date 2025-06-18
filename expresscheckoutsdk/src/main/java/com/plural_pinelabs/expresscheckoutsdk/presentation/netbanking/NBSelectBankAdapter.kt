package com.plural_pinelabs.expresscheckoutsdk.presentation.netbanking

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.data.model.NetBank

class NBSelectBankAdapter(
    private var nbList: List<NetBank>,
    private val walletSelectionCallback: ItemClickListener<NetBank>?,
    private val imageLoader: ImageLoader?
) : RecyclerView.Adapter<NBSelectBankAdapter.NetBankItemViewHolder>() {

    inner class NetBankItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setItem(item: NetBank, position: Int) {
            val walletLogo: ImageView = itemView.findViewById(R.id.bank_logo)
            val walletTitle: TextView = itemView.findViewById(R.id.bank_title)
            val walletParentItem: ConstraintLayout =
                itemView.findViewById(R.id.bank_parent_item_layout)
            //TODO fetch image from URL or resource
            // walletLogo.setImageResource(item.bankImage)

            imageLoader?.let {
                walletLogo.load(item.bankImage.toUri(), it) {
                    crossfade(true)
                    placeholder(R.drawable.wallet_yes_bank)
                }
            }
            walletTitle.text = item.bankName
            walletParentItem.setOnClickListener {
                walletSelectionCallback?.onItemClick(position, item)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetBankItemViewHolder {
        return NetBankItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.netbanking_item_layout, parent, false)
        )
    }

    //TODO late change to diff util for better performance but it may add some overhead
    @SuppressLint("NotifyDataSetChanged")
    fun updateListWithNewItems(newList: List<NetBank>) {
        nbList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return nbList.size
    }

    override fun onBindViewHolder(holder: NetBankItemViewHolder, position: Int) {
        holder.setItem(nbList[position], position)
    }
}