package com.plural_pinelabs.expresscheckoutsdk.common

import androidx.test.espresso.idling.CountingIdlingResource

object SdkE2ETestController {
    val idlingResource = CountingIdlingResource("ExpressSDK-E2E")
}
