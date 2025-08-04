package com.plural_pinelabs.expresscheckoutsdk.presentation.offers

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetail

class OfferRVAdapter(
    private val context: Context,
    private val listOfTenure: ArrayList<OfferDetail>,
    private val itemClickListener: ItemClickListener<OfferDetail>
) : RecyclerView.Adapter<OfferRVAdapter.ItemViewHolder>() {


    private val imageLoader = ImageLoader.Builder(context)
        .components { add(SvgDecoder.Factory()) }
        .crossfade(true)
        .build()


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("StringFormatInvalid")
        fun setItem(item: OfferDetail, position: Int) {
            val offerLabel = itemView.findViewById<android.widget.TextView>(R.id.offer_label)
            val offerSubtitle = itemView.findViewById<android.widget.TextView>(R.id.offers_subtitle)
            val maxDiscountLabel = itemView.findViewById<android.widget.TextView>(R.id.saving_label)
            val availOfferBtn = itemView.findViewById<android.widget.TextView>(R.id.avail_offer_btn)
            val emiTag :ImageView = itemView.findViewById(R.id.emi_tag)
            offerLabel.text = item.offerTitle
            offerSubtitle.text = if (item.isInstantSaving) {
                context.getString(R.string.instant_discount_on_full_payment)
            } else {
                val emiTenureList =
                    item.tenureOffers.filter { it.tenureId!="7" }.sortedBy { it.tenureId }.joinToString(separator = ",") { it.fullTenure.tenure_value.toString() } ?: ""
                String.format(
                    context.getString(R.string.applicable_on_emitenurelist_emi_tenures),
                    emiTenureList
                )
            }
            val maxSavings = if (item.isInstantSaving) {
                emiTag.visibility = View.GONE
                val tenure = item.tenureOffers.find { it.tenureId == "7" }
                val savings: Int =
                    tenure?.let { tenure.discountAmount.plus(tenure.cashbackAmount) } ?: 0
                savings
            } else {
                item.maxSaving
            }
            maxDiscountLabel.text = context.getString(
                R.string.save_rs_x,
                Utils.convertToRupeesWithSymobl(
                    context, maxSavings
                )
            )
            availOfferBtn.setOnClickListener {
                itemClickListener.onItemClick(position, item)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.offers_item_layour, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listOfTenure.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.setItem(listOfTenure[position], position)
    }

    override fun getItemViewType(position: Int): Int {
        //Note: This is a temporary work around to avoid icon being repeated as we are fetching icons
        // from a server upon scroll the previous icon items are shown only
        return position
    }


}