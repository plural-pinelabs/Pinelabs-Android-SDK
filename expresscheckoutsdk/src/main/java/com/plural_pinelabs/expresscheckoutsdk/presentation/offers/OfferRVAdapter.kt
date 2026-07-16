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
import coil.load
import coil.request.CachePolicy
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_IMAGES
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetail

class OfferRVAdapter(
    private val context: Context,
    private val listOfTenure: ArrayList<OfferDetail>,
    private val itemClickListener: ItemClickListener<OfferDetail>,
    private val bankLogoMap: HashMap<String, String>,
    private val bankNameKeyList: List<String>,
    private val banKTitleToCodeMap: HashMap<String, String>,
    private val showUpiTag: Boolean = false,
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
            val offerTypeTagContainer = itemView.findViewById<View>(R.id.offer_type_tag_container)
            val emiTag: ImageView = itemView.findViewById(R.id.emi_tag)
            val logo: ImageView = itemView.findViewById(R.id.logo)
            val isUpiOffer = isUpiOffer(item)

            val showEmiTag = !showUpiTag && !isUpiOffer && !item.isInstantSaving
            offerTypeTagContainer.visibility = if (showEmiTag) View.VISIBLE else View.GONE
            emiTag.visibility = if (showEmiTag) View.VISIBLE else View.GONE

            val maxSavings = if (item.isInstantSaving) {
                val tenure = item.tenureOffers?.find { it.tenureId == "7" }
                val savings: Int =
                    tenure?.let { tenure.discountAmount.plus(tenure.cashbackAmount) } ?: 0
                if (savings > 0) savings else item.maxSaving
            } else {
                item.maxSaving
            }

            if (isUpiOffer) {
                offerLabel.text = buildUpiOfferTitle(item, maxSavings)
                offerSubtitle.text = context.getString(R.string.upi_offer_only_applicable_upi_id)
                maxDiscountLabel.visibility = View.GONE
            } else {
                offerLabel.text = item.offerTitle
                offerSubtitle.text = if (item.isInstantSaving) {
                    context.getString(R.string.instant_discount_on_full_payment)
                } else {
                    val emiTenureList =
                        item.tenureOffers
                            ?.filter { it.tenureId != "7" }
                            ?.sortedBy { it.tenureId }
                            ?.joinToString(separator = ",")
                            {
                                Regex("""\d+""").find(it.tenure)?.value ?: ""
                            }
                            ?: ""
                    String.format(
                        context.getString(R.string.applicable_on_emitenurelist_emi_tenures),
                        emiTenureList
                    )
                }
                maxDiscountLabel.visibility = View.VISIBLE
                maxDiscountLabel.text = buildSavingsTagText(item, maxSavings)
            }

            availOfferBtn.setOnClickListener {
                itemClickListener.onItemClick(position, item)
            }

            if (isUpiOffer) {
                logo.setImageResource(resolveUpiOfferIcon(item))
            } else {
                val imageTitle = bankNameKeyList.find {
                    it.contains(
                        item.name.removeSuffix(" BANK"), ignoreCase = true
                    )
                }
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
                } else {
                    logo.setImageResource(R.drawable.ic_generic)
                }
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

    private fun buildSavingsTagText(item: OfferDetail, maxSavings: Int): String {
        val discountType = resolveDiscountType(item)

        val savingsLabel = if (discountType.equals("CASHBACK", true)) {
            context.getString(R.string.cashback)
        } else if (discountType.equals("INSTANT", true)) {
            context.getString(R.string.instant_discount)
        } else if (item.isInstantSaving) {
            context.getString(R.string.cashback)
        } else {
            context.getString(R.string.instant_discount)
        }

        return context.getString(
            R.string.offer_savings_tag_format,
            savingsLabel,
            Utils.convertToRupeesWithSymobl(context, maxSavings),
        )
    }

    private fun buildUpiOfferTitle(item: OfferDetail, maxSavings: Int): String {
        val amount = Utils.convertToRupeesWithSymobl(context, maxSavings)
        return if (resolveDiscountType(item).equals("CASHBACK", true)) {
            context.getString(R.string.upi_offer_title_flat_cashback, amount)
        } else {
            context.getString(R.string.upi_offer_title_flat_off, amount)
        }
    }

    private fun resolveDiscountType(item: OfferDetail): String? {
        return item.tenureOffers
            ?.firstOrNull()
            ?.offers
            ?.firstOrNull()
            ?.discount
            ?.type
            ?.trim()
    }

    private fun isUpiOffer(item: OfferDetail): Boolean {
        return item.type.equals(PaymentModes.UPI.paymentModeID, true)
    }

    private fun resolveUpiOfferIcon(item: OfferDetail): Int {
        val candidateText = listOf(
            item.issuer?.display_name,
            item.offerTitle,
            item.name,
        ).joinToString(" ") { it.orEmpty() }.lowercase()

        return when {
            candidateText.contains("phonepe") -> R.drawable.ic_phone_pe
            candidateText.contains("google pay") || candidateText.contains("googlepay") || candidateText.contains("gpay") -> R.drawable.ic_googlepay_upi
            candidateText.contains("paytm") -> R.drawable.ic_paytm_upi
            candidateText.contains("cred") -> R.drawable.ic_cred_upi
            candidateText.contains("navi") -> R.drawable.ic_navi_upi
            candidateText.contains("supermoney") || candidateText.contains("super money") -> R.drawable.ic_supermoney_upi
            candidateText.contains("bhim") -> R.drawable.ic_bhim_upi
            else -> R.drawable.ic_upi_logo
        }
    }


}