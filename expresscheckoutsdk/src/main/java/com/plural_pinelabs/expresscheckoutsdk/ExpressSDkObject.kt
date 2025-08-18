package com.plural_pinelabs.expresscheckoutsdk

import android.content.Context
import com.plural_pinelabs.expresscheckoutsdk.data.model.Address
import com.plural_pinelabs.expresscheckoutsdk.data.model.EMIPaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetail
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure
import java.util.concurrent.atomic.AtomicReference

internal data class SDKObject(
    val context: Context,
    val callback: ExpressSDKCallback,
    val token: String,
    val sandBoxMode: Boolean = false,
    var fetchResponseDTO: FetchResponseDTO? = null,
    var processPaymentResponse: ProcessPaymentResponse? = null,
    var phoneNumber: String? = null,
    var emiPaymentModeData: EMIPaymentModeData? = null,
    var payableAmount: Int? = null,
    var convenienceFee: Int? = null,
    var convenienceFeeGst: Int? = null,
    var selectedTenure: Tenure? = null,
    var selectedOfferDetail: OfferDetail? = null,
    var addressList: List<Address>? = null,
    var selectedAddress: Address? = null,
    var customerId:String? = null,
    var customerToken: String? = null
)

internal object ExpressSDKObject {
    private val sdkObjectRef = AtomicReference<SDKObject?>()

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
        getSDKObject()?.fetchResponseDTO = it
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
        val fetchResponse = getFetchData()
        return (fetchResponse?.paymentData?.originalTxnAmount?.currency ?: run {
            ""
        })
    }

    fun setProcessPaymentResponse(it: ProcessPaymentResponse) {
        getSDKObject()?.processPaymentResponse = it
    }

    fun getProcessPaymentResponse(): ProcessPaymentResponse? {
        return getSDKObject()?.processPaymentResponse
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
}


