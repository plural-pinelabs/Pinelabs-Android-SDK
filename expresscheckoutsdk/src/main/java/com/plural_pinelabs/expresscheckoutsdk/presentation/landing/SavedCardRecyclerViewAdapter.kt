package com.plural_pinelabs.expresscheckoutsdk.presentation.landing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_IMAGES
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardTokens

class SavedCardRecyclerViewAdapter(
    private val context: Context,
    private val savedCardList: List<SavedCardTokens>,
    private val savedCardSelectionCallback: ItemClickListener<SavedCardTokens>?
) :
    RecyclerView.Adapter<SavedCardRecyclerViewAdapter.SavedCardViewHolder>() {

    private lateinit var bankLogoMap: HashMap<String, String>
    private lateinit var banKTitleToCodeMap: HashMap<String, String>
    private lateinit var bankNameKeyList: List<String>

    private var selectedPosition = -1
    private var suppressFocusListener = false
    private var selectedType = 0 // 0-> none, 1-> cvvLess, 2-> cvvRequired

    init {
        mapBanKLogo()
    }

    inner class SavedCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardLogo = itemView.findViewById<ImageView>(R.id.card_icon)
        val cardName = itemView.findViewById<TextView>(R.id.card_issuer_name)
        val last4Digits = itemView.findViewById<TextView>(R.id.card_last_4_digits)
        val cvvEditText = itemView.findViewById<EditText>(R.id.cvv_input_field)
        val cvvLessSelect = itemView.findViewById<CheckBox>(R.id.cvv_less_selection)
        val payButton = itemView.findViewById<TextView>(R.id.pay_by_saved_card)
        val errorText = itemView.findViewById<TextView>(R.id.cvv_error_view)
        val parent = itemView.findViewById<LinearLayout>(R.id.saved_card_cvv_parent_layout)


        fun setItem(item: SavedCardTokens, position: Int) {
            cardName.text = item.cardData.issuerName ?: ""
            last4Digits.text = item.cardData.last4Digit
            loadBankLogo(item.cardData.issuerName, cardLogo)
            payButton.text = context.getString(
                R.string.pay_amount_text,
                context.getString(R.string.rupee_symbol),
                Utils.convertInRupees(ExpressSDKObject.getAmount())
            )

            val isSelected = position == selectedPosition
            parent.background = AppCompatResources.getDrawable(
                context,
                if (isSelected) R.color.dense_background else R.color.white
            )

            if (isSelected) {
                payButton.visibility = View.VISIBLE
                payButton.isClickable = true
            } else {
                payButton.visibility = View.GONE
                payButton.isClickable = false
            }
            cvvLessSelect.setOnCheckedChangeListener(null)
            cvvLessSelect.isChecked = isSelected && selectedType == 1

            if (!item.cardData.cvvRequired) {
                // if (position % 2 == 0) {
                cvvEditText.visibility = View.GONE
                cvvLessSelect.visibility = View.VISIBLE
            } else {
                cvvEditText.visibility = View.VISIBLE
                cvvLessSelect.visibility = View.GONE
            }

            cvvLessSelect.setOnCheckedChangeListener { _, isChecked ->
                val previousPosition = selectedPosition
                selectedPosition = if (isChecked) position else -1
                selectedType = if (isChecked) 1 else 0
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }


            suppressFocusListener = true
            cvvEditText.clearFocus() // optional: to reset focus
            suppressFocusListener = false

            cvvEditText.setOnClickListener {
                itemView.post {
                    val previousPosition = selectedPosition
                    selectedPosition = position
                    selectedType = 2
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                }
            }


            cvvEditText.setOnFocusChangeListener { _, hasFocus ->
                if (suppressFocusListener) return@setOnFocusChangeListener
                if (hasFocus) {
                    errorText.visibility = View.GONE
                } else {
                    if (cvvEditText.text.isNullOrEmpty()) {
                        payButton.visibility = View.GONE
                        payButton.isEnabled = false
                        payButton.isClickable = false
                    } else {
                        payButton.visibility = View.VISIBLE
                        payButton.isEnabled = true
                        payButton.isClickable = true
                    }
                }
            }

            payButton.setOnClickListener {
                if (item.cardData.cvvRequired && (cvvEditText.text.isNullOrEmpty() || cvvEditText.text.toString().length < 3)) {
                    errorText.visibility = View.VISIBLE
                    return@setOnClickListener
                } else if (item.cardData.cvvRequired && cvvEditText.text.isNotEmpty() && cvvEditText.text.toString().length >= 3) {
                    errorText.visibility = View.GONE
                    item.cvvInput = cvvEditText.text.toString()
                    savedCardSelectionCallback?.onItemClick(position, item)
                } else if (cvvLessSelect.isChecked) {
                    savedCardSelectionCallback?.onItemClick(position, item)
                }
            }
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

    private fun mapBanKLogo() {
        bankLogoMap = Utils.getBankLogoHashMap()
        bankNameKeyList = Utils.getListOfBanKTitle()
        banKTitleToCodeMap = Utils.bankTitleAndCodeMapper()
    }

    private fun loadBankLogo(issuerName: String, logo: ImageView) {
        issuerName.let { item ->
            val imageTitle =
                bankNameKeyList.find {
                    it.contains(
                        item.removeSuffix(" BANK"),
                        ignoreCase = true
                    )
                }

            if (imageTitle != null) {
                val imageUrl = BASE_IMAGES + bankLogoMap[banKTitleToCodeMap[imageTitle]]
                val imageLoader = ImageLoader.Builder(context)
                    .components {
                        add(SvgDecoder.Factory())
                    }
                    .crossfade(true)
                    .build()
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .target(logo)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
                imageLoader.enqueue(request)
            }

        }
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

}
