package com.plural_pinelabs.expresscheckoutsdk.common

enum class ErrorCode(val code: String) {
    INVALID_TOKEN("1001"),
    INVALID_REQUEST("1002"),
    INTERNAL_SERVER_ERROR("1003"),
    INTERNET_NOT_AVAILABLE("1004"),
    // Payment Gateway SDK related errors
    ORDER_PROCESSED("2001"), //  success code for order processing
    PAYMENT_FAILED("2002"), //  failure code for payment failure
    TRANSACTION_CANCELLED("2003"), //  code for transaction cancellation
    UNKNOWN_PAYMENT_ERROR("2004"), //  code for an unhandled payment error
    EXCEPTION_THROWN("2005"), //  code for an unhandled exception
}