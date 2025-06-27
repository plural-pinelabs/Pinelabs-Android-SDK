package com.plural_pinelabs.expresscheckoutsdk.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout

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
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                progressBarContainer.visibility = View.GONE
                errorView.visibility = View.VISIBLE
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
                if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight) {
                    AndroidInterface.onScrollEnd();
                }
            }
        """.trimIndent(), null
        )
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onScrollEnd() {
            onScrollEnd?.invoke()
        }
    }

    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    fun destroy() {
        webView.removeJavascriptInterface("AndroidInterface")
    }
}
