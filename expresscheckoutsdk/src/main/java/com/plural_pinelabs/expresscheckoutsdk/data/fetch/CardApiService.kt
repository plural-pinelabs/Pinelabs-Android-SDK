package com.plural_pinelabs.expresscheckoutsdk.data.fetch

import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataRequestList
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.ApiService
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface CardApiService : ApiService {

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
}

