package com.plural_pinelabs.expresscheckoutsdk.common

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import com.airbnb.lottie.LottieAnimationView
import com.clevertap.android.sdk.CleverTapAPI
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.BuildConfig
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.APP_VERSION
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EMAIL_REGEX
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.IMAGE_LOGO
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MOBILE_REGEX
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.OS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PLATFORM_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PLATFORM_VERSION
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SDK_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TRANSACTION_TYPE_SDK
import com.plural_pinelabs.expresscheckoutsdk.data.model.Palette
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode
import com.plural_pinelabs.expresscheckoutsdk.data.model.RecyclerViewPaymentOptionData
import com.plural_pinelabs.expresscheckoutsdk.data.model.SDKData
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.text.DecimalFormat
import java.util.TimeZone
import java.util.regex.Pattern
import kotlin.math.pow

internal object Utils {

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

    fun mapPaymentModes(paymentMode: PaymentMode): RecyclerViewPaymentOptionData {

        var paymentModeData = RecyclerViewPaymentOptionData()
        when (paymentMode.paymentModeId) {
            PaymentModes.CREDIT_DEBIT.toString() -> paymentModeData =
                RecyclerViewPaymentOptionData(
                    PaymentModes.CREDIT_DEBIT.paymentModeImage,
                    PaymentModes.CREDIT_DEBIT.paymentModeName,
                    PaymentModes.CREDIT_DEBIT.paymentModeDescription

                )

            PaymentModes.NET_BANKING.toString() -> paymentModeData =
                RecyclerViewPaymentOptionData(
                    PaymentModes.NET_BANKING.paymentModeImage,
                    PaymentModes.NET_BANKING.paymentModeName,
                    PaymentModes.NET_BANKING.paymentModeDescription
                )

            PaymentModes.UPI.toString() -> paymentModeData = RecyclerViewPaymentOptionData(
                PaymentModes.UPI.paymentModeImage,
                PaymentModes.UPI.paymentModeName,
                PaymentModes.UPI.paymentModeDescription
            )

            PaymentModes.WALLET.toString() -> paymentModeData = RecyclerViewPaymentOptionData(
                PaymentModes.WALLET.paymentModeImage,
                PaymentModes.WALLET.paymentModeName,
                PaymentModes.WALLET.paymentModeDescription
            )

            PaymentModes.EMI.toString() -> paymentModeData = RecyclerViewPaymentOptionData(
                PaymentModes.EMI.paymentModeImage,
                PaymentModes.EMI.paymentModeName,
                PaymentModes.EMI.paymentModeDescription
            )
        }

        return paymentModeData
    }

    val cardTypes = mapOf(
        "AMEX" to "^3[47]\\d{13}$".toRegex(),
        "BCGlobal" to "^(6541|6556)\\d{12}$".toRegex(),
        "Carte Blanche" to "^389\\d{11}$".toRegex(),
        "Diners Club" to "^3(?:0[0-5]|[68]\\d)\\d{11}$".toRegex(),
        "Discover" to "^(6[54][4-9]\\d{12}|622(12[6-9]|1[3-9]\\d|[2-8]\\d\\d|9[01]\\d|92[0-5])\\d{10})$".toRegex(),
        "Insta Payment" to "^63[7-9]\\d{13}$".toRegex(),
        "JCB" to "^(?:2131|1800|35\\d{3})\\d{11}$".toRegex(),
        "KoreanLocal" to "^9\\d{15}$".toRegex(),
        "Laser" to "^(6304|6706|6709|6771)\\d{12,15}$".toRegex(),
        "Maestro" to "^(5018|5020|5038|6304|6759|6761|6763)\\d{8,15}$".toRegex(),
        "MASTERCARD" to "^(5[1-5]\\d{14}|2(22[1-9]|2[3-9]\\d|[3-6]\\d\\d|7[01])\\d{12})$".toRegex(),
        "RUPAY" to Regex("^6(?!011)(?:\\d{15}|52[12]\\d{12})$"),
        "Solo" to "^((6334|6767)\\d{12}|(6334|6767)\\d{14}|(6334|6767)\\d{15})$".toRegex(),
        "Switch" to "^((49(0[35]|1[16]|36)|6(333|759))\\d{12,15}|564182\\d{10,13}|633110\\d{10,13})$".toRegex(),
        "Union Pay" to "^(62\\d{14,17})$".toRegex(),
        "VISA" to "^4\\d*$".toRegex(),
        "Visa Master" to "^(?:4\\d{12}(?:\\d{3})?|5[1-5]\\d{14})$".toRegex()
    )

    val cardIcons = mapOf(
        "AMEX" to R.drawable.amex,
        "VISA" to R.drawable.visa,
        "MASTERCARD" to R.drawable.mc,
        "RUPAY" to R.drawable.rupay,
        "Diners Club" to R.drawable.diners
    )

    fun convertToRupees(context: Context, amountInPaisa: Int?): String {
        if (amountInPaisa == null) {
            return "Some error occurred"
        }
        return context.getString(R.string.rupee_symbol) + " " + roundToDecimal(amountInPaisa.toDouble() / 100)
    }

    fun convertInRupees(amountInPaisa: Int?): Double {
        if (amountInPaisa == null) {
            return 0.0
        }
        return amountInPaisa.toDouble() / 100
    }

    fun convertToPaisa(amountInRupees: Double): Double {
        return amountInRupees * 100
    }

    fun roundToDecimal(amount: Double): String {
        val df = DecimalFormat("0.00")
        return df.format(amount)
    }

    fun transformAmount(ratio: Int, amountInPaisa: Int?): String {
        if (ratio > 1) {
            val divideBy = "10".toDouble().pow(ratio.toDouble())
            return amountInPaisa?.div(divideBy).toString()
        } else
            return amountInPaisa.toString()
    }

    fun convertTransformation(ratio: Int, amount: Double?): String {
        if (ratio > 1) {
            val multiply = "10".toDouble().pow(ratio.toDouble())
            val value = amount?.times(multiply)
            return roundToDecimal(value ?: 0.0)
        } else {
            return amount.toString()
        }
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

    fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
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


    fun createSDKData(context: Context): SDKData {
        return SDKData(
            TRANSACTION_TYPE_SDK,
            SDK_TYPE,
            APP_VERSION,
            APP_VERSION,
            BuildConfig.LIBRARY_PACKAGE_NAME,
            getDeviceName(),
            getDeviceId(context),
            PLATFORM_TYPE,
            OS,
            Build.VERSION.SDK,
            System.currentTimeMillis().toString(),
            PLATFORM_VERSION
        )
    }

    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        if (model.startsWith(manufacturer)) {
            return capitalize(model)
        }
        return capitalize(manufacturer) + " " + model
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.getContentResolver(),
            Settings.Secure.ANDROID_ID
        )
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true

        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(c.uppercaseChar())
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }

        return phrase.toString()
    }


    fun decodeBase64ToBitmap(base64String: String?): Bitmap? {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }


    fun showProcessPaymentDialog(context: Context): BottomSheetDialog {
        val bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context)
            .inflate(
                R.layout.processing_full_screen_dialog,
                null
            ) // Use `null` for parent in inflate

        // Set the content view *before* trying to get the BottomSheetBehavior
        bottomSheetDialog.setContentView(view)

        // IMPORTANT: Get the internal FrameLayout that holds the bottom sheet content
        val bottomSheet =
            bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        // Ensure the bottomSheet is not null
        bottomSheet?.let {

            val behavior = BottomSheetBehavior.from(it) // Apply behavior to the correct FrameLayout
            // Set height to MATCH_PARENT
            val layoutParams = it.layoutParams
            // Get the screen height programmatically
            val displayMetrics = Resources.getSystem().displayMetrics
            val screenHeight = displayMetrics.heightPixels
            layoutParams.height = screenHeight // Use screen height
            it.layoutParams = layoutParams
            behavior.peekHeight = screenHeight
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isFitToContents = false
            behavior.skipCollapsed = true
            it.setBackgroundColor(Color.TRANSPARENT) // Or use a translucent color
        }
        val logoAnimation: LottieAnimationView = view.findViewById(R.id.img_process_logo)
        logoAnimation.setAnimationFromUrl(IMAGE_LOGO)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        bottomSheetDialog.show()
        return bottomSheetDialog
    }


    fun showProcessPaymentBottomSheetDialog(
        context: Context,
        cancelPaymentText: String? = null,
        itemClickListener: ItemClickListener<Boolean>? = null
    ): BottomSheetDialog {
        val bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.process_payment_bottom_sheet, null)
        val processImageLogo: LottieAnimationView = view.findViewById(R.id.img_process_logo)
        val processCancelButton: TextView = view.findViewById(R.id.cancel_payment_text)
        cancelPaymentText?.let {
            if (it.isNotEmpty()) {
                processCancelButton.text = it
            }
        }
        processCancelButton.setOnClickListener {
            itemClickListener?.onItemClick(0, true)
            bottomSheetDialog.dismiss()
            showCancelPaymentDialog(context)
        }
        processImageLogo.setAnimationFromUrl(IMAGE_LOGO)
        processImageLogo.repeatCount = 350

        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)
        bottomSheetDialog.setContentView(view)

        bottomSheetDialog.show() // Show the dialog first
        return bottomSheetDialog
    }


    fun showCancelPaymentDialog(context: Context): BottomSheetDialog {
        val bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context)
            .inflate(
                R.layout.cancel_bottom_sheet_layout,
                null
            )
        bottomSheetDialog.setContentView(view)
        val cancelYesButton: Button = view.findViewById(R.id.cancel_yes_btn)
        val cancelNoButton: Button = view.findViewById(R.id.cancel_no_btn)
        val cancelButton: Button = view.findViewById(R.id.cancel_btn)
        cancelNoButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        cancelYesButton.setOnClickListener {
            // Handle the cancel action here
            //TODO cancel the payment
            bottomSheetDialog.dismiss()
        }

        cancelButton.setOnClickListener {
            // Handle the cancel action here
            //TODO cancel the payment pass an interface to here and listen to the click use the existing item click listener with boolean
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setCanceledOnTouchOutside(true)
        bottomSheetDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        bottomSheetDialog.show()
        return bottomSheetDialog
    }

    fun formatTimeInMinutes(context: Context, millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        Log.d("timer", "Minutes: $minutes, Remaining Seconds: $remainingSeconds")
        return String.format(context.getString(R.string.timer_format), minutes, remainingSeconds)
    }

}