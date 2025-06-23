package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_IMAGES
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer

class EMIBankRecyclerViewAdapter(
    private val list: List<Issuer>,
    private val emiBankSelectionCallback: ItemClickListener<Issuer>?,
    private val bankLogoMap: HashMap<String, String>,
    private val bankNameKeyList: List<String>,
    private val banKTitleToCodeMap: HashMap<String, String>
) :
    RecyclerView.Adapter<EMIBankRecyclerViewAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setItem(item: Issuer, position: Int) {
            val imageTitle =
                bankNameKeyList.find {
                    it.contains(
                        item.display_name.removeSuffix(" BANK"),
                        ignoreCase = true
                    )
                }
            val logo: ImageView = itemView.findViewById(R.id.bank_logo)
            val title: TextView = itemView.findViewById(R.id.bank_title)
            val parentItem: ConstraintLayout =
                itemView.findViewById(R.id.parent_item_layout)
            if (imageTitle != null) {
                val imageUrl = BASE_IMAGES + bankLogoMap[banKTitleToCodeMap[imageTitle]]
                val imageLoader = ImageLoader.Builder(itemView.context)
                    .components {
                        add(SvgDecoder.Factory())
                    }
                    .crossfade(true)
                    .build()
                val request = ImageRequest.Builder(itemView.context)
                    .data(imageUrl)
                    .target(logo)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
                imageLoader.enqueue(request)
                // TODO add a fallback image
            }
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