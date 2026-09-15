package com.plural_pinelabs.expresscheckoutsdk.logger

import android.content.Context
import okhttp3.OkHttpClient

/** Last9 telemetry is disabled for this release. */
internal object Last9RumManager {
    fun initialize(context: Context, token: String, runInSandboxMode: Boolean) = Unit
    fun instrument(builder: OkHttpClient.Builder) = Unit
    fun shutdown() = Unit
    fun trace(screen: String, event: String, attributes: Map<String, String> = emptyMap()) = Unit
}
