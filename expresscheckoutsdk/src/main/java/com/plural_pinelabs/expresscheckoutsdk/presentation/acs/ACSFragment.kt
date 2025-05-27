package com.plural_pinelabs.expresscheckoutsdk.presentation.acs

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BFF_RESPONSE_HANDLER_ENDPOINT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.IMAGE_LOGO
import com.plural_pinelabs.expresscheckoutsdk.common.TimerManager

internal class ACSFragment : Fragment() {

    private lateinit var webAcs: WebView
    private lateinit var constrainSuccess: ConstraintLayout
    private lateinit var logoAnimation: LottieAnimationView
    private lateinit var redirectUrl: String

    var orderId: String? = null
    var paymentId: String? = null
    var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_a_c_s, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        );
        val headerLayout = requireActivity().findViewById<ConstraintLayout>(R.id.header_layout)
        headerLayout.visibility = View.GONE
        initializeValueFromProcessPaymentResponse()
        setUpViews(view)
        setUpWebView()

    }


    private fun initializeValueFromProcessPaymentResponse() {
        val processPaymentResponse = ExpressSDKObject.getProcessPaymentResponse()
        processPaymentResponse?.let {
            orderId = it.order_id
            paymentId = it.payment_id
            token = ExpressSDKObject.getToken()
            redirectUrl = it.redirect_url ?: ""
        } ?: run {
            // TODO  Exit the SDK if the response is null

        }
    }

    private fun setUpViews(view: View) {
        webAcs = view.findViewById(R.id.web_acs)
        constrainSuccess = view.findViewById(R.id.constrain_success)
        logoAnimation = view.findViewById(R.id.img_success_logo)
        logoAnimation.setAnimationFromUrl(IMAGE_LOGO)
    }


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
        webAcs.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webAcs.loadUrl(redirectUrl)
        webAcs.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url?.contains(BFF_RESPONSE_HANDLER_ENDPOINT, ignoreCase = true) == true) {
                    webAcs.visibility = View.GONE
                    constrainSuccess.visibility = View.VISIBLE
                    //success we can close
                }
            }
        }
        webAcs.addJavascriptInterface(
            WebAppInterface(requireActivity()),
            Constants.ACS_JAVA_SCRIPT_INTERFACE
        )
    }

    inner class WebAppInterface(context: Activity) {

        @JavascriptInterface
        fun postMessage(response: String?) {
            webAcs.visibility = View.GONE
            constrainSuccess.visibility = View.VISIBLE
            // listener?.onRetry(true, "", "")
            //TODO listen to the status API

        }
    }

}