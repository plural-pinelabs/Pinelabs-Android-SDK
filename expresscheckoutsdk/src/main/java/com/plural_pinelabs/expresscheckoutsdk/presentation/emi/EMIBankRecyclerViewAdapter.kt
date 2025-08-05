package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.load
import coil.request.CachePolicy
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_IMAGES
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer

class EMIBankRecyclerViewAdapter(
    private val context: Context,
    private val list: List<Issuer>,
    private val emiBankSelectionCallback: ItemClickListener<Issuer>?,
    private val bankLogoMap: HashMap<String, String>,
    private val bankNameKeyList: List<String>,
    private val banKTitleToCodeMap: HashMap<String, String>,
) : RecyclerView.Adapter<EMIBankRecyclerViewAdapter.ItemViewHolder>() {


    private val imageLoader = ImageLoader.Builder(context)
        .components { add(SvgDecoder.Factory()) }
        .crossfade(true)
        .build()


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setItem(item: Issuer, position: Int) {
            val imageTitle = bankNameKeyList.find {
                it.contains(
                    item.display_name.removeSuffix(" BANK"), ignoreCase = true
                )
            }
            val logo: ImageView = itemView.findViewById(R.id.bank_logo)
            val title: TextView = itemView.findViewById(R.id.bank_title)
            val saveLayout: LinearLayout = itemView.findViewById(R.id.saving_layout)
            val saveAmountTv: TextView = itemView.findViewById(R.id.saving_text_value)


            val maxDiscount = if ((item.maxDiscountAmount ?: 0) > 0) {
                Utils.convertToRupeesWithSymobl(
                    context,
                    item.maxDiscountAmount ?: 0,
                )
            } else {
                "error"
            }


            saveLayout.visibility = View.GONE
            if (!maxDiscount.contains("error", true)) {
                saveLayout.visibility = View.VISIBLE
                saveAmountTv.text =
                    String.format(itemView.context.getString(R.string.save_rs_x), maxDiscount)
            } else {
                saveLayout.visibility = View.GONE
            }
            val parentItem: ConstraintLayout = itemView.findViewById(R.id.parent_item_layout)
            if (imageTitle != null) {
                logo.setImageDrawable(null)
                val imageUrl = BASE_IMAGES + bankLogoMap[banKTitleToCodeMap[imageTitle]]
                if (imageUrl.isNotBlank()) {
                    logo.load(imageUrl, imageLoader) {
                        placeholder(R.drawable.ic_generic)
                        error(R.drawable.ic_generic)
                        memoryCachePolicy(CachePolicy.ENABLED)
                    }
                } else {
                    logo.setImageResource(R.drawable.ic_generic)
                }

            }


            title.text = item.display_name.removeSuffix(" BANK")
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

    override fun getItemViewType(position: Int): Int {
        //Note: This is a temporary work around to avoid icon being repeated as we are fetching icons
        // from a server upon scroll the previous icon items are shown only
        return position
    }

}