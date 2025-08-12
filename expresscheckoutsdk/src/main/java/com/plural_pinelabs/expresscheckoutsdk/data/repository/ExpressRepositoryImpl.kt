package com.plural_pinelabs.expresscheckoutsdk.data.repository

import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.toResultFlow
import com.plural_pinelabs.expresscheckoutsdk.data.fetch.CommonApiService
import com.plural_pinelabs.expresscheckoutsdk.data.fetch.ExpressApiService
import com.plural_pinelabs.expresscheckoutsdk.data.fetch.FetchApiService
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
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.ApiService
import com.plural_pinelabs.expresscheckoutsdk.domain.repository.ExpressRepository
import kotlinx.coroutines.flow.Flow

class ExpressRepositoryImpl(
    private val apiService: ApiService,
    private val networkHelper: NetworkHelper
) : ExpressRepository {
    override suspend fun fetchData(token: String?): Flow<BaseResult<FetchResponseDTO>> {
        // call the API to fetch data
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as FetchApiService).fetchData(token)
        }
    }

    override suspend fun getMetaData(
        token: String,
        request: CardBinMetaDataRequestList
    ): Flow<BaseResult<CardBinMetaDataResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).getMetaData(token, request)
        }
    }

    override suspend fun processPayment(
        token: String?,
        paymentData: ProcessPaymentRequest?
    ): Flow<BaseResult<ProcessPaymentResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).processPayment(token, paymentData)
        }
    }

    override suspend fun submitOTP(
        token: String?,
        otpRequest: OTPRequest
    ): Flow<BaseResult<OTPResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).submitOTP(token, otpRequest)
        }
    }

    override suspend fun requestOTP(
        token: String?,
        otpRequest: OTPRequest
    ): Flow<BaseResult<OTPResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).initiateOTP(token, otpRequest)
        }
    }

    override suspend fun resendOTP(
        token: String?,
        otpRequest: OTPRequest
    ): Flow<BaseResult<OTPResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).initiateOTP(token, otpRequest)
        }
    }

    override suspend fun sendOTPCustomer(
        token: String?,
        otpRequest: OTPRequest?
    ): Flow<BaseResult<SavedCardResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).sendOTPCustomer(token, otpRequest)
        }
    }

    override suspend fun validateOTPCustomer(
        token: String?,
        otpRequest: OTPRequest?
    ): Flow<BaseResult<SavedCardResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).validateOTPCustomer(token, otpRequest)
        }
    }

    override suspend fun transactionStatus(
        token: String?
    ): Flow<BaseResult<TransactionStatusResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).statusOfTransaction(token)
        }
    }

    override suspend fun fetchAddress(
        token: String?,
        request: ExpressAddress
    ): Flow<BaseResult<ExpressAddressResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as ExpressApiService).fetchAddress("Bearer ${token?.trim()}", request)
        }
    }

    override suspend fun addCustomerAddresses(
        token: String?,
        request: ExpressAddress
    ): Flow<BaseResult<ExpressAddressResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as ExpressApiService).addCustomerAddresses("Bearer ${token?.trim()}", request)
        }
    }

    override suspend fun createInactiveUser(
        token: String?,
        request: CustomerInfo?
    ): Flow<BaseResult<CustomerInfo>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).createInactive(token, request)
        }
    }

    override suspend fun validateOffers(
        token: String?,
        paymentData: ProcessPaymentRequest?
    ): Flow<BaseResult<OfferEligibilityResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).validateOffer(token, paymentData)
        }
    }

    override suspend fun getKFS(
        token: String?,
        paymentData: ProcessPaymentRequest?
    ): Flow<BaseResult<KFSResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).getKFS(token, paymentData)
        }
    }

    override suspend fun validateUpdateOrder(
        token: String?,
        request: OTPRequest?
    ): Flow<BaseResult<CustomerInfoResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as CommonApiService).validateUpdateOrder(token, request)
        }
    }
}