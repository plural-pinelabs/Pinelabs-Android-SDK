package com.plural_pinelabs.expresscheckoutsdk.domain.repository

import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataRequestList
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfoResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddress
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddressResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.KFSResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferEligibilityResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import kotlinx.coroutines.flow.Flow

interface ExpressRepository {

    suspend fun fetchData(token: String?): Flow<BaseResult<FetchResponseDTO>>
    suspend fun getMetaData(
        token: String,
        request: CardBinMetaDataRequestList
    ): Flow<BaseResult<CardBinMetaDataResponse>>

    suspend fun processPayment(
        token: String?,
        paymentData: ProcessPaymentRequest?
    ): Flow<BaseResult<ProcessPaymentResponse>>

    suspend fun submitOTP(
        token: String?,
        otpRequest: OTPRequest
    ): Flow<BaseResult<OTPResponse>>

    suspend fun requestOTP(token: String?, otpRequest: OTPRequest): Flow<BaseResult<OTPResponse>>
    suspend fun resendOTP(token: String?, otpRequest: OTPRequest): Flow<BaseResult<OTPResponse>>

    suspend fun sendOTPCustomer(
        token: String?,
        otpRequest: OTPRequest?
    ): Flow<BaseResult<SavedCardResponse>>

    suspend fun validateOTPCustomer(
        token: String?,
        otpRequest: OTPRequest?
    ): Flow<BaseResult<SavedCardResponse>>

    suspend fun transactionStatus(
        token: String?
    ): Flow<BaseResult<TransactionStatusResponse>>

    suspend fun fetchAddress(
        token: String?,
        request: ExpressAddress
    ): Flow<BaseResult<ExpressAddressResponse>>

   suspend fun createInactiveUser(
    token: String?,
    request: CustomerInfo?
    ): Flow<BaseResult<CustomerInfo>>

    suspend fun validateOffers(
        token: String?,
        paymentData: ProcessPaymentRequest?
    ): Flow<BaseResult<OfferEligibilityResponse>>

    suspend fun getKFS(
        token: String?,
        paymentData: ProcessPaymentRequest?
    ): Flow<BaseResult<KFSResponse>>

    suspend fun validateUpdateOrder(
        token: String?,
        request: OTPRequest?
    ): Flow<BaseResult<CustomerInfoResponse>>
}