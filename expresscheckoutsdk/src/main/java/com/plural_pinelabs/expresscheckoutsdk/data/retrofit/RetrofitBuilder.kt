package com.plural_pinelabs.expresscheckoutsdk.data.retrofit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.plural_pinelabs.expresscheckoutsdk.BuildConfig
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_CHECKOUTBFF
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_URL_PROD
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_URL_QA
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_URL_UAT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.HTTPS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TIMEOUT
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModeDeserialiser
import com.plural_pinelabs.expresscheckoutsdk.data.fetch.FetchApiService
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Base64
import java.util.concurrent.TimeUnit

object RetrofitBuilder {

    private val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val clientBuilder: OkHttpClient.Builder = createBuilder()

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Any::class.java, PaymentModeDeserialiser())
        .create()

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(HTTPS + BASE_URL_UAT + BASE_CHECKOUTBFF)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(clientBuilder.build())
            .build()
    }

    val fetchApiService: FetchApiService = getRetrofit().create(FetchApiService::class.java)

    private fun createBuilder(): OkHttpClient.Builder {

        val sha256QA = String(Base64.getDecoder().decode(BuildConfig.SHA256_QA))
        val sha256UAT = String(Base64.getDecoder().decode(BuildConfig.SHA256_UAT))
        val sha256PROD = String(Base64.getDecoder().decode(BuildConfig.SHA256_PROD))

        val certificatePinner_QA = CertificatePinner.Builder()
            .add(BASE_URL_QA, sha256QA)
            .build()

        val certificatePinner_UAT = CertificatePinner.Builder()
            .add(BASE_URL_UAT, sha256UAT)
            .build()

        val certificatePinner_PROD = CertificatePinner.Builder()
            .add(BASE_URL_PROD, sha256PROD)
            .build()

        val clientBuilder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) clientBuilder.addInterceptor(interceptor)

        clientBuilder.certificatePinner(certificatePinner_QA)
        clientBuilder.certificatePinner(certificatePinner_UAT)
        clientBuilder.certificatePinner(certificatePinner_PROD)
        clientBuilder.connectTimeout(TIMEOUT, TimeUnit.SECONDS)

        clientBuilder.readTimeout(TIMEOUT, TimeUnit.SECONDS)

        clientBuilder.writeTimeout(TIMEOUT, TimeUnit.SECONDS)

        return clientBuilder
    }
}