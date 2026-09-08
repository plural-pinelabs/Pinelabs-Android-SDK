package com.plural_pinelabs.expresscheckoutsdk.data.repository

import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.toResultFlow
import com.plural_pinelabs.expresscheckoutsdk.data.fetch.CommonApiService
import com.plural_pinelabs.expresscheckoutsdk.data.fetch.ExpressApiService
import com.plural_pinelabs.expresscheckoutsdk.data.fetch.FetchApiService
import com.plural_pinelabs.expresscheckoutsdk.data.model.AddressRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.AddressResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.CancelTransactionResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataRequestList
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.CreateWalletRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.CreateWalletResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfoResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddress
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddressResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.KFSResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.LogData
import com.plural_pinelabs.expresscheckoutsdk.data.model.LogResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferEligibilityResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiFetchVpaRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiFetchVpaResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiOfferValidateRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletAddMoneyRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletAddMoneyResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletResetOtpRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletResetOtpResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletValidateRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.WalletValidateResponse
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.ApiService
import com.plural_pinelabs.expresscheckoutsdk.domain.repository.ExpressRepository
import kotlinx.coroutines.flow.Flow

class ExpressRepositoryImpl(
    private val apiService: ApiService,
    private val networkHelper: NetworkHelper
) : ExpressRepository {
    private val fetchApiService = apiService as? FetchApiService
    private val commonApiService = apiService as? CommonApiService
    private val expressApiService = apiService as? ExpressApiService

    private fun requireFetchApiService(): FetchApiService {
        return requireNotNull(fetchApiService) {
            "ExpressRepositoryImpl requires FetchApiService for this operation"
        }
    }

    private fun requireCommonApiService(): CommonApiService {
        return requireNotNull(commonApiService) {
            "ExpressRepositoryImpl requires CommonApiService for this operation"
        }
    }

    private fun requireExpressApiService(): ExpressApiService {
        return requireNotNull(expressApiService) {
            "ExpressRepositoryImpl requires ExpressApiService for this operation"
        }
    }

    override suspend fun fetchData(token: String?): Flow<BaseResult<FetchResponseDTO>> {
        // call the API to fetch data
        return toResultFlow(networkHelper = networkHelper) {
            requireFetchApiService().fetchData(token)
        }
    }

    override suspend fun getMetaData(
        token: String,
        request: CardBinMetaDataRequestList
    ): Flow<BaseResult<CardBinMetaDataResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().getMetaData(token, request)
        }
    }

    override suspend fun processPayment(
        token: String?,
        paymentData: ProcessPaymentRequest?
    ): Flow<BaseResult<ProcessPaymentResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().processPayment(token, paymentData)
        }
    }

    override suspend fun submitOTP(
        token: String?,
        otpRequest: OTPRequest
    ): Flow<BaseResult<OTPResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().submitOTP(token, otpRequest)
        }
    }

    override suspend fun requestOTP(
        token: String?,
        otpRequest: OTPRequest
    ): Flow<BaseResult<OTPResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().initiateOTP(token, otpRequest)
        }
    }

    override suspend fun resendOTP(
        token: String?,
        otpRequest: OTPRequest
    ): Flow<BaseResult<OTPResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().initiateOTP(token, otpRequest)
        }
    }

    override suspend fun sendOTPCustomer(
        token: String?,
        otpRequest: OTPRequest?
    ): Flow<BaseResult<SavedCardResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().sendOTPCustomer(token, otpRequest)
        }
    }

    override suspend fun validateOTPCustomer(
        token: String?,
        otpRequest: OTPRequest?
    ): Flow<BaseResult<SavedCardResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().validateOTPCustomer(token, otpRequest)
        }
    }

    override suspend fun transactionStatus(
        token: String?,
        orderId: String?,
    ): Flow<BaseResult<TransactionStatusResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().statusOfTransaction(token, orderId)
        }
    }

    override suspend fun graphQl(
        token: String?,
        request: ExpressAddress
    ): Flow<BaseResult<ExpressAddressResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireExpressApiService().graphQl("Bearer ${token?.trim()}", request)
        }
    }

    override suspend fun addCustomerAddresses(
        token: String?,
        request: ExpressAddress
    ): Flow<BaseResult<ExpressAddressResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireExpressApiService().addCustomerAddresses(
                "Bearer ${token?.trim()}",
                request
            )
        }
    }

    override suspend fun createInactiveUser(
        token: String?,
        request: CustomerInfo?
    ): Flow<BaseResult<CustomerInfo>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().createInactive(token, request)
        }
    }

    override suspend fun validateOffers(
        token: String?,
        paymentData: ProcessPaymentRequest?
    ): Flow<BaseResult<OfferEligibilityResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().validateOffer(token, paymentData)
        }
    }

    override suspend fun validateOffersV2(
        token: String?,
        request: UpiOfferValidateRequest?
    ): Flow<BaseResult<OfferEligibilityResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().validateOfferV2(token, request)
        }
    }

    override suspend fun getKFS(
        token: String?,
        paymentData: ProcessPaymentRequest?
    ): Flow<BaseResult<KFSResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().getKFS(token, paymentData)
        }
    }

    override suspend fun validateUpdateOrder(
        token: String?,
        request: OTPRequest?
    ): Flow<BaseResult<CustomerInfoResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().validateUpdateOrder(token, request)
        }
    }

    override suspend fun getUpdateAddress(
        token: String?,
        request: AddressRequest?
    ): Flow<BaseResult<AddressResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().updateAddress(token, request)
        }
    }

    override suspend fun logData(
        token: String?,
        request: List<LogData>?
    ): Flow<BaseResult<LogResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().log(token, request)
        }
    }

    override suspend fun createWallet(
        token: String?,
        request: CreateWalletRequest
    ): Flow<BaseResult<CreateWalletResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().createWallet(token, request)
        }
    }

    override suspend fun cancelPayment(
        token: String?,
        cancelPayment: Boolean
    ): Flow<BaseResult<CancelTransactionResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().cancelTransaction(token, cancelPayment)
        }
    }

    override suspend fun addMoneyWallet(
        token: String?,
        request: WalletAddMoneyRequest
    ): Flow<BaseResult<WalletAddMoneyResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().addMoneyToWallet(token, request)
        }
    }

    override suspend fun validateWalletBalance(
        token: String?,
        request: WalletValidateRequest
    ): Flow<BaseResult<WalletValidateResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().validateWalletBalance(token, request)
        }
    }

    override suspend fun resetWalletOtp(
        token: String?,
        customerId: String,
    ): Flow<BaseResult<WalletResetOtpResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().resetWalletOtp(
                customerId = customerId,
                request = WalletResetOtpRequest(token = token),
            )
        }
    }

    override suspend fun fetchUpiVpa(
        token: String?,
        request: UpiFetchVpaRequest,
    ): Flow<BaseResult<UpiFetchVpaResponse>> {
        return toResultFlow(networkHelper = networkHelper) {
            requireCommonApiService().fetchUpiVpa(token, request)
        }
    }
}