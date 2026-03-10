package com.plural_pinelabs.expresscheckoutsdk.presentation.upi
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BHIM_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CRED_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.GPAY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KIWI_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MOBIKWIK_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAYTM
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PHONEPE
import com.plural_pinelabs.expresscheckoutsdk.data.model.Address
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.FeatureFlag
import com.plural_pinelabs.expresscheckoutsdk.data.model.MerchantInfo
import org.junit.Assert.assertEquals
import org.junit.Test
class UpiAppPackagesTest {
    @Test
    fun `includes Mobikwik and Kiwi in configured order when TPAP is configurable`() {
        assertEquals(
            listOf(PHONEPE, GPAY, KIWI_UPI, CRED_UPI, BHIM_UPI, PAYTM, MOBIKWIK_UPI),
            getSupportedUpiPackages(fetchData(isTpapConfigurable = true))
        )
    }
    @Test
    fun `excludes Mobikwik and Kiwi and keeps base order when TPAP is not configurable`() {
        assertEquals(
            listOf(PHONEPE, GPAY, CRED_UPI, BHIM_UPI, PAYTM),
            getSupportedUpiPackages(fetchData(isTpapConfigurable = false))
        )
    }
    @Test
    fun `excludes Mobikwik and Kiwi and keeps base order when fetch data is missing`() {
        assertEquals(
            listOf(PHONEPE, GPAY, CRED_UPI, BHIM_UPI, PAYTM),
            getSupportedUpiPackages(null)
        )
    }
    @Test
    fun `excludes Mobikwik and Kiwi and keeps base order when TPAP flag is null`() {
        assertEquals(
            listOf(PHONEPE, GPAY, CRED_UPI, BHIM_UPI, PAYTM),
            getSupportedUpiPackages(fetchData(isTpapConfigurable = null))
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