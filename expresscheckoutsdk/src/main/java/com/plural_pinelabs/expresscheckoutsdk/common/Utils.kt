package com.plural_pinelabs.expresscheckoutsdk.common

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.toColorInt
import com.clevertap.android.sdk.CleverTapAPI
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EMAIL_REGEX
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MOBILE_REGEX
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.TimeZone
import java.util.regex.Pattern

object Utils {

    val MTAG: String
        get() {
            val stackTrace = Throwable().stackTrace
            return if (stackTrace.size > 1) {
                stackTrace[1].className.substringAfterLast(".")
            } else {
                "ExpressSDK"
            }
        }

    fun hasInternetConnection(context: Context?): Boolean {
        try {
            if (context == null)
                return false
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

    // Method to map the pixel format to color depth in bits
    fun getColorDepth(pixelFormat: Int): Int {
        return when (pixelFormat) {
            PixelFormat.RGBA_8888 -> 32 // 32 bits per pixel
            PixelFormat.RGB_565 -> 16 // 16 bits per pixel
            PixelFormat.RGBA_4444 -> 16 // 16 bits per pixel
            PixelFormat.RGBX_8888 -> 32 // 32 bits per pixel
            else -> 24 // Default to 24 bits per pixel
        }
    }

    fun isValidPhoneNumber(phoneNumber: String?): Boolean {
        val regex = MOBILE_REGEX
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(phoneNumber.toString())
        return matcher.matches()
    }

    fun isValidEmail(email: String?): Boolean {
        val regex = EMAIL_REGEX
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(email.toString())
        return matcher.matches()
    }

    fun isValidName(name: String): Boolean {
        val regex = "^[A-Za-z]+(?:[ '-][A-Za-z]+)*$".toRegex()
        return name.matches(regex)
    }

    fun isValidPincode(pincode: String): Boolean {
        val postalCodeRegex =
            "^([A-Za-z0-9]{3,10}[-\\s]?[A-Za-z0-9]{3,10})\$|^(\\d{4,10})\$|^[A-Za-z]\\d[A-Za-z] \\d[A-Za-z]\\d\$|^\\d{5}(-\\d{4})?\$\n".toRegex()
        return pincode.matches(postalCodeRegex)
    }

    fun buttonBackground(context: Context, palette: Palette?): Drawable {

        val stateListDrawable = StateListDrawable()

        // Create different drawables for different states
        val pressedDrawable = GradientDrawable().apply {
            if (palette?.C900.isNullOrEmpty()) {
                color = AppCompatResources.getColorStateList(context, R.color.colorPrimary)
            } else {
                palette?.C900?.toColorInt()?.let { setColor(it) }
            }


            cornerRadius = 16f // Normal corner radius
        }

        // Add states to the StateListDrawable
        stateListDrawable.addState(intArrayOf(android.R.attr.state_enabled), pressedDrawable)
        stateListDrawable.addState(intArrayOf(), pressedDrawable) // Default state

        return stateListDrawable
    }

    fun cleverTapLog() {
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.OFF)
    }


}