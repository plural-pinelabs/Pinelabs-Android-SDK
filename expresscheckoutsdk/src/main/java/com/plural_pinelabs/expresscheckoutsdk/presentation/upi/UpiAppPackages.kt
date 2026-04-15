package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BHIM_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CRED_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.GPAY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KIWI_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MOBIKWIK_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.NAVI_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAYTM
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PHONEPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SUPERMONEY_UPI
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO

private const val MOBIKWIK_LEGACY_UPI = "com.mobikwik"

internal fun getUpiAppsInstalledInDevice(fetchData: FetchResponseDTO?): List<String> {
    val isTpapConfigurable = fetchData?.merchantInfo?.featureFlags?.isTpapConfigurable == true
    val baseUpiApps = listOf(
        PHONEPE,
        GPAY,
        PAYTM,
        CRED_UPI,
        BHIM_UPI,
        NAVI_UPI,
        SUPERMONEY_UPI
    )

    return if (isTpapConfigurable) {
        baseUpiApps + listOf(KIWI_UPI, MOBIKWIK_UPI)
    } else {
        baseUpiApps
    }
}

internal fun getUpiAppsInDisplayOrder(installedUpiApps: List<String>): List<String> {
    if (installedUpiApps.isEmpty()) return emptyList()

    val canonicalToOriginal = linkedMapOf<String, String>()
    installedUpiApps.forEach { packageName ->
        val canonical = canonicalizeUpiPackage(packageName)
        if (!canonicalToOriginal.containsKey(canonical)) {
            canonicalToOriginal[canonical] = packageName
        }
    }

    val normalizedInstalledApps = canonicalToOriginal.keys.toList()
    val hasKiwi = normalizedInstalledApps.contains(KIWI_UPI)
    val hasMobikwik = normalizedInstalledApps.contains(MOBIKWIK_UPI)

    val scenarioOrder = when {
        hasKiwi && hasMobikwik -> listOf(KIWI_UPI, MOBIKWIK_UPI, CRED_UPI, BHIM_UPI)
        hasKiwi -> listOf(CRED_UPI, KIWI_UPI, BHIM_UPI, MOBIKWIK_UPI)
        hasMobikwik -> listOf(CRED_UPI, MOBIKWIK_UPI, BHIM_UPI, KIWI_UPI)
        else -> listOf(CRED_UPI, BHIM_UPI, KIWI_UPI, MOBIKWIK_UPI)
    }

    val preferredOrder = listOf(PHONEPE, GPAY, PAYTM) + scenarioOrder + listOf(
        NAVI_UPI,
        SUPERMONEY_UPI
    )
    val sortedKnownApps = preferredOrder.filter { normalizedInstalledApps.contains(it) }
    val remainingApps = normalizedInstalledApps.filterNot { preferredOrder.contains(it) }

    return (sortedKnownApps + remainingApps).map { normalized ->
        canonicalToOriginal[normalized] ?: normalized
    }
}

private fun canonicalizeUpiPackage(packageName: String): String {
    return when (packageName.lowercase()) {
        MOBIKWIK_UPI, MOBIKWIK_LEGACY_UPI -> MOBIKWIK_UPI
        else -> packageName.lowercase()
    }
}