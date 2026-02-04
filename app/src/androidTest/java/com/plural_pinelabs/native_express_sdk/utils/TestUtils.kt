package com.plural_pinelabs.native_express_sdk.utils

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId

fun waitForView(viewId: Int, timeout: Long = 5_000) {
    val startTime = System.currentTimeMillis()
    val endTime = startTime + timeout
    do {
        try {
            onView(withId(viewId)).check(matches(isDisplayed()))
            return
        } catch (e: Exception) {
            Thread.sleep(200)
        }
    } while (System.currentTimeMillis() < endTime)

    throw AssertionError("View with id $viewId not found within $timeout ms")
}
