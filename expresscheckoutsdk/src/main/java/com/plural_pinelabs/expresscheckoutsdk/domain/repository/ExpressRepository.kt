package com.plural_pinelabs.expresscheckoutsdk.domain.repository

import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import kotlinx.coroutines.flow.Flow

interface ExpressRepository {

    suspend fun fetchData(token: String?): Flow<BaseResult<FetchResponseDTO>>
}