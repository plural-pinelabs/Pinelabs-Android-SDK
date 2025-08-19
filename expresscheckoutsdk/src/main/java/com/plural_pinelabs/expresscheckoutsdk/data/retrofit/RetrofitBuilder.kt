package com.plural_pinelabs.expresscheckoutsdk.data.retrofit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.plural_pinelabs.expresscheckoutsdk.BuildConfig
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_CHECKOUT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_CHECKOUTBFF
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_URL_EXPRESS_PROD
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_URL_EXPRESS_UAT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_URL_PROD
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_URL_QA
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_URL_UAT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.HTTPS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TIMEOUT
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModeDeserialiser
import com.plural_pinelabs.expresscheckoutsdk.data.fetch.CommonApiService
import com.plural_pinelabs.expresscheckoutsdk.data.fetch.ExpressApiService
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
        val baseUrl = if (ExpressSDKObject.isSandBoxMode()) BASE_URL_UAT else BASE_URL_PROD
        return Retrofit.Builder()
            .baseUrl(HTTPS + baseUrl + BASE_CHECKOUTBFF)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(clientBuilder.build())
            .build()
    }

    private fun getRetrofitForCheckout(): Retrofit {
        val baseUrl = if (ExpressSDKObject.isSandBoxMode()) BASE_URL_UAT else BASE_URL_PROD
        return Retrofit.Builder()
            .baseUrl(HTTPS + baseUrl + BASE_CHECKOUT)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(clientBuilder.build())
            .build()
    }


    private fun getRetrofitForExpressCheckout(): Retrofit {
        //TODO update the prod url of the express checout and dev to UAT
        val baseUrl =
            if (ExpressSDKObject.isSandBoxMode()) BASE_URL_EXPRESS_UAT else BASE_URL_EXPRESS_PROD
        return Retrofit.Builder()
            .baseUrl(HTTPS + baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(clientBuilder.build())
            .build()
    }

    val fetchApiService: FetchApiService = getRetrofit().create(FetchApiService::class.java)
    val commonApiService: CommonApiService = getRetrofit().create(CommonApiService::class.java)
    val expressApiService: ExpressApiService =
        getRetrofitForExpressCheckout().create(ExpressApiService::class.java)
    val checkoutApiService: CommonApiService =
        getRetrofitForCheckout().create(CommonApiService::class.java)

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