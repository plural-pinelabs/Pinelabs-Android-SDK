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
}