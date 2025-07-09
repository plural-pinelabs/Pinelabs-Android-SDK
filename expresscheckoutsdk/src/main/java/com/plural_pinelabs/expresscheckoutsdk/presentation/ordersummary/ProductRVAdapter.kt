package com.plural_pinelabs.expresscheckoutsdk.presentation.ordersummary

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.load
import coil.request.CachePolicy
import com.clevertap.android.sdk.isNotNullAndBlank
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.CartItem

class ProductRVAdapter(
    private val context: Context,
    private val list: List<CartItem>,
) : RecyclerView.Adapter<ProductRVAdapter.ItemViewHolder>() {


    private val imageLoader = ImageLoader.Builder(context)
        .components { add(SvgDecoder.Factory()) }
        .crossfade(true)
        .build()


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("StringFormatInvalid")
        fun setItem(item: CartItem, position: Int) {
            val productNameText: TextView = itemView.findViewById(R.id.product_name)
            val productPriceText: TextView = itemView.findViewById(R.id.product_price_value)
            val productQuantityText: TextView = itemView.findViewById(R.id.product_quantity)
            val productImage: ImageView = itemView.findViewById(R.id.product_image)

            productNameText.text = item.item_name
            productQuantityText.text = String.format(
                context.getString(
                    R.string.qty_x,
                ), item.item_quantity.toString()
            )

            productPriceText.text =
                Utils.convertToRupeesWithSymobl(context, item.item_original_unit_price?.toInt())
            if (item.item_image_url?.isNotNullAndBlank() == true) {
                productImage.load(item.item_image_url, imageLoader) {
                    placeholder(R.drawable.ic_generic)
                    error(R.drawable.ic_generic)
                    memoryCachePolicy(CachePolicy.ENABLED)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.order_item_layout, parent, false)
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