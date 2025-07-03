package com.plural_pinelabs.expresscheckoutsdk.presentation

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BottomSheetRetryFragment
import com.plural_pinelabs.expresscheckoutsdk.common.CustomExceptionHandler
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModeSharedViewModel
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode

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
    private lateinit var headerParentLayout: ConstraintLayout

    private lateinit var mainContentLayout: ConstraintLayout
    private val sharedViewModel: PaymentModeSharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    }

    private fun initExceptionHandler() {
        val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
        if (currentHandler !is CustomExceptionHandler) {
            Thread.setDefaultUncaughtExceptionHandler(
                CustomExceptionHandler(currentHandler)
            )
        }
    }


    private fun showHideHeaderLayout(isShow: Boolean) {
        val headerLayout = findViewById<View>(R.id.header_layout)
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
        headerParentLayout = findViewById(R.id.header_layout_parent)
        showHideHeaderLayout(false)
        mainContentLayout = findViewById(R.id.main)
        cancelBtn.setOnClickListener {
            Utils.showCancelPaymentDialog(this)
        }
        setupObservers()
    }


    fun updateValueForHeaderLayout(fetchResponse: FetchResponseDTO?) {
        fetchResponse?.let { fetchData ->
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
                val amountString = Utils.convertToRupees(this, amount)

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
        }
    }


    private fun setupObservers() {
        // Observe payment failure
        sharedViewModel.retryEvent.observe(this) {
            if (it)
                showRetryBottomSheet()
        }

        // Observe retry selection
        sharedViewModel.selectedPaymentMethod.observe(this) { selectedMethod ->
            navigateToPaymentFragment(selectedMethod)
        }
    }

    private fun showRetryBottomSheet() {
        val bottomSheetDialog =
            BottomSheetRetryFragment()
        bottomSheetDialog.isCancelable = false
        bottomSheetDialog.show(supportFragmentManager, "")
    }


    private fun navigateToPaymentFragment(selectedMethod: PaymentMode) {
        val navHostController = findNavController(R.id.nav_host_fragment_container)
//        val destinationId = when (selectedMethod.paymentModeId) {
//            PaymentModes.CREDIT_DEBIT.paymentModeID -> R.id.cardFragment
//
//            PaymentModes.UPI.paymentModeID -> {}
//        }

    }
}