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
import com.plural_pinelabs.expresscheckoutsdk.data.model.Address
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.FeatureFlag
import com.plural_pinelabs.expresscheckoutsdk.data.model.MerchantInfo
import org.junit.Assert.assertEquals
import org.junit.Test

private const val MOBIKWIK_LEGACY_UPI = "com.mobiKwik"
private const val UNKNOWN_UPI_APP = "com.example.randomupi"

class UpiAppPackagesTest {
    @Test
    fun `includes Mobikwik and Kiwi in configured order when TPAP is configurable`() {
        assertEquals(
            listOf(
                PHONEPE,
                GPAY,
                PAYTM,
                CRED_UPI,
                BHIM_UPI,
                NAVI_UPI,
                SUPERMONEY_UPI,
                KIWI_UPI,
                MOBIKWIK_UPI
            ),
            getSupportedUpiPackages(fetchData(isTpapConfigurable = true))
        )
    }
    @Test
    fun `excludes Mobikwik and Kiwi and keeps base order when TPAP is not configurable`() {
        assertEquals(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI),
            getSupportedUpiPackages(fetchData(isTpapConfigurable = false))
        )
    }
    @Test
    fun `excludes Mobikwik and Kiwi and keeps base order when fetch data is missing`() {
        assertEquals(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI),
            getSupportedUpiPackages(null)
        )
    }
    @Test
    fun `excludes Mobikwik and Kiwi and keeps base order when TPAP flag is null`() {
        assertEquals(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI),
            getSupportedUpiPackages(fetchData(isTpapConfigurable = null))
        )
    }

    @Test
    fun `display order follows default scenario when no strategic apps are installed`() {
        val ordered = getUpiAppsInDisplayOrder(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI)
        )

        assertEquals(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI),
            ordered
        )
    }

    @Test
    fun `display order follows kiwi scenario when only kiwi is installed`() {
        val ordered = getUpiAppsInDisplayOrder(
            listOf(
                PHONEPE,
                GPAY,
                PAYTM,
                CRED_UPI,
                KIWI_UPI,
                BHIM_UPI,
                NAVI_UPI,
                SUPERMONEY_UPI
            )
        )

        assertEquals(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, KIWI_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI),
            ordered
        )
    }

    @Test
    fun `display order follows dual scenario when kiwi and mobikwik are installed`() {
        val ordered = getUpiAppsInDisplayOrder(
            listOf(
                PHONEPE,
                GPAY,
                PAYTM,
                CRED_UPI,
                KIWI_UPI,
                MOBIKWIK_UPI,
                BHIM_UPI,
                NAVI_UPI,
                SUPERMONEY_UPI
            )
        )

        assertEquals(
            listOf(
                PHONEPE,
                GPAY,
                PAYTM,
                KIWI_UPI,
                MOBIKWIK_UPI,
                CRED_UPI,
                BHIM_UPI,
                NAVI_UPI,
                SUPERMONEY_UPI
            ),
            ordered
        )
    }

    @Test
    fun `display order follows mobikwik-only scenario when only mobikwik is installed`() {
        val ordered = getUpiAppsInDisplayOrder(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, MOBIKWIK_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI)
        )

        assertEquals(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, MOBIKWIK_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI),
            ordered
        )
    }

    @Test
    fun `display order treats legacy mobikwik package as strategic app`() {
        val ordered = getUpiAppsInDisplayOrder(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, MOBIKWIK_LEGACY_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI)
        )

        assertEquals(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, MOBIKWIK_LEGACY_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI),
            ordered
        )
    }

    @Test
    fun `display order keeps unknown installed upi apps at the end`() {
        val ordered = getUpiAppsInDisplayOrder(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI, UNKNOWN_UPI_APP)
        )

        assertEquals(
            listOf(PHONEPE, GPAY, PAYTM, CRED_UPI, BHIM_UPI, NAVI_UPI, SUPERMONEY_UPI, UNKNOWN_UPI_APP),
            ordered
        )
    }

    private fun fetchData(isTpapConfigurable: Boolean?) = FetchResponseDTO(
        merchantInfo = MerchantInfo(
            merchantId = 1,
            merchantName = "Test Merchant",
            merchantDisplayName = null,
            featureFlags = FeatureFlag(
                isSavedCardEnabled = null,
                isNativeOTPEnabled = null,
                isDCCEnabled = null,
                isTpapConfigurable = isTpapConfigurable
            )
        ),
        customerInfo = null,
        shippingAddress = Address(),
        billingAddress = Address()
    )
}