package com.plural_pinelabs.expresscheckoutsdk.data.fetch

import com.plural_pinelabs.expresscheckoutsdk.data.model.AddressRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.AddressResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataRequestList
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfoResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.KFSResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.LogRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferEligibilityResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.ApiService
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CommonApiService : ApiService {

    @POST("getMetaData")
    suspend fun getMetaData(
        @Query(
            "token",
            encoded = true
        ) token: String, @Body request: CardBinMetaDataRequestList
    ): Response<CardBinMetaDataResponse>

    @POST("process/payment")
    suspend fun processPayment(
        @Query("token", encoded = true) token: String?,
        @Body request: ProcessPaymentRequest?
    ): Response<ProcessPaymentResponse>

    @POST("otp/submitOtp")
    suspend fun submitOTP(
        @Query(
            "token",
            encoded = true
        ) token: String?, @Body request: OTPRequest
    ): Response<OTPResponse>

    @POST("otp/initiateOtp")
    suspend fun initiateOTP(
        @Query(
            "token",
            encoded = true
        ) token: String?, @Body request: OTPRequest
    ): Response<OTPResponse>


    @POST("otp/resendOtp")
    suspend fun resendOTP(
        @Query(
            "token",
            encoded = true
        ) token: String?, @Body request: OTPRequest
    ): Response<OTPResponse>

    @POST("customer/otp/send")
    suspend fun sendOTPCustomer(
        @Query(
            "token",
            encoded = true
        ) token: String?, @Body request: OTPRequest?
    ): Response<SavedCardResponse>

    @POST("customer/otp/validate")
    suspend fun validateOTPCustomer(
        @Query(
            "token",
            encoded = true
        ) token: String?, @Body request: OTPRequest?
    ): Response<SavedCardResponse>


    @POST("customer/otp/validate-and-update-order")
    suspend fun validateUpdateOrder(
        @Query(
            "token",
            encoded = true
        ) token: String?, @Body request: OTPRequest?
    ): Response<CustomerInfoResponse>

    @POST("customer/create-inactive")
    suspend fun createInactive(
        @Query(
            "token",
            encoded = true
        ) token: String?, @Body request: CustomerInfo?
    ): Response<CustomerInfo>

    @GET("inquiry")
    suspend fun statusOfTransaction(
        @Query(
            "token",
            encoded = true
        ) token: String?
    ): Response<TransactionStatusResponse>

    @POST("offer/validate")
    suspend fun validateOffer(
        @Query("token", encoded = true) token: String?,
        @Body request: ProcessPaymentRequest?
    ): Response<OfferEligibilityResponse>

    @POST("offer/keyfactstatement")
    suspend fun getKFS(
        @Query("token", encoded = true) token: String?,
        @Body request: ProcessPaymentRequest?
    ): Response<KFSResponse>


    @POST("update/address")
    suspend fun updateAddress(
        @Query("token", encoded = true) token: String?,
        @Body request: AddressRequest?
    ): Response<AddressResponse>


    @POST("log")
    suspend fun log(
        @Query("token", encoded = true) token: String?,
        @Body request: LogRequest?
    ): Response<String>


}

