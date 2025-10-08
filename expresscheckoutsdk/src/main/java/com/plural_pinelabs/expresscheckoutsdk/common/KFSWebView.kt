package com.plural_pinelabs.expresscheckoutsdk.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.logger.SdkLogger

class KFSWebView(
    private val context: Context,
    private val webView: WebView,
    private val progressBarContainer: LinearLayout,
    private val errorView: LinearLayout,
    private val onScrollEnd: (() -> Unit)? = null
) {

    init {
        setupWebView()
    }

    @SuppressLint("JavascriptInterface")
    private fun setupWebView() {
        SdkLogger.log(
            context,
            "WEBVIEW_INIT",
            "KFS Initializing WebView",
            "",
            "INFO",
            "SDK"
        )
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.addJavascriptInterface(WebAppInterface(), "AndroidInterface")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBarContainer.visibility = View.VISIBLE
                errorView.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBarContainer.visibility = View.GONE
                webView.visibility = View.VISIBLE
                injectScrollListener()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                progressBarContainer.visibility = View.GONE
                errorView.visibility = View.VISIBLE
                SdkLogger.log(
                    context,
                    "WEBVIEW_ERROR",
                    " KFS WebView error: ${error?.description}",
                    ExpressSDKObject.getFetchData()?.transactionInfo?.orderId ?: "",
                    "HIGH",
                    "SDK"
                )
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                progressBarContainer.visibility = View.GONE
                errorView.visibility = View.VISIBLE
                SdkLogger.log(
                    context,
                    "WEBVIEW_HTTP_ERROR",
                    " KFS WebView HTTP error: ${errorResponse?.statusCode}",
                    ExpressSDKObject.getFetchData()?.transactionInfo?.orderId ?: "",
                    "HIGH",
                    "SDK"
                )
            }
        }

        webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val contentHeight = (webView.contentHeight * webView.scale).toInt()
            val viewHeight = webView.height
            if (scrollY + viewHeight >= contentHeight) {
                onScrollEnd?.invoke()
            }
        }
    }

    private fun injectScrollListener() {
        webView.evaluateJavascript(
            """
            window.onscroll = function() {
                if ((window.innerHeight + window.scrollY) &gt; document.body.offsetHeight) {
                    AndroidInterface.onScrollEnd();
                }
            }
        """.trimIndent(), null
        )
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onScrollEnd() {
            Log.d("WebView", "JavaScript scroll end triggered")

            onScrollEnd?.invoke()
        }
    }
}
