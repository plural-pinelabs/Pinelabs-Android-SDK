package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.data.model.DiscountValue
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer
import com.plural_pinelabs.expresscheckoutsdk.data.model.Offer
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetail
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure
import com.plural_pinelabs.expresscheckoutsdk.data.model.TenureOffer
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiOfferEntity
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiOfferRankedOffer
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiOfferTenure

private val upiOfferGson = Gson()

private data class RankedEntityOffer(
    val entity: UpiOfferEntity,
    val tenure: UpiOfferTenure,
    val offer: UpiOfferRankedOffer,
    val ranking: Int,
    val discountValue: Int,
    val discountType: String,
)

internal fun resolveUpiPaymentModeData(fetchData: FetchResponseDTO?): PaymentModeData? {
    val upiPaymentMode = fetchData?.paymentModes?.firstOrNull {
        it.paymentModeId.equals(PaymentModes.UPI.paymentModeID, ignoreCase = true)
    } ?: return null

    val paymentModeData = upiPaymentMode.paymentModeData ?: return null

    return when (paymentModeData) {
        is PaymentModeData -> paymentModeData
        is LinkedTreeMap<*, *> -> upiOfferGson.fromJson(
            upiOfferGson.toJsonTree(paymentModeData),
            PaymentModeData::class.java,
        )

        is Map<*, *> -> upiOfferGson.fromJson(
            upiOfferGson.toJsonTree(paymentModeData),
            PaymentModeData::class.java,
        )

        else -> null
    }
}

internal fun resolveUpiOfferDetails(fetchData: FetchResponseDTO?): List<OfferDetail> {
    return resolveAllEntityOffers(fetchData).map { rankedOffer ->
        val entityDisplayName = rankedOffer.entity.entity_display_name
            ?.takeIf { it.isNotBlank() }
            ?: rankedOffer.entity.entity_name
            ?.takeIf { it.isNotBlank() }
            ?: "UPI"

        val entityType = rankedOffer.entity.entity_type
            ?.takeIf { it.isNotBlank() }
            ?: PaymentModes.UPI.paymentModeID

        val entityId = rankedOffer.entity.entity_id
            ?.takeIf { it.isNotBlank() }
            ?: entityDisplayName

        val tenureId = rankedOffer.tenure.tenure_id
            ?.takeIf { it.isNotBlank() }
            ?: "7"

        val tenureType = rankedOffer.tenure.tenure_type
            ?.takeIf { it.isNotBlank() }
            ?: "MONTH"

        val emiType = rankedOffer.offer.emi_type
            ?.takeIf { it.isNotBlank() }
            ?: "STANDARD"

        val selectedTenure = Tenure(
            tenure_id = tenureId,
            name = rankedOffer.tenure.name
                ?.takeIf { it.isNotBlank() }
                ?: "No EMI Only Cashback",
            tenure_type = tenureType,
            tenure_value = rankedOffer.tenure.tenure_value ?: 0,
            emi_type = emiType,
        )

        val offer = Offer(
            programType = "offer_ranking_${rankedOffer.ranking}",
            discount = DiscountValue(
                type = rankedOffer.discountType,
                value = rankedOffer.discountValue,
            ),
        )

        val issuer = Issuer(
            id = entityId,
            name = rankedOffer.entity.entity_name
                ?.takeIf { it.isNotBlank() }
                ?: entityDisplayName,
            display_name = entityDisplayName,
            issuer_type = entityType,
            priority = rankedOffer.entity.entity_priority ?: Int.MAX_VALUE,
            tenures = listOf(selectedTenure),
        )

        OfferDetail(
            name = entityDisplayName,
            type = entityType,
            offerTitle = entityDisplayName,
            maxSaving = rankedOffer.discountValue,
            isInstantSaving = true,
            issuer = issuer,
            issuerId = issuer.id,
            tenureOffers = listOf(
                TenureOffer(
                    tenureId = selectedTenure.tenure_id,
                    tenure = selectedTenure.name,
                    offers = listOf(offer),
                    emiType = selectedTenure.emi_type,
                    discountAmount = rankedOffer.discountValue,
                    cashbackAmount = 0,
                    offerLabel = "",
                    fullTenure = selectedTenure,
                ),
            ),
        )
    }
}

internal fun resolveHighestUpiOfferDiscount(fetchData: FetchResponseDTO?): Int? {
    return resolveAllEntityOffers(fetchData)
        .maxOfOrNull { it.discountValue }
        ?.takeIf { it > 0 }
}

private fun resolveAllEntityOffers(fetchData: FetchResponseDTO?): List<RankedEntityOffer> {
    val entities = resolveUpiPaymentModeData(fetchData)?.offerData?.entities.orEmpty()

    val offers = entities.flatMap { entity ->
        val allRankedOffers = entity.tenures.orEmpty().flatMap { tenure ->
            tenure.offers.orEmpty().map { offer ->
                RankedEntityOffer(
                    entity = entity,
                    tenure = tenure,
                    offer = offer,
                    ranking = offer.offer_ranking ?: Int.MAX_VALUE,
                    discountValue = offer.total_discount_amount?.value
                        ?: offer.discount?.amount?.value
                        ?: 0,
                    discountType = offer.discount?.discount_type
                        ?.takeIf { it.isNotBlank() }
                        ?.uppercase()
                        ?: "INSTANT",
                )
            }
        }

        allRankedOffers.filter { it.discountValue > 0 }
    }

    return offers.sortedWith(
        compareByDescending<RankedEntityOffer> { it.discountValue }
            .thenBy { it.ranking }
            .thenBy { it.entity.entity_priority ?: Int.MAX_VALUE },
    )
}
