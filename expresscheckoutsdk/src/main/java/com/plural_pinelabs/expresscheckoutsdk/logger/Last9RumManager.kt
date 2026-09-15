package com.plural_pinelabs.expresscheckoutsdk.logger

import android.app.Application
import android.content.Context
import android.util.Log
import com.plural_pinelabs.expresscheckoutsdk.BuildConfig
import io.last9.rum.L9RumConfig
import io.last9.rum.L9Rum
import okhttp3.OkHttpClient

internal object Last9RumManager {
    private const val TAG = "Last9RumManager"
    private const val SERVICE_VERSION = "1.0.8"
    private const val PLACEHOLDER_CLIENT_TOKEN = "replace-with-last9-client-token"
    private const val ATTR_SCREEN = "express.screen"
    private const val ATTR_EVENT = "express.event"

    @Volatile
    private var instrumented = false

    fun initialize(context: Context, token: String, runInSandboxMode: Boolean) {
        val application = context.applicationContext as? Application ?: return
        val baseUrl = BuildConfig.LAST9_BASE_URL.trim()
        val clientToken = BuildConfig.LAST9_CLIENT_TOKEN.trim()
        if (baseUrl.isEmpty()) {
            Log.i(TAG, "Last9 init skipped: baseUrl is empty")
            return
        }

        if (clientToken.isEmpty()) {
            Log.i(TAG, "Last9 init skipped: clientToken is empty")
            return
        }

        if (clientToken == PLACEHOLDER_CLIENT_TOKEN) {
            Log.i(
                TAG,
                "Last9 init running with placeholder clientToken; local SDK logs will work but telemetry export may be rejected"
            )
        }

        Log.i(
            TAG,
            "Last9 init requested: origin=${BuildConfig.LAST9_ORIGIN}, flow=${BuildConfig.LAST9_FLOW_NAME}, sandbox=$runInSandboxMode, tokenPresent=${token.isNotBlank()}"
        )

        if (BuildConfig.LAST9_ORIGIN.isBlank()) {
            Log.i(TAG, "Last9 init skipped: origin is empty")
            return
        }

        runCatching {
            if (L9Rum.isActive()) {
                Log.i(TAG, "Last9 was already active; shutting down before re-init")
                L9Rum.shutdown()
            }

            L9Rum.initialize(
                application,
                L9RumConfig(
                    baseUrl = baseUrl,
                    origin = BuildConfig.LAST9_ORIGIN,
                    clientToken = clientToken,
                    serviceName = BuildConfig.LAST9_FLOW_NAME.ifBlank { "express-checkout" },
                    serviceVersion = SERVICE_VERSION,
                    deploymentEnvironment = if (runInSandboxMode) "sandbox" else "production"
                )
            )
            L9Rum.spanAttributes(
                mapOf(
                    "express.sdk.token_present" to token.isNotBlank().toString(),
                    "express.sdk.sandbox" to runInSandboxMode.toString()
                )
            )
            Log.i(TAG, "Last9 initialized successfully and span attributes were attached")
        }.onFailure {
            Log.w(TAG, "Last9 RUM initialization failed", it)
        }
    }

    fun instrument(builder: OkHttpClient.Builder) {
        if (instrumented) {
            Log.i(TAG, "Last9 OkHttp instrumentation skipped: already instrumented")
            return
        }

        runCatching {
            L9Rum.instrumentOkHttp(builder)
            instrumented = true
            Log.i(TAG, "Last9 OkHttp instrumentation installed")
        }.onFailure {
            Log.w(TAG, "Last9 OkHttp instrumentation failed", it)
        }
    }

    fun shutdown() {
        runCatching {
            if (L9Rum.isActive()) {
                L9Rum.shutdown()
                Log.i(TAG, "Last9 shutdown completed")
            } else {
                Log.i(TAG, "Last9 shutdown skipped: SDK not active")
            }
        }.onFailure {
            Log.w(TAG, "Last9 RUM shutdown failed", it)
        }
    }

    fun trace(screen: String, event: String, attributes: Map<String, String> = emptyMap()) {
        if (!L9Rum.isActive()) {
            Log.i(TAG, "Last9 trace skipped: SDK not active for $screen/$event")
            return
        }

        val spanAttributes = linkedMapOf(
            ATTR_SCREEN to screen,
            ATTR_EVENT to event
        ).apply {
            putAll(attributes)
        }

        runCatching {
            L9Rum.spanAttributes(spanAttributes)
            Log.i(TAG, "Last9 trace recorded: screen=$screen, event=$event, attributes=$attributes")
        }.onFailure {
            Log.w(TAG, "Last9 trace failed: screen=$screen, event=$event", it)
        }
    }
}