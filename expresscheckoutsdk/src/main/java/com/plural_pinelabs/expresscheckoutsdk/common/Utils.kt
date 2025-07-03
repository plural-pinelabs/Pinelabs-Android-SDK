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
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ALLAHABAD_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ALLAHABAD_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ANDHRA_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ANDHRA_CORPORATE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ANDHRA_CORPORATE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ANDHRA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.APP_VERSION
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.AU_SMALL_FINANCE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.AU_SMALL_FINANCE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.AXIS_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.AXIS_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BAHRAIN_AND_KUWAIT_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BAHRAIN_AND_KUWAIT_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BANDHAN_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BANDHAN_BANK_CORPORATE_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BANDHAN_CORPORATE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BANDHAN_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BARCLAYS_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BARCLAYS_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BARODA_RETAIL_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BARODA_RETAIL_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BARODA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASSIEN_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASSIEN_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CANARA_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CANARA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CATHOLIC_SYRIAN_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CATHOLIC_SYRIAN_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CENTRAL_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CENTRAL_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CITY_UNION_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CITY_UNION_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CORPORATION_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CORPORATION_CORPORATE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CORPORATION_CORPORATE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CORPORATION_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.COSMOS_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.COSMOS_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DEDUSCHE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DEDUSCHE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DEFAULT_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DEFAULT_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DENA_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DENA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DEVELOPMENT_CREDIT_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DEVELOPMENT_CREDIT_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DHANLAXMI_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DHANLAXMI_CORPORATE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DHANLAXMI_CORPORATE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DHANLAXMI_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.DISPLAY_NAME_SUFFIX
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EMAIL_REGEX
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EMI_CC_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EMI_DC_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EQUITAS_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EQUITAS_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ESAF_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ESAF_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.FEDERAL_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.FEDERAL_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.FINCARE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.FINCARE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.HDFC_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.HDFC_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ICICI_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ICICI_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.IDBI_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.IDBI_CORPORATE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.IDBI_CORPORATE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.IDBI_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.IDFC_FIRST_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.IDFC_FIRST_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.IMAGE_LOGO
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.INDIAN_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.INDIAN_OVERSEAS_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.INDIAN_OVERSEAS_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.INDIAN_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.INDUSIND_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.INDUSIND_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.JANATA_SAHAKARI_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.JANATA_SAHAKARI_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.JK_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.JK_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KALYAN_JANATA_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KALYAN_JANATA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KARNATAKA_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KARNATAKA_GRAMIN_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KARNATAKA_GRAMIN_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KARNATAKA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KARUR_VYSYA_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KARUR_VYSYA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KOTAK_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.KOTAK_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.LAXMI_VILAS_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.LAXMI_VILAS_BANK_RETAIL_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.LAXMI_VILAS_RETAIL_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.LAXMI_VILAS_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.LOW_COST_EMI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MAHARASHTRA_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MAHARASHTRA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MEHSANA_URBAN_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MEHSANA_URBAN_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.MOBILE_REGEX
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.NORTH_EAST_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.NORTH_EAST_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.NO_COST_EMI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ORIENTAL_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ORIENTAL_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.OS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PLATFORM_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PLATFORM_VERSION
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PNB_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PNB_CORPORATE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PNB_CORPORATE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PNB_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PUNJAB_AND_SINDH_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PUNJAB_AND_SINDH_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PUNJAB_MAHARASHTRA_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PUNJAB_MAHARASHTRA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.RBL_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.RBL_CORPORATE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.RBL_CORPORATE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.RBL_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SARASWAT_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SARASWAT_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SBI_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SBI_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SDK_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SHAMRO_VITHAL_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SHAMRO_VITHAL_CORPORATE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SHAMRO_VITHAL_CORPORATE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SHAMRO_VITHAL_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SOUTH_INDIAN_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SOUTH_INDIAN_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.STANDARD_CHARTERED_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.STANDARD_CHARTERED_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SURYODAY_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SURYODAY_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SYNDICATE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SYNDICATE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TAMILNAD_MERCHANTILE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TAMILNAD_MERCHANTILE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TAMIL_NADU_STATE_COOP_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TAMIL_NADU_STATE_COOP_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.THANE_BHARAT_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.THANE_BHARAT_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.THE_KALUPUR_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.THE_KALUPUR_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TJSB_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TJSB_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TRANSACTION_TYPE_SDK
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UCO_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UCO_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UNION_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UNION_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UNITED_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UNITED_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.VARACHHA_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.VARACHHA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.VIJAYA_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.VIJAYA_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.YES_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.YES_CORPORATE_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.YES_CORPORATE_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.YES_TITLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ZOROASTRAIN_BANK_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ZOROASTRAIN_TITLE
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer
import com.plural_pinelabs.expresscheckoutsdk.data.model.Palette
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode
import com.plural_pinelabs.expresscheckoutsdk.data.model.RecyclerViewPaymentOptionData
import com.plural_pinelabs.expresscheckoutsdk.data.model.SDKData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.text.DecimalFormat
import java.util.TimeZone
import java.util.regex.Pattern
import kotlin.math.pow
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BARODA_BANK_CODE as BARODA_BANK_CODE1

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


    fun formatToIndianNumbering(value: Double): String {
        val formatter = DecimalFormat("##,##,###.00")
        return formatter.format(value)
    }


    fun convertToRupees(context: Context, amountInPaisa: Int?): String {
        if (amountInPaisa == null) {
            return "Some error occurred"
        }
        return context.getString(R.string.rupee_symbol) + " " + formatToIndianNumbering(
            amountInPaisa.toDouble() / 100
        )
    }

    fun convertInRupees(amountInPaisa: Int?): String {
        if (amountInPaisa == null) {
            return "Error"
        }
        return formatToIndianNumbering(amountInPaisa.toDouble() / 100)
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
            context.contentResolver,
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

    fun getBankLogoHashMap(): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        hashMap[AXIS_BANK_CODE] = Constants.AXIS
        hashMap[HDFC_BANK_CODE] = Constants.HDFC
        hashMap[ICICI_BANK_CODE] = Constants.ICICI
        hashMap[JK_BANK_CODE] = Constants.JK
        hashMap[FEDERAL_BANK_CODE] = Constants.FEDERAL
        hashMap[KARNATAKA_BANK_CODE] = Constants.KARNATAKA
        hashMap[CORPORATION_BANK_CODE] = Constants.CORPORATION
        hashMap[INDIAN_BANK_CODE] = Constants.INDIAN
        hashMap[YES_BANK_CODE] = Constants.YES
        hashMap[CENTRAL_BANK_CODE] = Constants.CENTRAL
        hashMap[KOTAK_BANK_CODE] = Constants.KOTAK
        hashMap[ORIENTAL_BANK_CODE] = Constants.ORIENTAL
        hashMap[UNITED_BANK_CODE] = Constants.UNITED_BANK
        hashMap[INDIAN_OVERSEAS_BANK_CODE] = Constants.INDIAN_OVERSEAS
        hashMap[CITY_UNION_BANK_CODE] = Constants.CITY_UNION
        hashMap[UNION_BANK_CODE] = Constants.UNION_BANK
        hashMap[CANARA_BANK_CODE] = Constants.CANARA_BANK
        hashMap[MAHARASHTRA_BANK_CODE] = Constants.MAHARASHTRA_BANK
        hashMap[CATHOLIC_SYRIAN_BANK_CODE] = Constants.CATHOLIC_SYRIAN
        hashMap[DHANLAXMI_BANK_CODE] = Constants.DHANALAKSHMI
        hashMap[ANDHRA_BANK_CODE] = Constants.ANDHRA
        hashMap[VIJAYA_BANK_CODE] = Constants.VIJAYA
        hashMap[SARASWAT_BANK_CODE] = Constants.SARASWAT
        hashMap[PNB_BANK_CODE] = Constants.PNB
        hashMap[UCO_BANK_CODE] = Constants.UCO
        hashMap[PUNJAB_AND_SINDH_BANK_CODE] = Constants.PUNJAB_AND_SINDH
        hashMap[INDUSIND_BANK_CODE] = Constants.INDUSIND
        hashMap[TAMILNAD_MERCHANTILE_BANK_CODE] = Constants.TAMINAD_MERCHANTILE
        hashMap[COSMOS_BANK_CODE] = Constants.COSMOS
        hashMap[PNB_CORPORATE_BANK_CODE] = Constants.PNB_CORPORATE
        hashMap[ANDHRA_CORPORATE_BANK_CODE] = Constants.ANDHRA_CORPORATE
        hashMap[BARODA_BANK_CODE1] = Constants.BARODA
        hashMap[BAHRAIN_AND_KUWAIT_BANK_CODE] = Constants.BAHRAIN_KUWAIT
        hashMap[BARODA_RETAIL_BANK_CODE] = Constants.BARODA_RETAIL
        hashMap[DEDUSCHE_BANK_CODE] = Constants.DEDUSHE
        hashMap[DEVELOPMENT_CREDIT_BANK_CODE] = Constants.DEVELOPMENT
        hashMap[DENA_BANK_CODE] = Constants.DENA
        hashMap[IDBI_BANK_CODE] = Constants.IDBI
        hashMap[KARUR_VYSYA_BANK_CODE] = Constants.KARUR
        hashMap[LAXMI_VILAS_BANK_CODE] = Constants.LAXMI_VILAS
        hashMap[LAXMI_VILAS_BANK_RETAIL_CODE] = Constants.LAXMI_VILAS_RETAIL
        hashMap[PUNJAB_MAHARASHTRA_BANK_CODE] = Constants.PUNJAB_MAHARASHTRA
        hashMap[STANDARD_CHARTERED_BANK_CODE] = Constants.STANDARD_CHARTERED_BANK
        hashMap[SOUTH_INDIAN_BANK_CODE] = Constants.SOUTH_INDIAN_BANK
        hashMap[SHAMRO_VITHAL_BANK_CODE] = Constants.SHAMRO
        hashMap[SYNDICATE_BANK_CODE] = Constants.SYNDICATE
        hashMap[TAMIL_NADU_STATE_COOP_BANK_CODE] = Constants.TAMIL_NADU_STATE_COORP
        hashMap[JANATA_SAHAKARI_BANK_CODE] = Constants.JANATA
        hashMap[TJSB_BANK_CODE] = Constants.TJSB
        hashMap[KALYAN_JANATA_BANK_CODE] = Constants.KALYAN_JANATA
        hashMap[MEHSANA_URBAN_BANK_CODE] = Constants.MEHSANA
        hashMap[BANDHAN_BANK_CODE] = Constants.BANDHAN
        hashMap[IDFC_FIRST_BANK_CODE] = Constants.IDFC
        hashMap[BASSIEN_BANK_CODE] = Constants.BASSIEN
        hashMap[RBL_BANK_CODE] = Constants.RBL
        hashMap[THE_KALUPUR_BANK_CODE] = Constants.KALUPUR
        hashMap[THANE_BHARAT_BANK_CODE] = Constants.THANE
        hashMap[SURYODAY_BANK_CODE] = Constants.SURYODAY
        hashMap[ESAF_BANK_CODE] = Constants.ESAF
        hashMap[VARACHHA_BANK_CODE] = Constants.VARACHHA
        hashMap[NORTH_EAST_BANK_CODE] = Constants.NORTH_EAST
        hashMap[IDBI_CORPORATE_BANK_CODE] = Constants.IDBI_CORPORATE
        hashMap[YES_CORPORATE_BANK_CODE] = Constants.YES_CORPORATE
        hashMap[CORPORATION_CORPORATE_BANK_CODE] = Constants.CORPORATION_CORPORATE
        hashMap[RBL_CORPORATE_BANK_CODE] = Constants.RBL_CORPORATE
        hashMap[SHAMRO_VITHAL_CORPORATE_BANK_CODE] = Constants.SHAMRO_CORPORATE
        hashMap[DHANLAXMI_CORPORATE_BANK_CODE] = Constants.DHANLAKSHMI
        hashMap[BARCLAYS_BANK_CODE] = Constants.BARCLAYS
        hashMap[ZOROASTRAIN_BANK_CODE] = Constants.ZOROASTRAIN
        hashMap[AU_SMALL_FINANCE_BANK_CODE] = Constants.AU
        hashMap[ALLAHABAD_BANK_CODE] = Constants.ALLAHABAD
        hashMap[SBI_BANK_CODE] = Constants.SBI
        hashMap[FINCARE_BANK_CODE] = Constants.FINCARE
        hashMap[BANDHAN_BANK_CORPORATE_CODE] = Constants.BANDHAN_CORPORATE
        hashMap[KARNATAKA_GRAMIN_BANK_CODE] = Constants.KARNATAKA_GRAMIN
        hashMap[DEFAULT_BANK_CODE] = Constants.BASE_IMAGES
        return hashMap
    }

    fun getListOfBanKTitle(): List<String> {
        val bankTitleKeys = listOf(
            AXIS_TITLE,
            HDFC_TITLE,
            JK_TITLE,
            ICICI_TITLE,
            FEDERAL_TITLE,
            KARNATAKA_TITLE,
            CORPORATION_TITLE,
            INDIAN_TITLE,
            YES_TITLE,
            CENTRAL_TITLE,
            KOTAK_TITLE,
            ORIENTAL_TITLE,
            UNITED_TITLE,
            INDIAN_OVERSEAS_TITLE,
            CITY_UNION_TITLE,
            UNION_TITLE,
            CANARA_TITLE,
            MAHARASHTRA_TITLE,
            CATHOLIC_SYRIAN_TITLE,
            DHANLAXMI_TITLE,
            ANDHRA_TITLE,
            VIJAYA_TITLE,
            SARASWAT_TITLE,
            PNB_TITLE,
            UCO_TITLE,
            PUNJAB_AND_SINDH_TITLE,
            INDUSIND_TITLE,
            TAMILNAD_MERCHANTILE_TITLE,
            COSMOS_TITLE,
            PNB_CORPORATE_TITLE,
            ANDHRA_CORPORATE_TITLE,
            BARODA_TITLE,
            BAHRAIN_AND_KUWAIT_TITLE,
            BARODA_RETAIL_TITLE,
            DEDUSCHE_TITLE,
            DEVELOPMENT_CREDIT_TITLE,
            DENA_TITLE,
            IDBI_TITLE,
            KARUR_VYSYA_TITLE,
            LAXMI_VILAS_TITLE,
            LAXMI_VILAS_RETAIL_TITLE,
            PUNJAB_MAHARASHTRA_TITLE,
            STANDARD_CHARTERED_TITLE,
            SOUTH_INDIAN_TITLE,
            SHAMRO_VITHAL_TITLE,
            SYNDICATE_TITLE,
            TAMIL_NADU_STATE_COOP_TITLE,
            JANATA_SAHAKARI_TITLE,
            TJSB_TITLE,
            KALYAN_JANATA_TITLE,
            MEHSANA_URBAN_TITLE,
            BANDHAN_TITLE,
            IDFC_FIRST_TITLE,
            BASSIEN_TITLE,
            RBL_TITLE,
            THE_KALUPUR_TITLE,
            EQUITAS_TITLE,
            THANE_BHARAT_TITLE,
            SURYODAY_TITLE,
            ESAF_TITLE,
            VARACHHA_TITLE,
            NORTH_EAST_TITLE,
            IDBI_CORPORATE_TITLE,
            YES_CORPORATE_TITLE,
            CORPORATION_CORPORATE_TITLE,
            RBL_CORPORATE_TITLE,
            SHAMRO_VITHAL_CORPORATE_TITLE,
            DHANLAXMI_CORPORATE_TITLE,
            BARCLAYS_TITLE,
            ZOROASTRAIN_TITLE,
            AU_SMALL_FINANCE_TITLE,
            ALLAHABAD_TITLE,
            SBI_TITLE,
            FINCARE_TITLE,
            BANDHAN_CORPORATE_TITLE,
            KARNATAKA_GRAMIN_TITLE,
            DEFAULT_TITLE
        )
        return bankTitleKeys
    }

    fun bankTitleAndCodeMapper(
    ): HashMap<String, String> {
        val bankTitleToCodeMap = hashMapOf(
            AXIS_TITLE to AXIS_BANK_CODE,
            HDFC_TITLE to HDFC_BANK_CODE,
            JK_TITLE to JK_BANK_CODE,
            ICICI_TITLE to ICICI_BANK_CODE,
            FEDERAL_TITLE to FEDERAL_BANK_CODE,
            KARNATAKA_TITLE to KARNATAKA_BANK_CODE,
            CORPORATION_TITLE to CORPORATION_BANK_CODE,
            INDIAN_TITLE to INDIAN_BANK_CODE,
            YES_TITLE to YES_BANK_CODE,
            CENTRAL_TITLE to CENTRAL_BANK_CODE,
            KOTAK_TITLE to KOTAK_BANK_CODE,
            ORIENTAL_TITLE to ORIENTAL_BANK_CODE,
            UNITED_TITLE to UNITED_BANK_CODE,
            INDIAN_OVERSEAS_TITLE to INDIAN_OVERSEAS_BANK_CODE,
            CITY_UNION_TITLE to CITY_UNION_BANK_CODE,
            UNION_TITLE to UNION_BANK_CODE,
            CANARA_TITLE to CANARA_BANK_CODE,
            MAHARASHTRA_TITLE to MAHARASHTRA_BANK_CODE,
            CATHOLIC_SYRIAN_TITLE to CATHOLIC_SYRIAN_BANK_CODE,
            DHANLAXMI_TITLE to DHANLAXMI_BANK_CODE,
            ANDHRA_TITLE to ANDHRA_BANK_CODE,
            VIJAYA_TITLE to VIJAYA_BANK_CODE,
            SARASWAT_TITLE to SARASWAT_BANK_CODE,
            PNB_TITLE to PNB_BANK_CODE,
            UCO_TITLE to UCO_BANK_CODE,
            PUNJAB_AND_SINDH_TITLE to PUNJAB_AND_SINDH_BANK_CODE,
            INDUSIND_TITLE to INDUSIND_BANK_CODE,
            TAMILNAD_MERCHANTILE_TITLE to TAMILNAD_MERCHANTILE_BANK_CODE,
            COSMOS_TITLE to COSMOS_BANK_CODE,
            PNB_CORPORATE_TITLE to PNB_CORPORATE_BANK_CODE,
            ANDHRA_CORPORATE_TITLE to ANDHRA_CORPORATE_BANK_CODE,
            BARODA_TITLE to BARODA_BANK_CODE1,
            BAHRAIN_AND_KUWAIT_TITLE to BAHRAIN_AND_KUWAIT_BANK_CODE,
            BARODA_RETAIL_TITLE to BARODA_RETAIL_BANK_CODE,
            DEDUSCHE_TITLE to DEDUSCHE_BANK_CODE,
            DEVELOPMENT_CREDIT_TITLE to DEVELOPMENT_CREDIT_BANK_CODE,
            DENA_TITLE to DENA_BANK_CODE,
            IDBI_TITLE to IDBI_BANK_CODE,
            KARUR_VYSYA_TITLE to KARUR_VYSYA_BANK_CODE,
            LAXMI_VILAS_TITLE to LAXMI_VILAS_BANK_CODE,
            LAXMI_VILAS_RETAIL_TITLE to LAXMI_VILAS_BANK_RETAIL_CODE,
            PUNJAB_MAHARASHTRA_TITLE to PUNJAB_MAHARASHTRA_BANK_CODE,
            STANDARD_CHARTERED_TITLE to STANDARD_CHARTERED_BANK_CODE,
            SOUTH_INDIAN_TITLE to SOUTH_INDIAN_BANK_CODE,
            SHAMRO_VITHAL_TITLE to SHAMRO_VITHAL_BANK_CODE,
            SYNDICATE_TITLE to SYNDICATE_BANK_CODE,
            TAMIL_NADU_STATE_COOP_TITLE to TAMIL_NADU_STATE_COOP_BANK_CODE,
            JANATA_SAHAKARI_TITLE to JANATA_SAHAKARI_BANK_CODE,
            TJSB_TITLE to TJSB_BANK_CODE,
            KALYAN_JANATA_TITLE to KALYAN_JANATA_BANK_CODE,
            MEHSANA_URBAN_TITLE to MEHSANA_URBAN_BANK_CODE,
            BANDHAN_TITLE to BANDHAN_BANK_CODE,
            IDFC_FIRST_TITLE to IDFC_FIRST_BANK_CODE,
            BASSIEN_TITLE to BASSIEN_BANK_CODE,
            RBL_TITLE to RBL_BANK_CODE,
            THE_KALUPUR_TITLE to THE_KALUPUR_BANK_CODE,
            EQUITAS_TITLE to EQUITAS_BANK_CODE,
            THANE_BHARAT_TITLE to THANE_BHARAT_BANK_CODE,
            SURYODAY_TITLE to SURYODAY_BANK_CODE,
            ESAF_TITLE to ESAF_BANK_CODE,
            VARACHHA_TITLE to VARACHHA_BANK_CODE,
            NORTH_EAST_TITLE to NORTH_EAST_BANK_CODE,
            IDBI_CORPORATE_TITLE to IDBI_CORPORATE_BANK_CODE,
            YES_CORPORATE_TITLE to YES_CORPORATE_BANK_CODE,
            CORPORATION_CORPORATE_TITLE to CORPORATION_CORPORATE_BANK_CODE,
            RBL_CORPORATE_TITLE to RBL_CORPORATE_BANK_CODE,
            SHAMRO_VITHAL_CORPORATE_TITLE to SHAMRO_VITHAL_CORPORATE_BANK_CODE,
            DHANLAXMI_CORPORATE_TITLE to DHANLAXMI_CORPORATE_BANK_CODE,
            BARCLAYS_TITLE to BARCLAYS_BANK_CODE,
            ZOROASTRAIN_TITLE to ZOROASTRAIN_BANK_CODE,
            AU_SMALL_FINANCE_TITLE to AU_SMALL_FINANCE_BANK_CODE,
            ALLAHABAD_TITLE to ALLAHABAD_BANK_CODE,
            SBI_TITLE to SBI_BANK_CODE,
            FINCARE_TITLE to FINCARE_BANK_CODE,
            BANDHAN_CORPORATE_TITLE to BANDHAN_BANK_CORPORATE_CODE,
            KARNATAKA_GRAMIN_TITLE to KARNATAKA_GRAMIN_BANK_CODE,
            DEFAULT_TITLE to DEFAULT_BANK_CODE
        )
        return bankTitleToCodeMap

    }

    //Custom sort extension for list of tenure type so that we can sort in the order of nocost > low cost > standard
    // and each group sorted by the tenure value among itself
    fun List<Tenure>.customSorted(): List<Tenure> {
        return return this.sortedBy { it.tenure_value }
    }

    fun getTitleForEMI(context: Context, issuer: Issuer?): String {
        val title = issuer?.display_name?.removeSuffix(DISPLAY_NAME_SUFFIX) ?: ""
        var suffixText = " "
        if (issuer?.issuer_type?.equals(EMI_CC_TYPE) == true) {
            suffixText = context.getString(R.string.credit_card)

        } else if (issuer?.issuer_type?.equals(EMI_DC_TYPE) == true) {
            suffixText = context.getString(R.string.debit_card)
        }
        return context.getString(
            R.string.issuer_title_with_emi_card,
            title,
            suffixText
        )
    }

    fun getMaxDiscountTenurePerIssuer(issuers: List<Issuer>): HashMap<String, Tenure> {
        val result = HashMap<String, Tenure>()

        for (issuer in issuers) {
            val maxTenure = issuer.tenures
                .filter { it.total_discount_amount?.value != null || it.total_subvention_amount?.value != null }
                .maxByOrNull {
                    it.total_discount_amount?.value ?: it.total_subvention_amount?.value ?: 0
                }

            if (maxTenure != null) {
                result[issuer.id] = maxTenure
            }
        }

        return result
    }


    fun List<Tenure>.markBestValueInPlace(): List<Tenure> {
        this
            .groupBy { it.emi_type }
            .forEach { (_, tenures) ->
                val bestTenure = tenures.maxByOrNull { tenure ->
                    (tenure.total_discount_amount?.value ?: tenure.total_subvention_amount?.value
                    ?: 0.0).toDouble()
                }
                if (bestTenure?.emi_type.equals(NO_COST_EMI, true)) {
                    bestTenure?.isRecommended = true
                } else if (bestTenure?.emi_type.equals(LOW_COST_EMI, true)) {
                    bestTenure?.isBestValue = true
                }
            }
        return this
    }

    fun handleCTAEnableDisable(context: Context, isEnabled: Boolean, button: Button) {
        button.isEnabled = isEnabled
        if (isEnabled) {
            button.setBackgroundResource(R.drawable.primary_button_background)
            button.setTextColor(
                AppCompatResources.getColorStateList(
                    context,
                    R.color.white
                )
            )
        } else {
            button.setBackgroundResource(R.drawable.primary_button_disabled_bg)
            button.setTextColor(
                AppCompatResources.getColorStateList(
                    context,
                    R.color.text_disabled_C0C9D2
                )
            )

        }


    }


}