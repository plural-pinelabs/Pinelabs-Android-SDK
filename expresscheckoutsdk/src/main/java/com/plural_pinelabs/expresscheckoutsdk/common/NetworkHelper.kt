package com.plural_pinelabs.expresscheckoutsdk.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.TimeZone

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
            return false
        }
    }

    fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val nextEle = en.nextElement()
                val enumIpAddress = nextEle.inetAddresses
                while (enumIpAddress.hasMoreElements()) {
                    val inetAddress = enumIpAddress.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }

    fun getTimeOffset(): Int {
        // Get the default time zone of the device
        val timeZone: TimeZone = TimeZone.getDefault()
        // Get the offset from UTC in milliseconds
        val offsetMillis: Int = timeZone.getOffset(System.currentTimeMillis())
        // Convert milliseconds to minutes
        val offsetMinutes = offsetMillis / (1000 * 60)
        return offsetMinutes
    }
}