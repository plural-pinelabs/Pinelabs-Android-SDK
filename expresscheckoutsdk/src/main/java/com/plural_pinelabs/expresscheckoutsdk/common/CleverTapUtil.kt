package com.plural_pinelabs.expresscheckoutsdk.common

import android.content.Context
import com.clevertap.android.sdk.CleverTapAPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.APP_VERSION
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_ADDRESS_CHANGED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_ADDRESS_ENTERED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_CARD_DETAILS_ENTERED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_CHECKOUT_ABANDONED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_CHECKOUT_CONTINUE_CLICKED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_CHECKOUT_RENDERED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_INITIALISED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_MOBILE_CHANGED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_MOBILE_ENTERED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_OFFER_APPLIED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_OTP_ENTERED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_PAYMENT_MODE_SELECTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_PAYMENT_MODE_VIEWED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_TRANSACTION_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EVENT_SDK_TRANSACTION_SUCCESSFUL
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_ABANDON_TIME
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_ADDRESS_FILL_TIME
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_ADDRESS_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_AVAILABLE_MODES
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_BANK_NAME
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CARD_BIN
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CARD_ITEM
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CARD_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CART_VALUE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CHANGE_REASON
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CHANGE_TIME
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CHECKOUT_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CITY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CONVERSION_TIME
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CTA_TEXT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CUSTOMER_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_CVV_VALIDATED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_DEVICE_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_DEVICE_INFO
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_DISCOUNT_AMOUNT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_DISCOUNT_APPLIED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_ENTRY_METHOD
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_ERROR_CODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_FAILURE_ORDER_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_FAILURE_REASON
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_FAILURE_STEP
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_FIELD_CHANGED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_INTEREST_RATE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_ISSUER
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_IS_AUTO_APPLIED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_IS_COD
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_IS_PERSONALIZED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_IS_RECOMMENDED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_IS_RETRY_AVAILABLE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_IS_VALIDATED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_LAST_STEP
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_LAST_VIEWED_MODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_MOBILE_LENGTH
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_MOBILE_PREFIX
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_MODE_TRIGGER
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_MONTHLY_EMI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_OFFERS_SELECTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_OFFER_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_OFFER_LABEL
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_OFFER_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_OTP_ENTERED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_OTP_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_OTP_SUCCESS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_PAYMENT_MODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_PAYMENT_MODE_SORTING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_PINCODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_PLATFORM
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_RECOMMENDED_MODE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_RESEND_OTP_CLICKED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_SDK_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_SDK_VERSION
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_SESSION_DURATION
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_STATE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_SUBVENTION_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_SUCCESS_ORDER_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_TENURE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_TXN_AMOUNT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_VALIDATION_PASSED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_VALIDATION_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_VPA_VALUE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_WAS_AUTOFILLED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PARAM_WAS_SAVED_CARD
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.SDK_TYPE_PLATRFORM_ANDROID
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO

object CleverTapUtil {

    fun getInstance(context: Context): CleverTapAPI? {
        return try {
            CleverTapAPI.getDefaultInstance(context)
        } catch (e: Exception) {
            null
        }
    }

    fun updateCleverTapUserProfile(context: Context, fetchResponse: FetchResponseDTO?) {
        try {
            val profileUpdate = HashMap<String, Any?>()
            val merchantInfo = fetchResponse?.merchantInfo
            val customerInfoData = fetchResponse?.customerInfo
            profileUpdate["Name"] =
                fetchResponse?.transactionInfo?.orderId ?: ""
            profileUpdate["Identity"] =
                merchantInfo?.merchantDisplayName + merchantInfo?.merchantId ?: ""
            profileUpdate["Email"] = customerInfoData?.emailId ?: ""
            profileUpdate["Phone"] = customerInfoData?.mobileNo ?: ""
            profileUpdate["Customer ID"] = customerInfoData?.customerId ?: ""
            val clevertapDefaultInstance: CleverTapAPI?? = getInstance(context = context)
            clevertapDefaultInstance?.pushProfile(profileUpdate)

        } catch (_: Exception) {
        }
    }

    fun sdkInitialized(cleverTapAPI: CleverTapAPI?, context: Context) {
        val sdkInitializedData = mapOf(
            PARAM_SDK_TYPE to SDK_TYPE_PLATRFORM_ANDROID,
            PARAM_PLATFORM to SDK_TYPE_PLATRFORM_ANDROID,
            PARAM_SDK_VERSION to APP_VERSION,
            PARAM_DEVICE_ID to Utils.getDeviceId(context)
        )
        cleverTapAPI?.pushEvent(EVENT_SDK_INITIALISED, sdkInitializedData)
    }


    fun sdkCheckoutRendered(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?
    ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_PLATFORM to SDK_TYPE_PLATRFORM_ANDROID,
                PARAM_CART_VALUE to fetchResponse.paymentData?.originalTxnAmount?.amount,
                PARAM_CARD_ITEM to fetchResponse.cartDetails?.cart_items?.size,
                PARAM_CHECKOUT_TYPE to "D2C"
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_CHECKOUT_RENDERED, sdkCheckoutRenderedData)
        }

    }


    fun sdkMobileEntered(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        validationPassed: Boolean,
        isPaste: Boolean
    ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_MOBILE_PREFIX to "+91",
                PARAM_MOBILE_LENGTH to "10",
                PARAM_VALIDATION_PASSED to validationPassed,
                PARAM_ENTRY_METHOD to if (isPaste) "Autofill" else "type"
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_MOBILE_ENTERED, sdkCheckoutRenderedData)
        }
    }

    fun sdkOTPEntered(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        otpEntered: String,
        isResendClicked: Boolean,
        isOTPFailed: Boolean,
        isOTPSuccess: Boolean,
    ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_OTP_ENTERED to otpEntered,
                PARAM_RESEND_OTP_CLICKED to isResendClicked,
                PARAM_OTP_FAILED to isOTPFailed,
                PARAM_OTP_SUCCESS to isOTPSuccess
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_OTP_ENTERED, sdkCheckoutRenderedData)
        }
    }

    fun sdkMobileChanged(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        validationPassed: Boolean,
        isPaste: Boolean
    ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_MOBILE_PREFIX to "+91",
                PARAM_MOBILE_LENGTH to "10",
                PARAM_VALIDATION_PASSED to validationPassed,
                PARAM_ENTRY_METHOD to if (isPaste) "Autofill" else "type"
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_MOBILE_CHANGED, sdkCheckoutRenderedData)
        }
    }


    fun sdkAddressEntered(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        isPaste: Boolean,
        pincode: String?,
        city: String?,
        state: String?
    ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_ADDRESS_TYPE to if (isPaste) "auto-filled" else "Manual",
                PARAM_PINCODE to pincode,
                PARAM_CITY to city,
                PARAM_STATE to state,
                PARAM_ADDRESS_FILL_TIME to -1
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_ADDRESS_ENTERED, sdkCheckoutRenderedData)
        }
    }

    fun sdkAddressChanged(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        isPaste: Boolean,
        fieldChanged: Array<String>
    ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_CHANGE_REASON to "manual",
                PARAM_WAS_AUTOFILLED to isPaste,
                PARAM_FIELD_CHANGED to fieldChanged,
                PARAM_CHANGE_TIME to -1
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_ADDRESS_CHANGED, sdkCheckoutRenderedData)
        }
    }


    fun sdkPaymentModeView(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        payModes: String,
        recommendedMode: String,
        isPersonalized: Boolean,

        ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_AVAILABLE_MODES to payModes,
                PARAM_RECOMMENDED_MODE to recommendedMode,
                PARAM_PAYMENT_MODE_SORTING to "default",
                PARAM_IS_PERSONALIZED to isPersonalized
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_PAYMENT_MODE_VIEWED, sdkCheckoutRenderedData)
        }
    }


    fun sdkPaymentModeSelected(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        payModes: String,
        isRecommended: Boolean,
        issuer: String?,
        cardType: String?,
        bankName: String?,
        savedCard: Boolean?,

        ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_PAYMENT_MODE to payModes,
                PARAM_IS_RECOMMENDED to isRecommended,
                PARAM_ISSUER to issuer,
                PARAM_CARD_TYPE to cardType,
                PARAM_BANK_NAME to bankName,
                PARAM_WAS_SAVED_CARD to savedCard,
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_PAYMENT_MODE_SELECTED, sdkCheckoutRenderedData)
        }
    }


    fun sdkOfferApplied(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        offerId: String,
        offerType: String,
        discountAmount: String,
        isAutoApplied: Boolean,
        offerLabel: String,
        modeTrigger: String,

        ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_OFFER_ID to offerId,
                PARAM_OFFER_TYPE to offerType,
                PARAM_DISCOUNT_AMOUNT to discountAmount,
                PARAM_IS_AUTO_APPLIED to isAutoApplied,
                PARAM_OFFER_LABEL to offerLabel,
                PARAM_MODE_TRIGGER to modeTrigger,
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_OFFER_APPLIED, sdkCheckoutRenderedData)
        }
    }

    fun sdkCheckoutContinueClicked(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        paymentMode: String,
        cartValue: String,
        offerSelected: String,
        ctaText: String,

        ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_PAYMENT_MODE to paymentMode,
                PARAM_CART_VALUE to cartValue,
                PARAM_OFFERS_SELECTED to offerSelected,
                PARAM_CTA_TEXT to ctaText,
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_CHECKOUT_CONTINUE_CLICKED, sdkCheckoutRenderedData)
        }
    }

    fun sdkTransactionSuccessful(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        paymentMode: String,
        cartValue: String,
        issuer: String,
        txnAmount: String,
        discountApplied: String,
        conversionTime: String,
        isCod: Boolean,

        ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_SUCCESS_ORDER_ID to fetchResponse.transactionInfo?.orderId,
                PARAM_PAYMENT_MODE to paymentMode,
                PARAM_ISSUER to issuer,
                PARAM_TXN_AMOUNT to txnAmount,
                PARAM_CART_VALUE to cartValue,
                PARAM_DISCOUNT_APPLIED to discountApplied,
                PARAM_CONVERSION_TIME to conversionTime,
                PARAM_IS_COD to isCod,
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_TRANSACTION_SUCCESSFUL, sdkCheckoutRenderedData)
        }
    }


    fun sdkTransactionFailed(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        paymentMode: String,
        issuer: String,
        failureReason: String,
        errorCode: String,
        failureStep: String,
        isRetryAvailable: Boolean,
        deviceInfo: String


    ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_FAILURE_ORDER_ID to fetchResponse.transactionInfo?.orderId,
                PARAM_PAYMENT_MODE to paymentMode,
                PARAM_FAILURE_REASON to failureReason,
                PARAM_ERROR_CODE to errorCode,
                PARAM_ISSUER to issuer,
                PARAM_FAILURE_STEP to failureStep,
                PARAM_IS_RETRY_AVAILABLE to isRetryAvailable,
                PARAM_DEVICE_INFO to deviceInfo,
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_TRANSACTION_FAILED, sdkCheckoutRenderedData)
        }
    }


    fun sdkTransactionAbandoned(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        abandonTime: String,
        sessionDuration: String,
        lastViewedMode: String,
        deviceInfo: String,
        lastStep: String,


        ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_CART_VALUE to fetchResponse.paymentData?.originalTxnAmount?.amount,
                PARAM_LAST_STEP to lastStep,
                PARAM_ABANDON_TIME to abandonTime,
                PARAM_SESSION_DURATION to sessionDuration,
                PARAM_DEVICE_INFO to deviceInfo,
                PARAM_LAST_VIEWED_MODE to lastViewedMode,
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_CHECKOUT_ABANDONED, sdkCheckoutRenderedData)
        }
    }


    fun cardNumberEntered(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        cardType: String,
        cardBin: String,
        isValidated: Boolean,

        ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_CART_VALUE to fetchResponse.paymentData?.originalTxnAmount?.amount,
                PARAM_CARD_BIN to cardBin,
                PARAM_CARD_TYPE to cardType,
                PARAM_IS_VALIDATED to isValidated,
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_CARD_DETAILS_ENTERED, sdkCheckoutRenderedData)
        }
    }


    fun cvvEntered(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        issuer: String,
        isCVVValidated: Boolean,

        ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_CART_VALUE to fetchResponse.paymentData?.originalTxnAmount?.amount,
                PARAM_ISSUER to issuer,
                PARAM_CVV_VALIDATED to isCVVValidated,
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_CARD_DETAILS_ENTERED, sdkCheckoutRenderedData)
        }
    }


    fun upiVPAEntered(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        vpaValue: String,
        validationStatus: Boolean,
        isSaved: Boolean,

        ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_CART_VALUE to fetchResponse.paymentData?.originalTxnAmount?.amount,
                PARAM_VPA_VALUE to vpaValue,
                PARAM_VALIDATION_STATUS to validationStatus,
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_CARD_DETAILS_ENTERED, sdkCheckoutRenderedData)
        }
    }


    fun emiOptionSelected(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        tenure: String,
        interestRate: String,
        subventionType: String,
        monthlyEmi: String,
        isSplitEMI: Boolean,

        ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCheckoutRenderedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_CART_VALUE to fetchResponse.paymentData?.originalTxnAmount?.amount,
                PARAM_TENURE to tenure,
                PARAM_INTEREST_RATE to interestRate,
                PARAM_SUBVENTION_TYPE to subventionType,
                PARAM_MONTHLY_EMI to monthlyEmi,
            )
            cleverTapAPI?.pushEvent(EVENT_SDK_CARD_DETAILS_ENTERED, sdkCheckoutRenderedData)
        }
    }

    fun sdkCrashed(
        cleverTapAPI: CleverTapAPI?,
        fetchResponse: FetchResponseDTO?,
        errorCode: String,
        errorMessage: String,
        stackTrace: String
    ) {
        fetchResponse?.let {
            val customerInfoData = fetchResponse.customerInfo
            val sdkCrashedData: Map<String, Any?> = mapOf(
                PARAM_CUSTOMER_ID to customerInfoData?.customer_id,
                PARAM_ERROR_CODE to errorCode,
                "Error Message" to errorMessage,
                "Stack Trace" to stackTrace
            )
            cleverTapAPI?.pushEvent("SDK_Crashed", sdkCrashedData)
        }
    }
}