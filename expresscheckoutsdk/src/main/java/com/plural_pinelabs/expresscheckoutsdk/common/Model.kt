package com.plural_pinelabs.expresscheckoutsdk.common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Palette(
    @SerializedName("50") val C50: String, @SerializedName("100") val C100: String,
    @SerializedName("200") val C200: String, @SerializedName("300") val C300: String,
    @SerializedName("400") val C400: String, @SerializedName("500") val C500: String,
    @SerializedName("600") val C600: String, @SerializedName("700") val C700: String,
    @SerializedName("800") val C800: String, @SerializedName("900") var C900: String
) : Parcelable
