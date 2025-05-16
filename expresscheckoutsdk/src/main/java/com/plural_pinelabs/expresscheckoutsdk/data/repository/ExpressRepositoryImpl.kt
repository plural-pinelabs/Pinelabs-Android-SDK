package com.plural_pinelabs.expresscheckoutsdk.data.repository

import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.toResultFlow
import com.plural_pinelabs.expresscheckoutsdk.data.fetch.FetchApiService
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.ApiService
import com.plural_pinelabs.expresscheckoutsdk.domain.repository.ExpressRepository
import kotlinx.coroutines.flow.Flow

class ExpressRepositoryImpl(
    private val apiService: ApiService,
    private val networkHelper: NetworkHelper
) : ExpressRepository {
    override suspend fun fetchData(token:String?): Flow<BaseResult<FetchResponseDTO>> {
        // call the API to fetch data
        return toResultFlow(networkHelper = networkHelper) {
            (apiService as FetchApiService).fetchData(token)
        }
    }
}