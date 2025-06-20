package com.plural_pinelabs.expresscheckoutsdk.presentation.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAYMENT_REFERENCE_TYPE_CARD
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataRequestList
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferEligibilityResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CardFragmentViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) :
    ViewModel() {

    private val _metaDataResult =
        MutableStateFlow<BaseResult<CardBinMetaDataResponse>>(BaseResult.Loading(false))
    val metaDataResult: StateFlow<BaseResult<CardBinMetaDataResponse>> = _metaDataResult

    private val _processPaymentResult =
        MutableStateFlow<BaseResult<ProcessPaymentResponse>>(BaseResult.Loading(false))
    val processPaymentResult: StateFlow<BaseResult<ProcessPaymentResponse>> = _processPaymentResult

    private val _validateOfferResult =
        MutableStateFlow<BaseResult<OfferEligibilityResponse>>(BaseResult.Loading(false))
    val validateOfferResult: StateFlow<BaseResult<OfferEligibilityResponse>> = _validateOfferResult

    private val _otpRequestResult =
        MutableStateFlow<BaseResult<OTPResponse>>(BaseResult.Loading(true))
    val otpRequestResult: StateFlow<BaseResult<OTPResponse>> = _otpRequestResult

    fun getCardBinMetaData(cardNumber: String, token: String) {
        val binRequest = CardBinMetaDataRequest(cardNumber, PAYMENT_REFERENCE_TYPE_CARD)
        val binRequestList = arrayListOf<CardBinMetaDataRequest>()
        binRequestList.add(binRequest)
        val cardBinMetaDataRequestList = CardBinMetaDataRequestList(card_details = binRequestList)
        val fetchData = ExpressSDKObject.getFetchData()
        val isDCCEnabled: Boolean =
            fetchData?.merchantInfo?.featureFlags?.isDCCEnabled ?: false
        if (isDCCEnabled) {
            cardBinMetaDataRequestList.amount = fetchData?.paymentData?.originalTxnAmount?.amount
            cardBinMetaDataRequestList.markup_required = true
            cardBinMetaDataRequestList.dcc_details_required = true
        }
        fetchMetaData(token, cardBinMetaDataRequestList)
    }


    private fun fetchMetaData(token: String, requestList: CardBinMetaDataRequestList) {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.getMetaData(token = token, requestList).collect {
                _metaDataResult.value = it

            }
        }
    }

    fun processPayment(token: String?, paymentData: ProcessPaymentRequest?) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.processPayment(token, paymentData).collect {
                _processPaymentResult.value = it
            }
        }

    fun validateOffer(token: String?, paymentData: ProcessPaymentRequest?) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.validateOffers(token, paymentData).collect {
                _validateOfferResult.value = it
            }
        }

    fun generateOTP(token: String?, otpRequest: OTPRequest) =
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.requestOTP(token, otpRequest).collect { values ->
                _otpRequestResult.value = values
            }
        }
}