package com.plural_pinelabs.expresscheckoutsdk.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class FetchResponseDTO(
    val transactionInfo: TransactionInfo? = null,
    val merchantInfo: MerchantInfo? = null,
    val paymentData: PaymentData? = null,
    val paymentModes: List<PaymentMode>? = null,
    var merchantBrandingData: BrandConfig? = null,
    var dccData: DccData? = null,
    var customerInfo: CustomerInfo?,
    val shippingAddress: Address,
    val billingAddress: Address,
    val cartDetails: CartDetails? = null,
    val convenienceFeesInfo: List<ConvenienceFeesInfo>? = null,
    val merchantMetadata: MerchantMetadata? = null,
)

@Parcelize
data class CartItem(
    val item_id: String? = null,
    val item_name: String? = null,
    val item_description: String? = null,
    val item_details_url: String? = null,
    val item_image_url: String? = null,
    val item_original_unit_price: Long? = null,
    val item_discounted_unit_price: Long? = null,
    val item_quantity: Int? = null,
    val item_currency: String? = null
) : Parcelable

@Parcelize
data class CartDetails(
    val cart_items: List<CartItem>? = null
) : Parcelable


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
    val id: String? = null,
    val customer_id: String? = null,
    val full_name: String? = null,
    val address1: String? = null,
    val address2: String? = null,
    val address3: String? = null,
    val pincode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val address_type: String? = null,
    val address_category: String? = null

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

@Parcelize
data class Logo(val imageSize: String, val imageContent: String) : Parcelable

@Parcelize
data class BrandTheme(val color: String) : Parcelable


data class RecyclerViewPaymentOptionData(
    val paymentImage: Int = -1,
    val paymentOption: Int = -1,
    val description: Int = -1,
)

@Parcelize
// New data class to hold customer information
data class CustomerInfo(
    val lastUsedPaymode: LastUsedPaymode? = null,
    val shipping_address: Address? = null,
    val customerId: String? = null,
    var customer_id: String? = null,
    val firstName: String? = null,
    var first_name: String? = null,
    val lastName: String? = null,
    var last_name: String? = null,
    val isEditCustomerDetailsAllowed: Boolean? = null,
    var is_edit_customer_details_allowed: Boolean? = null,
    var is_edit: Boolean? = null,
    var countryCode: String? = null,
    var country_code: String? = null,
    var mobileNo: String? = null,
    var mobile_number: String? = null,
    var mobileNumber: String? = null,
    var emailId: String? = null,
    var email_id: String? = null,
    val totalTokens: Int? = null,
    val tokens: List<SavedCardTokens>? = null,
    var shippingAddress: Address? = null,
    var billingAddress: Address? = null,
    val status: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    var customerToken: String? = null
) : Parcelable

data class CustomerInfoResponse(
    val status: String?,
    val customerInfo: CustomerInfo?,
    val customerToken: String? = null,
)

@Parcelize
data class ProcessPaymentRequest(
    val card_token_data: CardTokenData? = null,
    val customer_data: CustomerData? = null,
    val card_data: CardData? = null,
    val upi_data: UpiData? = null,
    val wallet_data: WalletData? = null,
    val netbanking_data: NetBankingData? = null,
    val extras: Extra? = null,
    val txn_data: UpiTransactionData? = null,
    val convenience_fee_data: ConvenienceFeesData? = null,
    val emi_data: EmiData? = null,
    val card_meta_data: CardMetaData? = null,
) : Parcelable

@Parcelize
data class EmiData(
    val offer_details: OfferDetails? = null,
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
    val customerInfo: CustomerInfo? = null,
    val mobileNo: String?,
    val emailId: String?
) : Parcelable


@Parcelize
data class CardData(
    val card_number: String?,
    val cvv: String?,
    val card_holder_name: String?,
    val card_expiry_year: String?,
    val card_expiry_month: String?,
    val isNativeOTPSupported: Boolean?,
    var save: Boolean?,
    val card_type: String? = null,
    val network_name: String? = null,
    val issuer_name: String? = null,
    val card_category: String? = null,
    val country_code: String? = null,
    val token_txn_type: String? = null,
    val last4_digit: String? = null,
    val is_native_otp_eligible: Boolean? = null,

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
    val sdk_data: SDKData? = null,
    val order_amount: Int? = null,
    val language: String? = null
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
    val deep_link: String?,
    val order_summary: OrderSummary?
)

@Parcelize
data class OrderSummary(
    val order_id: String,
    val merchant_order_reference: String,
    val type: String,
    val status: String,
    val callback_url: String,
    val merchant_id: String,
    val order_amount: Amount,
    val pre_auth: Boolean,
    val part_payment: Boolean,
    val allowed_payment_methods: List<String>,
    val purchase_details: PurchaseDetails,
    val payments: List<Payment>,
    val created_at: String,
    val updated_at: String,
    val integration_mode: String,
    val payment_retries_remaining: Int
) : Parcelable


@Parcelize
data class PurchaseDetails(
    val customer: CustomerInfo,
    val merchant_metadata: MerchantMetadata,
    val products: List<ProductDetail>
) : Parcelable

@Parcelize
data class MerchantMetadata(
    val key1: String,
    val key2: String,
    val express_checkout_enabled: String? = null,
    val express_checkout_allowed_action: String? = null


) : Parcelable


@Parcelize
data class Payment(
    val id: String,
    val merchant_payment_reference: String,
    val status: String,
    val payment_amount: Amount,
    val payment_method: String,
    val error_detail: ErrorDetail,
    val created_at: String,
    val updated_at: String,
    val challenge_url: String? = null,
    val payment_option: PaymentOptions? = null,
    val acquirer_data: AcquirerData? = null,
) : Parcelable

@Parcelize
data class AcquirerData(
    val approval_code: String,
    val acquirer_reference: String,
    val rrn: String,
    val is_aggregator: Boolean,
    val acquirer_name: String
) : Parcelable


@Parcelize
data class PaymentOptions(
    val card_data: CardData? = null,
) : Parcelable

@Parcelize
data class ErrorDetail(
    val code: String,
    val message: String
) : Parcelable


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

@Parcelize
data class OTPRequest(
    val payment_id: String? = null,
    var otp: String? = null,
    val customerId: String? = null,
    val otpId: String? = null,
    val updateOrderDetails: UpdateOrderDetails? = null
) : Parcelable

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
    val cardData: SavedCardDataObject,
    var cvvInput: String? = null,
) : Parcelable

@Parcelize
data class SavedCardDataObject(
    val last4Digit: String,
    val networkName: String,
    val issuerName: String,
    val cvvRequired: Boolean
) : Parcelable

@Parcelize
data class UpdateOrderDetails(
    val customer: CustomerInfo?
) : Parcelable

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


@Parcelize
data class ExpressAddress(
    val operationName: String? = null,
    val query: String,
    val variables: Variables,
) : Parcelable


@Parcelize
data class Variables(
    val customerToken: String? = null,
    val customerId: String? = null,
    val addresses: List<Address>? = null,
    val addressId: String? = null,
) : Parcelable


@Parcelize
data class ExpressAddressResponse(
    val data: Data
) : Parcelable


@Parcelize
data class Data(
    val getCustomerAddressesByMobile: CustomerAddresses
) : Parcelable

@Parcelize
data class CustomerAddresses(
    val success: Boolean,
    val message: String,
    val data: AddressData,
    val error: String? = null
) : Parcelable

@Parcelize
data class AddressData(
    val addresses: List<Address>
) : Parcelable


//EMI data classes

@Parcelize
data class EMIPaymentModeData(
    val issuers: List<Issuer>,
    val offerDetails: List<OfferDetail>? = null
) : Parcelable

@Parcelize
data class Issuer(
    val id: String,
    val name: String,
    val display_name: String,
    val issuer_type: String,
    val priority: Int,
    val tenures: List<Tenure>,
    val issuer_data: IssuerData? = null,
    var maxDiscountAmount: Int? = null,
) : Parcelable

@Parcelize
data class Tenure(
    val tenure_id: String,
    val name: String,
    val tenure_type: String,
    val tenure_value: Int,
    val issuer_offer_parameters: List<IssuerOfferParameter>? = null,
    val details: List<ProductDetail>? = null,
    val discount: Discount? = null,
    val loan_amount: Amount? = null,
    val total_discount_amount: Amount? = null,
    val net_payment_amount: Amount? = null,
    val monthly_emi_amount: Amount? = null,
    val total_emi_amount: Amount? = null,
    val interest_amount: Amount? = null,
    val interest_rate_percentage: Double? = null,
    val processing_fee_details: ProcessingFeeDetails? = null,
    val emi_type: String,
    val additionalCashback: String? = null,
    val label: String? = null,
    val total_subvention_amount: Amount? = null,
    val subvention: Subvention? = null,
    val convenience_fee_breakdown: ConvenienceFeeBreakdown? = null,
    var isBestValue: Boolean = false,
    var isRecommended: Boolean = false

) : Parcelable

@Parcelize
data class IssuerOfferParameter(
    val program_type: String,
    val offer_id: String,
    val offer_parameter_id: String
) : Parcelable

@Parcelize
data class ProductDetail(
    val product_code: String,
    val product_display_name: String,
    val brand_id: String,
    val brand_name: String,
    val product_amount: Amount? = null,
    val interest_amount: Amount? = null,
    val interest_rate: Double? = null,
    val discount: Discount? = null,
    val subvention: Subvention? = null,
    val product_offer_parameters: List<IssuerOfferParameter>? = null

) : Parcelable

@Parcelize
data class Discount(
    val discount_type: String,
    val percentage: Double,
    val amount: Amount? = null,
    val max_amount: Amount? = null
) : Parcelable

@Parcelize
data class Amount(
    val currency: String,
    val value: Int,
    val amount: Int,
) : Parcelable

@Parcelize
data class ProcessingFeeDetails(
    val percentage: Double? = null,
    val amount: Amount? = null
) : Parcelable

@Parcelize
data class IssuerData(
    val otp_length: Int,
    val otp_time_in_sec: Int,
    val otp_retry_count: Int,
    val is_consent_page_required: Boolean,
    val consent_data: String,
    val terms_and_conditions: String,
    val show_key_fact_statement: Boolean,
    val auth_type: String,
    val is_tokenized_transaction_supported: Boolean,
    val pan_number_last_digit_count: Int? = null,
    val penny_transaction_amount: Amount? = null

) : Parcelable

@Parcelize
data class OfferDetail(
    val issuerId: String,
    val name: String,
    val type: String,
    val tenureOffers: List<TenureOffer>?=null,
    val maxSaving: Int,
    val issuer: Issuer?=null,
    var isInstantSaving: Boolean = false,
    var offerTitle: String? = null,
) : Parcelable


@Parcelize
data class OfferDetails(
    val id: String?,
    val name: String?,
    val display_name: String?,
    val issuer_type: String?,
    val priority: Int?,
    val issuer_data: IssuerData?,
    val label: String?,
    val subventionType: String?,
    val isMultiCartEmi: Boolean?,
    val issuerName: String?,
    val isSplitEmi: Boolean?,
    val tenure: Tenure?,
    val tenures: List<Tenure>? = null,
) : Parcelable


@Parcelize
data class TenureOffer(
    val tenureId: String,
    val tenure: String,
    val offers: List<Offer>,
    val emiType: String,
    val discountAmount: Int,
    val cashbackAmount: Int,
    val offerLabel: String,
    val fullTenure: Tenure,
) : Parcelable

@Parcelize
data class Offer(
    val programType: String,
    val discount: DiscountValue
) : Parcelable

@Parcelize
data class DiscountValue(
    val type: String,
    val value: Int
) : Parcelable


@Parcelize
data class OfferEligibilityResponse(
    val code: String,
    val message: String
) : Parcelable


@Parcelize
data class Subvention(
    val subvention_type: String,
    val offer_type: String,
    val percentage: Double,
    val amount: Amount
) : Parcelable

@Parcelize
data class ConvenienceFeeBreakdown(
    val fee_calculated_on_amount: Amount? = null,
    val fee_amount: Amount? = null,
    val tax_amount: Amount? = null,
    val maximum_fee_amount: Amount? = null,
    val applicable_fee_amount: Amount? = null,
    val additional_fee_amount: Amount? = null,
) : Parcelable


@Parcelize
data class KFSResponse(
    val key_fact_pdf_url: String? = null
) : Parcelable

@Parcelize
data class ConvenienceFeesInfo(
    val paymentAmount: Amount? = null,
    val convenienceFeesAmount: Amount? = null,
    val convenienceFeesGSTAmount: Amount? = null,
    val convenienceFeesAdditionalAmount: Amount? = null,
    val convenienceFeesMaximumFeeAmount: Amount? = null,
    val convenienceFeesApplicableFeeAmount: Amount? = null,
    val originalTxnAmount: Amount? = null,
    val paymentModeType: String? = null,
    val networkType: String? = null,
    val cardType: String? = null,
    val feeType: String? = null
) : Parcelable


@Parcelize
data class CardMetaData(
    val scheme_name: String? = null,
    val card_type: String? = null,
) : Parcelable


@Parcelize
data class BrandConfig(
    val logo: Logo,
    val brandTheme: BrandTheme,
    val theme: String,
    val brandName: String,
    val font: String,
    val expressCheckoutSettings: ExpressCheckoutSettings
) : Parcelable

@Parcelize
data class ExpressCheckoutSettings(
    val checkoutCollectMobile: Boolean,
    val checkoutCollectAddress: Boolean,
    val prefillCustomerDetails: Boolean,
    val showRecommendations: Boolean,
    val showUpsell: Boolean,
    val showCrossSell: Boolean,
    val enableEDD: Boolean,
    val enableLogisticsPush: Boolean
) : Parcelable


@Parcelize
data class AddressResponse(
    val status: String,
    val message: String,
    val data: CustomerData
) : Parcelable

@Parcelize
data class CustomerInfoData(
    val customerInfo: CustomerInfo
) : Parcelable


@Parcelize
data class LastUsedPaymode(
    val upi: Upi,
    val card: Card
) : Parcelable

@Parcelize
data class Upi(
    val lastUsedVPAs: List<String>
) : Parcelable

@Parcelize
data class Card(
    val lastUsedCard: List<String>
) : Parcelable


@Parcelize
data class AddressRequest
    (
    val address: Address? = null,
) : Parcelable


