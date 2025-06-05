package com.plural_pinelabs.expresscheckoutsdk.presentation.landing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardTokens

class SavedCardRecyclerViewAdapter(
    private val context: Context,
    private val savedCardList: List<SavedCardTokens>,
    private val savedCardSelectionCallback: ItemClickListener<SavedCardTokens>?
) :
    RecyclerView.Adapter<SavedCardRecyclerViewAdapter.SavedCardViewHolder>() {

    inner class SavedCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardLogo = itemView.findViewById<ImageView>(R.id.card_icon)
        val cardName = itemView.findViewById<TextView>(R.id.card_issuer_name)
        val last4Digits = itemView.findViewById<TextView>(R.id.card_last_4_digits)
        val cvvEditText = itemView.findViewById<EditText>(R.id.cvv_input_field)
        val cvvLessSelect = itemView.findViewById<CheckBox>(R.id.cvv_less_selection)
        fun setItem(item: SavedCardTokens, position: Int) {
           // cardLogo.setImageResource(item.cardData.issuerName)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedCardViewHolder {
        return SavedCardViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.save_card_cvv_less_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return savedCardList.size
    }

    override fun onBindViewHolder(holder: SavedCardViewHolder, position: Int) {
        holder.setItem(savedCardList[position], position)

    }

}
