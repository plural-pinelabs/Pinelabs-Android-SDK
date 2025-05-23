package com.plural_pinelabs.expresscheckoutsdk.common

internal object Constants {
    const val MOBILE_REGEX =
        "^[+]?\\d{1,4}[\\s-]?\\(?\\d{1,4}\\)?[\\s-]?\\d{1,4}[\\s-]?\\d{1,4}$"
    const val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    const val BASE_URL_UAT = "api-staging.pluralonline.com"
    const val BASE_URL_QA = "pluralqa.pinepg.in"
    const val BASE_URL_PROD = "api.pluralonline.com"
    const val BASE_ANIMATION = "https://d1xlp3rxzdtgvz.cloudfront.net/loaderAnimation/"
    const val BASE_IMAGES = "https://d1xlp3rxzdtgvz.cloudfront.net/bank-icons/bank-logos/"
    const val HTTPS = "https://"
    const val BASE_CHECKOUTBFF = "/api/v3/checkout-bff/"

    const val TIMEOUT: Long = 60

    const val CREDIT_DEBIT_ID = "CREDIT_DEBIT"
    const val PAY_BY_POINTS_ID = "PAYBYPOINTS"
    const val NET_BANKING_ID = "NET_BANKING"
    const val UPI_ID = "UPI"
    const val WALLET_ID = "WALLET"


    const val CARD_NUMBER_CHUNK = 4
    const val PAYMENT_REFERENCE_TYPE_CARD = "CARD"

    const val BROWSER_ACCEPT_ALL = "*/*"
    const val BROWSER_USER_AGENT_ANDROID =
        "Mozilla/5.0+(Macintosh;+Intel+Mac+OS+X+10_15_7)+AppleWebKit/537.36+(KHTML,+like+Gecko)+Chrome/133.0.0.0+Safari/537.36"
    const val BROWSER_DEVICE_CHANNEL = "browser"
    const val TRANSACTION_TYPE_SDK = "SDK"
    const val SDK_TYPE = "ANDROID"
    const val PLATFORM_TYPE = "NATIVE_MOBILE"
    const val OS = "Android"
    const val PLATFORM_VERSION = "v3"
    const val APP_VERSION = "1.0"

    const val IMAGE_LOGO = BASE_ANIMATION + "logo_shimmer.json"
    const val BFF_RESPONSE_HANDLER_ENDPOINT = "checkout-bff/responseHandler"
    const val ACS_JAVA_SCRIPT_INTERFACE = "AndroidInterface"



}