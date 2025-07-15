package com.plural_pinelabs.expresscheckoutsdk.data.fetch

import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddress
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddressResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardResponse
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.ApiService
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ExpressApiService : ApiService {

    @POST("graphql")
  suspend   fun fetchAddress(
        @Header("Authorization") authHeader: String?,
        @Body request: ExpressAddress
    ): Response<ExpressAddressResponse>

    @POST("customer/create-inactive")
    suspend fun createInactive(
        @Query(
            "token",
            encoded = true
        ) token: String?, @Body request: CustomerInfo?
    ): Response<CustomerInfo>

    @POST("customer/otp/send")
    suspend fun sendOTPCustomer(
        @Query(
            "token",
            encoded = true
        ) token: String?, @Body request: OTPRequest?
    ): Response<SavedCardResponse>


}