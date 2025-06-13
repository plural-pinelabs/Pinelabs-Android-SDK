package com.plural_pinelabs.expresscheckoutsdk.presentation.acs

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ACSViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BFF_RESPONSE_HANDLER_ENDPOINT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.IMAGE_LOGO
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_ATTEMPTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_PENDING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.MTAG
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import kotlinx.coroutines.launch

internal class ACSFragment : Fragment() {

    private lateinit var webAcs: WebView
    private lateinit var constrainSuccess: ConstraintLayout
    private lateinit var logoAnimation: LottieAnimationView
    private lateinit var redirectUrl: String
    private lateinit var viewModel: ACSFragmentViewModel

    private var orderId: String? = null
    private var paymentId: String? = null
    var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(
            this,
            ACSViewModelFactory(NetworkHelper(requireContext()))
        )[ACSFragmentViewModel::class.java]
        return inflater.inflate(R.layout.fragment_a_c_s, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        val headerLayout = requireActivity().findViewById<ConstraintLayout>(R.id.header_layout)
        headerLayout.visibility = View.GONE
        initializeValueFromProcessPaymentResponse()
        setUpViews(view)
        observeViewModel()
        setUpWebView()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.transactionStatusResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            //Throw error and exit SDK
                            findNavController().navigate(R.id.action_UPIFragment_to_failureFragment)
                        }

                        is BaseResult.Loading -> {
                            // nothing to do since we already show the process payment dialog
                        }

                        is BaseResult.Success<TransactionStatusResponse> -> {
                            val status = it.data.data.status
                            when (status) {
                                PROCESSED_PENDING -> {
                                    // Do nothing, we will keep polling for the transaction status
                                }

                                PROCESSED_STATUS -> {
                                    findNavController().navigate(R.id.action_ACSFragment_to_successFragment)
                                }

                                PROCESSED_ATTEMPTED -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Payment attempted TODO handle retry flow",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    findNavController().navigate(R.id.action_cardFragment_to_failureFragment)
                                    // TODO ATTEMPTED Handle the scenario for retry or failure
                                }

                                PROCESSED_FAILED -> {
                                    findNavController().navigate(R.id.action_ACSFragment_to_failureFragment)
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    private fun initializeValueFromProcessPaymentResponse() {
        val processPaymentResponse = ExpressSDKObject.getProcessPaymentResponse()
        processPaymentResponse?.let {
            orderId = it.order_id
            paymentId = it.payment_id
            token = ExpressSDKObject.getToken()
            redirectUrl = it.redirect_url ?: ""
        } ?: run {
            findNavController().navigate(R.id.action_ACSFragment_to_failureFragment)
        }
    }

    private fun setUpViews(view: View) {
        webAcs = view.findViewById(R.id.web_acs)
        constrainSuccess = view.findViewById(R.id.constrain_success)
        logoAnimation = view.findViewById(R.id.img_success_logo)
        logoAnimation.setAnimationFromUrl(IMAGE_LOGO)
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        webAcs.settings.javaScriptEnabled = true
        webAcs.settings.loadWithOverviewMode = true
        webAcs.settings.useWideViewPort = true
        webAcs.settings.setSupportZoom(true)
        webAcs.settings.builtInZoomControls = true
        webAcs.settings.javaScriptCanOpenWindowsAutomatically = true
        webAcs.settings.displayZoomControls = false
        webAcs.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        webAcs.clearCache(true)
        webAcs.clearHistory()
        webAcs.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webAcs.loadUrl(redirectUrl)
        webAcs.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url?.contains(BFF_RESPONSE_HANDLER_ENDPOINT, ignoreCase = true) == true) {
                    webAcs.visibility = View.GONE
                    constrainSuccess.visibility = View.VISIBLE
                    viewModel.getTransactionStatus(ExpressSDKObject.getToken())
                }
            }
        }
        webAcs.addJavascriptInterface(
            WebAppInterface(),
            Constants.ACS_JAVA_SCRIPT_INTERFACE
        )
    }


    inner class WebAppInterface {
        @JavascriptInterface
        fun postMessage(response: String?) {
            Log.d(MTAG, "ACS Interface response $response")
            webAcs.visibility = View.GONE
            constrainSuccess.visibility = View.VISIBLE
            viewModel.getTransactionStatus(ExpressSDKObject.getToken())
        }
    }

}