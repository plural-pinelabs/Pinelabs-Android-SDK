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
    val isTpapConfigurable = fetchData?.merchantInfo?.featureFlags?.isTpapConfigurable == true
    return if (isTpapConfigurable) {
        listOf(
            PHONEPE,
            GPAY,
            KIWI_UPI,
            CRED_UPI,
            BHIM_UPI,
            PAYTM,
            MOBIKWIK_UPI
        )
    } else {
        listOf(
            PHONEPE,
            GPAY,
            CRED_UPI,
            BHIM_UPI,
            PAYTM
        )
    }
}