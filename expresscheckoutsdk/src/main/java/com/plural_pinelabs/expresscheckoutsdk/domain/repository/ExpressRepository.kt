package com.plural_pinelabs.expresscheckoutsdk.domain.repository

import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataRequestList
import com.plural_pinelabs.expresscheckoutsdk.data.model.CardBinMetaDataResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentResponse
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

}