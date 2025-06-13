package com.plural_pinelabs.expresscheckoutsdk.common

import android.util.Log

class CustomExceptionHandler(
    private val originalHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Handle the exception (e.g., log it, report it, etc.)
        Log.e("ExpressLibrary", "Uncaught exception: ${throwable.message}", throwable)

        // Optionally prevent crash by not calling originalHandler
        // But be cautious: suppressing all crashes can hide critical issues

        // If you want to let the app crash after logging:
        originalHandler?.uncaughtException(thread, throwable)
    }
}
