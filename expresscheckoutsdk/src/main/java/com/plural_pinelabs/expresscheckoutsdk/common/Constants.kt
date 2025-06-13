package com.plural_pinelabs.expresscheckoutsdk.common

internal object Constants {
    const val MOBILE_REGEX =
        "^[+]?\\d{1,4}[\\s-]?\\(?\\d{1,4}\\)?[\\s-]?\\d{1,4}[\\s-]?\\d{1,4}$"
    const val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    const val BASE_URL_UAT = "api-staging.pluralonline.com"
    const val BASE_URL_QA = "pluralqa.pinepg.in"
    const val BASE_URL_EXPRESS_DEV = "nxt-express-checkout-svc-dev.v2.pinepg.in"
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

    //UPI
    const val UPI_INTENT_PREFIX = "upi://pay"
    const val GPAY = "com.google.android.apps.nbu.paisa.user"
    const val PHONEPE = "com.phonepe.app"
    const val PAYTM = "net.one97.paytm"
    const val BHIM_UPI = "in.org.npci.upiapp"
    const val CRED_UPI = "com.dreamplug.androidapp"
    const val UPI_INTENT = "INTENT"
    const val UPI_COLLECT = "COLELCT"
    const val UPI_TRANSACTION_STATUS_INTERVAL = 5000L


    const val PROCESSED_STATUS = "PROCESSED"
    const val PROCESSED_PENDING = "PENDING"
    const val PROCESSED_FAILED = "FAILED"
    const val PROCESSED_ATTEMPTED = "ATTEMPTED"

    const val REQ_USER_CONSENT = 200

    const val ERROR_KEY="Error"
    const val ERROR_MESSAGE_KEY="ErrorMessage"


}