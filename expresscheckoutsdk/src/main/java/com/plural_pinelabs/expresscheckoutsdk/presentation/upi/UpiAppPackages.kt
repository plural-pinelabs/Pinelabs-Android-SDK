package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BHIM_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CRED_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.GPAY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KIWI_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MOBIKWIK_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAYTM
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PHONEPE
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO

internal fun getSupportedUpiPackages(fetchData: FetchResponseDTO?): List<String> {
    val supportedPackages = mutableListOf(
        GPAY,
        PHONEPE,
        PAYTM,
        CRED_UPI,
        BHIM_UPI
    )

    if (fetchData?.merchantInfo?.isTpapConfigurable == true) {
        supportedPackages.add(1, KIWI_UPI)
        supportedPackages.add(3, MOBIKWIK_UPI)
    }

    return supportedPackages
}

