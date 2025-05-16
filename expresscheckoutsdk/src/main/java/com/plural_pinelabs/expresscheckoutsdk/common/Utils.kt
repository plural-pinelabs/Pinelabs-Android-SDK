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
import com.plural_pinelabs.expresscheckoutsdk.data.model.Palette
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