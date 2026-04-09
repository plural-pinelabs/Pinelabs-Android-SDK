package com.plural_pinelabs.expresscheckoutsdk

import android.content.Context
import com.plural_pinelabs.expresscheckoutsdk.data.model.Address
import com.plural_pinelabs.expresscheckoutsdk.data.model.EMIPaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetail
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletAddMoneyResponse
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

internal data class SDKObject(
    val context: Context,
    val callback: ExpressSDKCallback,
    val token: String,
    val sandBoxMode: Boolean = false,
    var fetchResponseDTO: FetchResponseDTO? = null,
    var processPaymentResponse: ProcessPaymentResponse? = null,
    var walletAddMoneyResponse: WalletAddMoneyResponse? = null,
    var selectedMode: String? = null,
    var phoneNumber: String? = null,
    var emiPaymentModeData: EMIPaymentModeData? = null,
    var payableAmount: Int? = null,
    var convenienceFee: Int? = null,
    var convenienceFeeGst: Int? = null,
    var selectedTenure: Tenure? = null,
    var selectedOfferDetail: OfferDetail? = null,
    var addressList: List<Address>? = null,
    var selectedAddress: Address? = null,
    var customerId: String? = null,
    var customerToken: String? = null,
    var createdAt: String? = null,
    var logCount: Int = -1,
    var resolvedCurrencyCode: String = "INR",
    var resolvedCurrencySymbol: String = "\u20B9",
    var resolvedCurrencyRatio: Int = 2
)

internal data class CurrencyMapping(
    val code: String,
    val symbol: String,
    val transformationRatio: Int
)

internal object ExpressSDKObject {
    private val sdkObjectRef = AtomicReference<SDKObject?>()
    private const val DEFAULT_CURRENCY_CODE = "INR"
    private const val DEFAULT_CURRENCY_SYMBOL = "\u20B9"
    private const val DEFAULT_CURRENCY_RATIO = 1

    fun initialize(
        context: Context,
        callback: ExpressSDKCallback,
        token: String,
        runInSandboxedSdk: Boolean = false
    ) {
        sdkObjectRef.set(SDKObject(context, callback, token, runInSandboxedSdk))
    }

    fun getToken(): String? {
        return sdkObjectRef.get()?.token
    }

    private fun getSDKObject(): SDKObject? {
        return sdkObjectRef.get()
    }

    fun isSandBoxMode(): Boolean {
        return getSDKObject()?.sandBoxMode ?: false
    }

    fun setFetchData(it: FetchResponseDTO) {
        val sdkObject = getSDKObject() ?: return
        sdkObject.fetchResponseDTO = it

        val transactionCurrencyCode =
            it.paymentData?.paymentAmount?.currency ?: it.paymentData?.originalTxnAmount?.currency
        val mappedCurrency = mapCurrency(transactionCurrencyCode, it)

        sdkObject.resolvedCurrencyCode = mappedCurrency.code
        sdkObject.resolvedCurrencySymbol = mappedCurrency.symbol
        sdkObject.resolvedCurrencyRatio = mappedCurrency.transformationRatio
    }

    fun getFetchData(): FetchResponseDTO? {
        return getSDKObject()?.fetchResponseDTO
    }

    fun getAmount(): Int {
        val fetchResponse = getFetchData()
        return getPayableAmount() ?: (fetchResponse?.paymentData?.originalTxnAmount?.amount ?: run {
            -1
        })
    }

    fun getOriginalOrderAmount(): Int {
        val fetchResponse = getFetchData()
        return (fetchResponse?.paymentData?.originalTxnAmount?.amount ?: run {
            -1
        })
    }

    fun getCallback(): ExpressSDKCallback? {
        return getSDKObject()?.callback
    }

    fun getCurrency(): String {
        return getSDKObject()?.resolvedCurrencyCode ?: DEFAULT_CURRENCY_CODE
    }

    fun getCurrencySymbol(): String {
        return getSDKObject()?.resolvedCurrencySymbol ?: DEFAULT_CURRENCY_SYMBOL
    }

    fun getCurrencyTransformationRatio(): Int {
        return getSDKObject()?.resolvedCurrencyRatio ?: DEFAULT_CURRENCY_RATIO
    }

    fun mapCurrency(
        currencyCode: String?,
        fetchResponse: FetchResponseDTO? = getFetchData()
    ): CurrencyMapping {
        val normalizedCode = currencyCode
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.uppercase(Locale.ROOT)
            ?: DEFAULT_CURRENCY_CODE

        val currencyData = fetchResponse?.dccData?.currencyMapper
            ?.entries
            ?.firstOrNull { it.key.equals(normalizedCode, ignoreCase = true) }
            ?.value

        val mappedSymbol = currencyData?.symbol
            ?.takeIf { it.isNotBlank() }
            ?: if (normalizedCode == DEFAULT_CURRENCY_CODE) DEFAULT_CURRENCY_SYMBOL else normalizedCode

        val mappedRatio = currencyData?.transformation_ratio
            ?.takeIf { it > 0 }
            ?: DEFAULT_CURRENCY_RATIO

        return CurrencyMapping(
            code = normalizedCode,
            symbol = mappedSymbol,
            transformationRatio = mappedRatio
        )
    }

    fun isMCCTransaction(): Boolean {
        return getFetchData()?.transactionInfo?.isMCCTransaction == true
    }

    fun setProcessPaymentResponse(it: ProcessPaymentResponse) {
        getSDKObject()?.processPaymentResponse = it
    }

    fun getProcessPaymentResponse(): ProcessPaymentResponse? {
        return getSDKObject()?.processPaymentResponse
    }

    fun setWalletAddMoneyResponse(response: WalletAddMoneyResponse?) {
        getSDKObject()?.walletAddMoneyResponse = response
    }

    fun getWalletAddMoneyResponse(): WalletAddMoneyResponse? {
        return getSDKObject()?.walletAddMoneyResponse
    }

    fun setSelectedMode(mode: String?) {
        getSDKObject()?.selectedMode = mode
    }

    fun getSelectedMode(): String? {
        return getSDKObject()?.selectedMode
    }

    fun setPhoneNumber(phoneNumber: String) {
        getSDKObject()?.phoneNumber = phoneNumber
    }

    fun getPhoneNumber(): String? {
        return getSDKObject()?.phoneNumber
            ?: getSDKObject()?.fetchResponseDTO?.customerInfo?.mobileNumber
    }

    fun setEMIPaymentModeData(emiPaymentModeData: EMIPaymentModeData) {
        getSDKObject()?.emiPaymentModeData = emiPaymentModeData
    }

    fun getEMIPaymentModeData(): EMIPaymentModeData? {
        return getSDKObject()?.emiPaymentModeData
    }

    fun setPayableAmount(amount: Int) {
        getSDKObject()?.payableAmount = amount
    }

    fun getPayableAmount(): Int? {
        return getSDKObject()?.payableAmount
    }

    fun setConvenienceFee(fee: Int?) {
        getSDKObject()?.convenienceFee = fee
    }

    fun getConvenienceFee(): Int? {
        return getSDKObject()?.convenienceFee
    }

    fun setConvenienceFeeGst(gst: Int?) {
        getSDKObject()?.convenienceFeeGst = gst
    }

    fun getConvenienceFeeGst(): Int? {
        return getSDKObject()?.convenienceFeeGst
    }

    fun setSelectedTenure(tenure: Tenure?) {
        getSDKObject()?.selectedTenure = tenure
    }

    fun getSelectedTenure(): Tenure? {
        return getSDKObject()?.selectedTenure
    }

    fun getSelectedOfferDetail(): OfferDetail? {
        return getSDKObject()?.selectedOfferDetail
    }

    fun setSelectedOfferDetail(offerDetail: OfferDetail?) {
        getSDKObject()?.selectedOfferDetail = offerDetail
    }

    fun getAddressList(): List<Address>? {
        return getSDKObject()?.addressList
    }

    fun setAddressList(addressList: List<Address>?) {
        getSDKObject()?.addressList = addressList
    }

    fun getSelectedAddress(): Address? {
        return getSDKObject()?.selectedAddress
    }

    fun setSelectedAddress(address: Address?) {
        getSDKObject()?.selectedAddress = address
    }

    fun getCustomerId(): String? {
        return getSDKObject()?.customerId
    }

    fun setCustomerId(customerId: String?) {
        getSDKObject()?.customerId = customerId
    }

    fun getCustomerToken(): String? {
        return getSDKObject()?.customerToken
    }

    fun setCustomerToken(customerToken: String?) {
        getSDKObject()?.customerToken = customerToken
    }

    fun getCreatedAt(): String? {
        return getSDKObject()?.createdAt
    }

    fun setCreatedAt(createdAt: String?) {
        getSDKObject()?.createdAt = createdAt
    }

    fun getLogCount(): Int {
        return getSDKObject()?.logCount ?: -1
    }

    fun setLogCount(logCount: Int) {
        getSDKObject()?.logCount = logCount
    }


}


