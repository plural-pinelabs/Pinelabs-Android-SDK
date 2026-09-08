package com.plural_pinelabs.expresscheckoutsdk.logger

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient

internal object Last9RumManager {
    private const val TAG = "Last9RumManager"

    fun initialize(context: Context, token: String, runInSandboxMode: Boolean) {
        Log.i(
            TAG,
            "Last9 disabled in public build: sandbox=$runInSandboxMode, tokenPresent=${token.isNotBlank()}, package=${context.packageName}"
        )
    }

    fun instrument(builder: OkHttpClient.Builder) {
        Log.i(TAG, "Last9 disabled in public build: OkHttp instrumentation skipped")
    }

    fun shutdown() {
        Log.i(TAG, "Last9 disabled in public build: shutdown skipped")
    }

    fun trace(screen: String, event: String, attributes: Map<String, String> = emptyMap()) {
        Log.i(TAG, "Last9 disabled in public build: trace skipped for $screen/$event with $attributes")
    }
}