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
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_URL_UAT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.HTTPS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TIMEOUT
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
    private val client: OkHttpClient by lazy { createClient() }

    private val gson: Gson = GsonBuilder()
        .create()

    private fun getRetrofit(): Retrofit {
        val baseUrl = if (ExpressSDKObject.isSandBoxMode()) BASE_URL_UAT else BASE_URL_PROD
        return Retrofit.Builder()
            .baseUrl(HTTPS + baseUrl + BASE_CHECKOUTBFF)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }

    private fun getRetrofitForCheckout(): Retrofit {
        val baseUrl = if (ExpressSDKObject.isSandBoxMode()) BASE_URL_UAT else BASE_URL_PROD
        return Retrofit.Builder()
            .baseUrl(HTTPS + baseUrl + BASE_CHECKOUT)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
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
            .client(client)
            .build()
    }

    // Resolve the environment when a checkout starts instead of freezing the
    // first environment used by this process for all later SDK sessions.
    val fetchApiService: FetchApiService
        get() = getRetrofit().create(FetchApiService::class.java)
    val commonApiService: CommonApiService
        get() = getRetrofit().create(CommonApiService::class.java)
    val expressApiService: ExpressApiService
        get() = getRetrofitForExpressCheckout().create(ExpressApiService::class.java)
    val checkoutApiService: CommonApiService
        get() = getRetrofitForCheckout().create(CommonApiService::class.java)

    private fun createClient(): OkHttpClient {

        val sha256UAT = String(Base64.getDecoder().decode(BuildConfig.SHA256_UAT))
        // val sha256PROD = String(Base64.getDecoder().decode(BuildConfig.SHA256_PROD))
        val sha256PROD = "sha256/" + BuildConfig.SHA256_PROD
        val certificatePinner = CertificatePinner.Builder()
            .add(BASE_URL_UAT, sha256UAT)
            .add(BASE_URL_PROD, sha256PROD)
            .add(BASE_URL_PROD, "sha256/" + BuildConfig.SHA256_PROD_BACKUP)
            .add(BASE_URL_PROD, "sha256/" + BuildConfig.SHA256_PROD_CERT_BACKUP)
            .build()

        val clientBuilder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) clientBuilder.addInterceptor(interceptor)

        // certificatePinner(...) replaces the previous value, so all host/pin
        // pairs must be installed in one CertificatePinner instance.
        clientBuilder.certificatePinner(certificatePinner)
        clientBuilder.connectTimeout(TIMEOUT, TimeUnit.SECONDS)

        clientBuilder.readTimeout(TIMEOUT, TimeUnit.SECONDS)

        clientBuilder.writeTimeout(TIMEOUT, TimeUnit.SECONDS)

        return clientBuilder.build()
    }
}
