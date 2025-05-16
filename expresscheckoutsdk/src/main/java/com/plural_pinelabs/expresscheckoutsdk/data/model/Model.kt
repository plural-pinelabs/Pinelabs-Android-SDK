package com.plural_pinelabs.expresscheckoutsdk.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class FetchResponseDTO(
    val transactionInfo: TransactionInfo? = null,
    val merchantInfo: MerchantInfo? = null,
    val paymentData: PaymentData? = null,
    val paymentModes: List<PaymentMode>? = null,
    var merchantBrandingData: MerchantBranding? = null,
    var dccData: DccData? = null,
    val customerInfo: CustomerInfo?,  // New field for customer information
    val shippingAddress: Address,
    val billingAddress: Address
)

@Parcelize
data class DccData(
    val currencyMapper: Map<String, Currency?>? = null
) : Parcelable

@Parcelize
data class Currency(
    val symbol: String?, val flag: String?, val transformation_ratio: Int?
) : Parcelable

@Parcelize
data class Address(
    val address1: String?,
    val address2: String?,
    val pincode: String?,
    val city: String?,
    val state: String?,
    val country: String?
) : Parcelable

data class FetchError(
    val error_code: String,
    val error_message: String,
    val error_details: ErrorDetails?
)

data class ErrorDetails(val source: String, val error: ErrorDetailsError)

data class ErrorDetailsError(val code: String, val message: String, val next: List<String>)

data class FetchFailure(
    val status: String,
    val type: String,
    val message: String,
    val traceId: String
)

data class TransactionInfo(
    val orderId: String
)

data class MerchantInfo(
    val merchantId: Int,
    val merchantName: String,
    val merchantDisplayName: String?,
    val featureFlags: FeatureFlag?
)

data class FeatureFlag(
    var isSavedCardEnabled: Boolean?,
    var isNativeOTPEnabled: Boolean?,
    var isDCCEnabled: Boolean?
)

data class OrignalTransactionAmount(var amount: Int?, val currency: String)

data class PaymentData(var originalTxnAmount: OrignalTransactionAmount?)

data class PaymentMode(
    val paymentModeId: String,
    val paymentModeData: Any?/*, @SerializedName("paymentModeData")val wallet: List<Wallet>*/
)

data class PaymentModeData(
    val upi_flows: List<String>?,
    val IssersUIDataList: List<issuerDataList>?,
    val acquirerWisePaymentData: List<AcquirerWisePaymentData>?
)

data class AcquirerWisePaymentData(
    val acquirerId: String,
    val isNbbl: Boolean,
    val PaymentOption: List<PaymentOption>
)

data class PaymentOption(val payCode: String?, val Name: String?, val merchantPaymentCode: String?)


data class Wallet(
    val bankName: String?,
    val merchantPaymentCode: String?,
    val acquirer: String?
)

data class issuerDataList(val bankName: String?, val merchantPaymentCode: String?)

data class MerchantBranding(val logo: Logo?, val brandTheme: BrandTheme?, var palette: Palette?)

data class Logo(val imageSize: String, val imageContent: String)

data class BrandTheme(val color: String)


data class RecyclerViewPaymentOptionData(
    val payment_image: Int = -1,
    val payment_option: String = ""
)

@Parcelize
// New data class to hold customer information
data class CustomerInfo(
    val customerId: String?,
    val firstName: String?,
    val lastName: String,
    val isEditCustomerDetailsAllowed: Boolean?,
    var is_edit_customer_details_allowed: Boolean?,
    var is_edit: Boolean?,
    var countryCode: String?,
    var country_code: String?,
    var mobileNo: String?,
    var emailId: String?,
    val totalTokens: Int?,
    val tokens: List<SavedCardTokens>?,
    var shippingAddress: Address? = null,
    var billingAddress: Address? = null,
    val status: String?,
    val created_at: String?,
    val updated_at: String?
) : Parcelable

data class CustomerInfoResponse(
    val status: String?,
    val customerInfo: CustomerInfo?
)

@Parcelize
data class ProcessPaymentRequest(
    val card_token_data: CardTokenData?,
    val customer_data: CustomerData?,
    val card_data: CardData?,
    val upi_data: UpiData?,
    val wallet_data: WalletData?,
    val netbanking_data: NetBankingData?,
    val extras: Extra?,
    val txn_data: UpiTransactionData?,
    val convenience_fee_data: ConvenienceFeesData?,
) : Parcelable

@Parcelize
data class ConvenienceFeesData(
    val convenience_fees_amt_in_paise: Int,
    val convenience_fees_fees_gst_amt_in_paise: Int,
    val convenience_fees_fees_addition_amt_in_paise: Int,
    val final_amt_in_paise: Int,
    val transaction_amount: Int,
    val convenience_fees_maximum_fee_amount: Int,
    val convenience_fees_applicable_fee_amount: Int,
    val currency: String
) : Parcelable

@Parcelize
data class NetBankingData(
    val pay_code: String?
) : Parcelable

@Parcelize
data class WalletData(
    val wallet_code: String?
) : Parcelable

data class ProcessPaymentResponse(
    val redirect_url: String?,
    val response_code: String,
    val response_message: String,
    val pg_upi_unique_request_id: String?,
    val deep_link: String?,
    val payment_id: String?,
    val order_id: String?,
    val short_link: String?,
)

@Parcelize
data class CardTokenData(
    val token_id: String?,
    val cvv: String?
) : Parcelable

@Parcelize
data class CustomerData(
    val mobileNo: String?,
    val email_id: String?
) : Parcelable


@Parcelize
data class CardData(
    val card_number: String,
    val cvv: String,
    val card_holder_name: String,
    val card_expiry_year: String,
    val card_expiry_month: String,
    val isNativeOTPSupported: Boolean?,
    var save: Boolean?
) : Parcelable

@Parcelize
data class DeviceInfo(
    val device_type: String?,
    val browser_user_agent: String?,

    val browser_accept_header: String?,
    val browser_language: String?,
    val browser_screen_height: String?,
    val browser_screen_width: String?,
    val browser_timezone: String?,
    val browser_window_size: String?,
    val browser_screen_color_depth: String?,
    val browser_java_enabled_val: Boolean?,
    val browser_javascript_enabled_val: Boolean?,
    val device_channel: String?,
    val browser_ip_address: String?
) : Parcelable

@Parcelize
data class RiskValidationDetails(
    val first_name: String,
    val last_name: String,
    val email: String,
    val address_line1: String,
    val address_line2: String,
    val city: String,
    val state: String,
    val country: String,
    val zipCode: String
) : Parcelable

@Parcelize
data class Extra(
    val payment_mode: List<String>?,
    var payment_amount: Int?,
    var payment_currency: String?,
    val card_last4: String?,
    val redeemable_amount: Int?,
    val registered_mobile_number: String?,
    val txn_mode: String?,
    val device_info: DeviceInfo?,
    var risk_validation_details: RiskValidationDetails?,
    var dcc_status: String? = null,
    val sdk_data: SDKData? = null
) : Parcelable

data class PBPBank(
    val bankName: String,
    val bankLogo: String
)

@Parcelize
data class UpiData(
    val upi_option: String,
    val vpa: String?,
    val txn_mode: String?
) : Parcelable

@Parcelize
data class UpiTransactionData(
    val SelectedPaymentModeId: Int
) : Parcelable

data class RewardRequest(
    val payment_method: String,
    val payment_option: RewardPaymentOption,
    val order_details: OrderDetails
)

data class RewardPaymentOption(
    val points_card_details: RewardPointsCardDetails
)

data class RewardPointsCardDetails(
    val card_last4: String,
    val card_number: String,
    val registered_mobile_number: String?
)

data class OrderDetails(
    val order_amount: OrderDetailsAmount
)

data class OrderDetailsAmount(
    val value: Int,
    val currency: String
)

data class RewardResponse(
    val payment_method: String,
    val is_eligible: Boolean,
    val payment_option_metadata: PaymentOptionMetaData,
    val redeemable_amount: OrderDetailsAmount,
    val balance: OrderDetailsAmount
)

data class PaymentOptionMetaData(
    val pay_by_point_option_data: PBPOptionData
)

data class PBPOptionData(
    var redeemable_points: Int
)

data class TransactionStatusResponse(
    val data: TransactionStatus
)

data class TransactionStatus(
    val order_id: String,
    var status: String,
    val is_retry_available: Boolean,
    val response_code: Int?,
    val response_message: String?,
    val payment_id: String?,
    val deep_link: String?
)

data class CancelTransactionResponse(
    val response_code: Int,
    val response_message: String
)

data class CancelResponseData(
    val order_id: String,
    val status: String,
    val signature: String
)

data class BinResponse(val GlobalBinsData: List<GlobalBinsData>, val resultInfo: ResultInfo)

data class GlobalBinsData(val issuerName: String, val cardType: String, val isDomesticCard: Boolean)

data class ResultInfo(val responseCode: String, val totalBins: String)

data class OTPRequest(
    val payment_id: String? = null,
    var otp: String? = null,
    val customerId: String? = null,
    val otpId: String? = null,
    val updateOrderDetails: UpdateOrderDetails? = null
)

data class OTPResponse(val next: List<String>?, val status: String?, val meta_data: MetaData?)

data class SavedCardResponse(val otpId: String?, val status: String?, val otpAttemptLeft: Int)

data class MetaData(val resend_after: String?)

data class CardBinMetaDataRequestList(
    var amount: Int? = null, var dcc_details_required: Boolean? = null,
    var markup_required: Boolean? = null, var card_details: List<CardBinMetaDataRequest>?
)

data class CardBinMetaDataRequest(
    val payment_identifier: String,
    val payment_reference_type: String
)

data class CardBinMetaDataResponse(val card_payment_details: List<CardBinMetaDataResponseData>)

@Parcelize
data class CardBinMetaDataResponseData(
    val payment_identifier: String,
    val payment_reference_type: String,
    val card_network: String,
    val card_issuer: String,
    val card_type: String,
    val card_category: String,
    var is_native_otp_supported: Boolean,
    val is_international_card: Boolean,
    val country_code: String,
    val currency: String,
    var is_currency_supported: Boolean,
    val converted_amount: Int,
    val conversion_rate: Double,
    val markup: Int
) : Parcelable

data class SavedCardData(val icon: Int, val text: String)

@Parcelize
data class SavedCardTokens(
    val tokenId: String,
    val expiredAt: String,
    val cardData: SavedCardDataObject
) : Parcelable

@Parcelize
data class SavedCardDataObject(
    val last4Digit: String,
    val networkName: String,
    val issuerName: String,
    val cvvRequired: Boolean
) : Parcelable

data class UpdateOrderDetails(
    val customer: CustomerInfo?
)

@Parcelize
data class SDKData(
    val transaction_type: String?,
    val sdk_type: String?,
    val sdk_version: String?,
    val app_version: String?,
    val app_id: String?,
    val device_model: String?,
    val device_id: String?,
    val platform_type: String?,
    val operating_system: String?,
    val operating_system_version: String?,
    /*val browser_name: String?,
    val browser_version: String?,*/
    val timestamp: String?,
    val version: String
) : Parcelable


sealed class PaymentModeDataType {
    data class Data(val data: PaymentModeData) : PaymentModeDataType()
    data class Wallets(val wallets: List<Wallet>) : PaymentModeDataType()
}

data class NetBank(
    var bankCode: String?,
    var bankName: String?,
    var bankImage: String,
    var isNBBLBank: Boolean = false
)

data class WalletBank(var bankCode: String?, var bankName: String?, var bankImage: Int)

@Parcelize
data class DCCDetails(
    var nativeCurrencyAmount: Int?,
    var foreginCurrency: String?,
    var foreginCurrencyAmount: Int?,
    var foreginCurrencyLabel: String?,
    var transformationRatio: Int?,
    var merchantName: String?,
    var conversionRate: Double?
) : Parcelable

@Parcelize
data class Palette(
    @SerializedName("50") val C50: String, @SerializedName("100") val C100: String,
    @SerializedName("200") val C200: String, @SerializedName("300") val C300: String,
    @SerializedName("400") val C400: String, @SerializedName("500") val C500: String,
    @SerializedName("600") val C600: String, @SerializedName("700") val C700: String,
    @SerializedName("800") val C800: String, @SerializedName("900") var C900: String
) : Parcelable
