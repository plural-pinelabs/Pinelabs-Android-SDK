package com.plural_pinelabs.expresscheckoutsdk.presentation

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.CleverTapAPI
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.SDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.CleverTapUtil
import com.plural_pinelabs.expresscheckoutsdk.common.CustomExceptionHandler
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.MTAG
import com.plural_pinelabs.expresscheckoutsdk.data.model.ConvenienceFeesInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.LogRequest
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.RetrofitBuilder
import com.plural_pinelabs.expresscheckoutsdk.logger.SdkLogger
import com.plural_pinelabs.expresscheckoutsdk.presentation.ordersummary.TopSheetDialogFragment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class LandingActivity : AppCompatActivity() {
    private lateinit var merchantLogoCard: CardView
    private lateinit var merchantLogo: ImageView
    private lateinit var merchantName: TextView
    private lateinit var cancelBtn: ImageView
    private lateinit var originalAmount: TextView
    private lateinit var strikeAmount: TextView
    private lateinit var orderSummary: TextView
    private lateinit var contactDetailsLayout: LinearLayout
    private lateinit var customerId: TextView
    private lateinit var customerEmail: TextView
    private lateinit var separator: TextView
    private lateinit var customerInfoData: CustomerInfo
    private lateinit var convenienceFessMessage: TextView
    private lateinit var mainContentLayout: ConstraintLayout


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
          SdkLogger.log(
            this,
            "SDK_LAUNCH",
            "SDK Launched",
            "",
            "INFO",
            "SDK"
        )
        setContentView(R.layout.activity_landing)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Required to change the background color of the screen
        window.decorView.setBackgroundColor(getResources().getColor(R.color.screen_background));
        setView()
        initExceptionHandler()
        ActivityLifecycleCallback.register(this.application)
        var cleverTapDefaultInstance: CleverTapAPI? =
            CleverTapAPI.getDefaultInstance(applicationContext)
    }

    private fun initExceptionHandler() {
        val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
        if (currentHandler !is CustomExceptionHandler) {
            Thread.setDefaultUncaughtExceptionHandler(
                CustomExceptionHandler(applicationContext, currentHandler)
            )
        }
    }


    fun showHideHeaderLayout(isShow: Boolean) {
        val headerLayout = findViewById<View>(R.id.header_layout)
        SdkLogger.log(
            this,
            "HEADER_VISIBILITY",
            "Header visibility changed: $isShow",
            ExpressSDKObject.getFetchData()?.transactionInfo?.orderId ?: "",
            "INFO",
            "SDK"
        )
        headerLayout.visibility = if (!isShow) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun setView() {
        merchantLogoCard = findViewById(R.id.merchant_logo_cardview)
        merchantLogo = findViewById(R.id.merchant_logo_img)
        merchantName = findViewById(R.id.merchant_name_title)
        cancelBtn = findViewById(R.id.cancel_btn_process)
        originalAmount = findViewById(R.id.txt_amount)
        strikeAmount = findViewById(R.id.amount_strike)
        orderSummary = findViewById(R.id.order_summary)
        contactDetailsLayout = findViewById(R.id.contact_details_layout)
        customerId = findViewById(R.id.txt_customer_id)
        customerEmail = findViewById(R.id.txt_customer_email)
        separator = findViewById(R.id.seperator)
        convenienceFessMessage = findViewById(R.id.convenience_fees_message)
        showHideHeaderLayout(false)
        mainContentLayout = findViewById(R.id.main)
        cancelBtn.setOnClickListener {
            SdkLogger.log(
                this,
                "PAYMENT_CANCEL_INITIATED",
                "User initiated payment cancellation",
                ExpressSDKObject.getFetchData()?.transactionInfo?.orderId ?: "",
                "INFO",
                "SDK"
            )
            Utils.showCancelPaymentDialog(this, object : ItemClickListener<Boolean> {
                override fun onItemClick(position: Int, item: Boolean) {
                    if (item) {
                        try {
                            runBlocking {
                                withTimeout(3000) { // Optional: timeout to avoid hanging
                                    val repo = ExpressRepositoryImpl(
                                        RetrofitBuilder.commonApiService,
                                        NetworkHelper(applicationContext)
                                    )
                                    val result = repo.cancelPayment(
                                        ExpressSDKObject.getToken(), ExpressSDKObject.getProcessPaymentResponse()!=null
                                    )
                                    result.collect {
                                        when (it) {
                                            is BaseResult.Success -> {

                                            }

                                            is BaseResult.Error -> {
                                                Log.e(
                                                    "ExpressLibrary",
                                                    "Failed to report crash logs: ${it.errorDescription}"
                                                )
                                            }

                                            is BaseResult.Loading -> {
                                                // No action needed for loading state here
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception){
                            Log.i("PineLabs error", "Error cancelling the transaction")
                        }
                        finally {
                            SdkLogger.log(
                                this@LandingActivity,
                                "PAYMENT_CANCELLED",
                                "Payment cancelled by user",
                                ExpressSDKObject.getFetchData()?.transactionInfo?.orderId ?: "",
                                "INFO",
                                "SDK"
                            )
                            CleverTapUtil.sdkTransactionAbandoned(
                                CleverTapUtil.getInstance(applicationContext),
                                ExpressSDKObject.getFetchData(),
                                System.currentTimeMillis().toString(),
                                "",
                                "",
                                Utils.createSDKData(applicationContext).toString(),
                                ""
                            )

                            try {
                                runBlocking {
                                    withTimeout(3000) { // Optional: timeout to avoid hanging
                                        val repo = ExpressRepositoryImpl(
                                            RetrofitBuilder.fetchApiService,
                                            NetworkHelper(applicationContext)
                                        )
                                        val logs = Utils.getUnSyncedErrors(applicationContext)
                                        val result = repo.logData(
                                            ExpressSDKObject.getToken(), logs
                                        )
                                        result.collect {
                                            when (it) {
                                                is BaseResult.Success -> {
                                                    if (it.data.status.equals(
                                                            "success",
                                                            ignoreCase = true
                                                        )
                                                    )
                                                        Utils.clearLogs(applicationContext)
                                                    Log.i(
                                                        "ExpressLibrary",
                                                        "Crash logs reported successfully"
                                                    )
                                                }

                                                is BaseResult.Error -> {
                                                    Log.e(
                                                        "ExpressLibrary",
                                                        "Failed to report crash logs: ${it.errorDescription}"
                                                    )
                                                }

                                                is BaseResult.Loading -> {
                                                    // No action needed for loading state here
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("ExpressLibrary", "Failed to report crash", e)
                            } finally {

                                val bundle = Bundle().apply {
                                    putBoolean("isCancelled", true) // Your boolean value
                                }

                                val navHostController =
                                    findNavController(R.id.nav_host_fragment_container)
                                navHostController.navigate(R.id.failureFragment,bundle)
                                // If you want to let the app crash after logging:
                            }

                        }
                    }
                }

            })
        }

        orderSummary.setOnClickListener {
            SdkLogger.log(
                this,
                "ORDER_SUMMARY_CLICKED",
                "User clicked on Order Summary",
                ExpressSDKObject.getFetchData()?.transactionInfo?.orderId ?: "",
                "INFO",
                "SDK"
            )
            val topFragment = TopSheetDialogFragment()
            topFragment.show(supportFragmentManager, "TopSheetDialogFragment")
        }
    }


    fun updateValueForHeaderLayout(fetchResponse: FetchResponseDTO?) {
        fetchResponse?.let { fetchData ->
            CleverTapUtil.updateCleverTapUserProfile(applicationContext, fetchData)
            showHideHeaderLayout(true)
            fetchData.customerInfo?.let { customerInfo ->
                strikeAmount.visibility = View.GONE
                if (!customerInfo.mobileNo.isNullOrEmpty() || !customerInfo.emailId.isNullOrEmpty()) {
                    contactDetailsLayout.visibility = View.VISIBLE
                    customerId.text = customerInfo.mobileNo ?: ""
                    customerEmail.text = customerInfo.emailId ?: ""
                    if (!customerInfo.emailId.isNullOrEmpty() && !customerInfo.mobileNo.isNullOrEmpty()) {
                        separator.visibility = View.VISIBLE
                    } else {
                        separator.visibility = View.GONE
                    }

                } else {
                    contactDetailsLayout.visibility = View.GONE
                    customerId.text = ""
                    customerEmail.text = ""
                    separator.visibility = View.GONE
                }
                if (customerInfo.isEditCustomerDetailsAllowed != null && customerInfo.isEditCustomerDetailsAllowed) {
                    customerInfoData = customerInfo
                    customerInfoData.customer_id = customerInfo.customerId
                    customerInfoData.billingAddress = fetchData.billingAddress
                    customerInfoData.shippingAddress = fetchData.shippingAddress
                }

                fetchData.merchantBrandingData?.logo?.imageContent?.let {
                    if (it.isNotEmpty()) {
                        try {
                            val content = it.split(",")[1]
                            val bitmap = Utils.decodeBase64ToBitmap(content)
                            merchantLogo.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        merchantLogoCard.visibility = View.GONE
                        merchantLogo.visibility = View.GONE
                    }
                } ?: run {
                    merchantLogoCard.visibility = View.GONE
                    merchantLogo.visibility = View.GONE
                }

                merchantName.text = fetchData.merchantInfo?.merchantDisplayName
                val amount = ExpressSDKObject.getAmount()
                val amountString = Utils.convertToRupeesWithSymobl(this, amount)

                val spannable: Spannable = SpannableString(amountString)
                val end = amountString.length
                val start = end - (amountString.split(".")[1]).length

                spannable.setSpan(
                    ForegroundColorSpan(resources.getColor(R.color.grey_99FFFFFF)),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                originalAmount.setText(spannable, TextView.BufferType.SPANNABLE)
            }
            if (!fetchResponse.convenienceFeesInfo.isNullOrEmpty()) {
                SdkLogger.log(
                    this,
                    "CONVENIENCE_FEE_INFO",
                    "Convenience fee info available",
                    ExpressSDKObject.getFetchData()?.transactionInfo?.orderId ?: "",
                    "INFO",
                    "SDK"
                )
                showHideConvenienceFessMessage(true)
            }
        }
    }

    fun showHideConvenienceFessMessage(
        isShow: Boolean,
        convenienceFeesInfo: ConvenienceFeesInfo? = null,
        showDefaultCardMessage: Boolean = false,
        cardNetwork: String? = null,

        ) {
        val amount = convenienceFeesInfo?.convenienceFeesApplicableFeeAmount?.amount
            ?: convenienceFeesInfo?.paymentAmount?.value ?: -1
        val payableAmount =
            convenienceFeesInfo?.paymentAmount?.amount ?: convenienceFeesInfo?.paymentAmount?.value
            ?: -1
        if (payableAmount > 0) {
            ExpressSDKObject.setPayableAmount(payableAmount)
            updateOrderAmount(payableAmount)
        } else {
            ExpressSDKObject.setPayableAmount(ExpressSDKObject.getOriginalOrderAmount())
            updateOrderAmount(ExpressSDKObject.getOriginalOrderAmount())
        }
        ExpressSDKObject.setConvenienceFee(
            (convenienceFeesInfo?.convenienceFeesAmount?.amount
                ?: 0) + (convenienceFeesInfo?.convenienceFeesAdditionalAmount?.amount ?: 0)
        )
        ExpressSDKObject.setConvenienceFeeGst(convenienceFeesInfo?.convenienceFeesGSTAmount?.amount)
        convenienceFessMessage.visibility = if (isShow) View.VISIBLE else View.GONE
        convenienceFessMessage.text = if (amount > 0) {
            when (convenienceFeesInfo?.paymentModeType) {
                PaymentModes.CREDIT_DEBIT.paymentModeID -> {
                    getString(
                        R.string.convenience_fees_x_added_for_x_card_payments,
                        Utils.convertToRupeesWithSymobl(this, amount),
                        cardNetwork ?: ""
                    )
                }

                PaymentModes.UPI.paymentModeID -> {
                    getString(
                        R.string.convenience_fees_x_added_upi,
                        Utils.convertToRupeesWithSymobl(this, amount)
                    )
                }

                PaymentModes.WALLET.paymentModeID -> {
                    getString(
                        R.string.convenience_fees_x_added_upi,
                        Utils.convertToRupeesWithSymobl(this, amount)
                    )
                }

                PaymentModes.NET_BANKING.paymentModeID -> {
                    getString(
                        R.string.convenience_fees_x_added_nb,
                        Utils.convertToRupeesWithSymobl(this, amount)
                    )
                }

                PaymentModes.EMI.paymentModeID -> {
                    getString(
                        R.string.convenience_fees_x_added_upi,
                        Utils.convertToRupeesWithSymobl(this, amount)
                    )
                }

                else -> {
                    getString(
                        R.string.convenience_fees_x_added_general,
                        Utils.convertToRupeesWithSymobl(this, amount)
                    )
                }
            }
        } else {
            if (showDefaultCardMessage) {
                getString(R.string.a_convenience_fee_may_be_added_based_on_your_selected_card_network)
            } else {
                getString(R.string.a_convenience_fee_may_be_added_based_on_your_selected_payment_method)
            }
        }
    }


    fun updateOrderAmount(payableAmount: Int) {
        val amountString = Utils.convertToRupeesWithSymobl(this, payableAmount)
        val spannable: Spannable = SpannableString(amountString)
        val end = amountString.length
        val start = end - (amountString.split(".")[1]).length

        spannable.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.grey_99FFFFFF)),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        originalAmount.setText(spannable, TextView.BufferType.SPANNABLE)
        SdkLogger.log(
            this,
            "ORDER_AMOUNT_UPDATED",
            "Order amount updated to $payableAmount",
            ExpressSDKObject.getFetchData()?.transactionInfo?.orderId ?: "",
            "INFO",
            "SDK"
        )
    }


}
