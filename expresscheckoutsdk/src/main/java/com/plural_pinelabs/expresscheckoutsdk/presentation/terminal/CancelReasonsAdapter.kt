package com.plural_pinelabs.expresscheckoutsdk.presentation.terminal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.R

class CancelReasonsAdapter(
    private val items: List<CancelReasonItem>,
    private val onSelected: (CancelReasonItem?) -> Unit
) : RecyclerView.Adapter<CancelReasonsAdapter.VH>() {

    private var selectedId: String? = null

    fun setSelected(id: String?) {
        val oldId = selectedId
        if (oldId == id) return

        selectedId = id

        oldId?.let { previousId ->
            items.indexOfFirst { it.id == previousId }
                .takeIf { it >= 0 }
                ?.let(::notifyItemChanged)
        }
        id?.let { newId ->
            items.indexOfFirst { it.id == newId }
                .takeIf { it >= 0 }
                ?.let(::notifyItemChanged)
        }
    }

    private fun toggleSelected(id: String): CancelReasonItem? {
        setSelected(if (selectedId == id) null else id)
        return getSelected()
    }

    fun getSelected(): CancelReasonItem? = items.firstOrNull { it.id == selectedId }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cancel_reason, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reasonText: TextView = itemView.findViewById(R.id.reason_text)
        private val reasonRadio: AppCompatRadioButton = itemView.findViewById(R.id.reason_radio)

        fun bind(item: CancelReasonItem) {
            reasonText.text = item.title
            reasonRadio.isChecked = item.id == selectedId

            itemView.setOnClickListener {
                onSelected(toggleSelected(item.id))
            }

            reasonRadio.setOnClickListener {
                onSelected(toggleSelected(item.id))
            }
        }
    }
}
