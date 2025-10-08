package com.plural_pinelabs.expresscheckoutsdk.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.plural_pinelabs.expresscheckoutsdk.logger.SdkLogger

class NetworkHelper(private val context: Context) {
    fun hasInternetConnection(): Boolean {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } catch (e: Exception) {
            SdkLogger.log(
                context,
                "NETWORK_CHECK_FAILED",
                e.message,
                "",
                "HIGH",
                "SDK"
            )
            return false
        }
    }

}