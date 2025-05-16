package com.plural_pinelabs.expresscheckoutsdk.data.fetch

import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface FetchApiService : ApiService {
    @POST("fetch/data")
    suspend fun fetchData(
        @Query(
            "token",
            encoded = true
        ) token: String?
    ): Response<FetchResponseDTO>

}