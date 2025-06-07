package com.plural_pinelabs.expresscheckoutsdk.data.fetch

import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddress
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddressResponse
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.ApiService
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ExpressApiService : ApiService {

    @POST("graphql")
  suspend   fun fetchAddress(
        @Header("Authorization") authHeader: String?,
        @Body request: ExpressAddress
    ): Response<ExpressAddressResponse>


}