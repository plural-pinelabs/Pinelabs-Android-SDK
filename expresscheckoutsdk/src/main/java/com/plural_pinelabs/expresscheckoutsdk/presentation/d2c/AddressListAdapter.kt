package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

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
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.dpToPx
import com.plural_pinelabs.expresscheckoutsdk.data.model.Address

class AddressListAdapter(
    private val context: Context,
    private val addressList: List<Address>,
    private val emiBankSelectionCallback: ItemClickListener<Address?>?
) :
    RecyclerView.Adapter<AddressListAdapter.ItemViewHolder>() {

    private var selectedPosition = -1 // No selection by default
    private var dp8 = 8.dpToPx()


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setItem(item: Address, position: Int) {
            val parent = itemView.findViewById<ConstraintLayout>(R.id.saved_address_item_parent)
            val edit = itemView.findViewById<ImageView>(R.id.edit_address_icon)
            val addressType = itemView.findViewById<TextView>(R.id.address_type)
            val name = itemView.findViewById<TextView>(R.id.first_name)
            val address = itemView.findViewById<TextView>(R.id.address_line_1)
            val city = itemView.findViewById<TextView>(R.id.city_pincode)
            val delete = itemView.findViewById<ImageView>(R.id.delete_address_icon)
            addressType.text = item.address_type

            // Apply selection UI based on selectedPosition
            if (position == selectedPosition) {
                parent.setBackgroundResource(R.drawable.black_field_border)
            } else {
                parent.setBackgroundResource(R.drawable.input_field_border)
            }
            parent.setPadding(dp8, dp8, dp8, dp8)

            // Set click listener
            parent.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                emiBankSelectionCallback?.onItemClick(0, item)
            }
            edit.setOnClickListener {
                emiBankSelectionCallback?.onItemClick(1, item)
            }
            delete.setOnClickListener {
                emiBankSelectionCallback?.onItemClick(2, item)
            }

            //handle edit address click
            name.text = item.full_name
            address.text = "${item.address1}, ${item.address2},  ${item.address3}".trim()
            city.text = "${item.city}, ${item.state}, ${item.pincode}".trim()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.saved_address_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.setItem(addressList[position], position)
    }

    override fun getItemViewType(position: Int): Int {
        //Note: This is a temporary work around to avoid icon being repeated as we are fetching icons
        // from a server upon scroll the previous icon items are shown only
        return position
    }
}