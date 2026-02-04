package com.plural_pinelabs.native_express_sdk.base

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.plural_pinelabs.expresscheckoutsdk.R as SdkR
import com.plural_pinelabs.native_express_sdk.R
import com.plural_pinelabs.native_express_sdk.utils.waitForView
import com.plural_pinelabs.expresscheckoutsdk.common.SdkTestMode
import org.junit.After
import org.junit.Before

abstract class BaseExpressSdkE2ETest {

    // 🔐 Enable test mode for every test
    @Before
    fun enableTestMode() {
        SdkTestMode.enabled = true
    }

    // 🔓 Disable after test
    @After
    fun disableTestMode() {
        SdkTestMode.enabled = false
    }

    protected fun startSdk(token: String) {
        onView(withId(R.id.edt_redirect_url))
            .perform(replaceText(token), closeSoftKeyboard())

        onView(withId(R.id.btn_start_sdk))
            .perform(click())
    }

    protected fun assertPaymentLanding() {

        // 1️⃣ Splash (Lottie)
        waitForView(SdkR.id.img_logo, 10_000)
        onView(withId(SdkR.id.img_logo))
            .check(matches(isDisplayed()))

        // 2️⃣ LandingActivity root
        waitForView(SdkR.id.main, 15_000)
        onView(withId(SdkR.id.main))
            .check(matches(isDisplayed()))

        // 3️⃣ PaymentModeFragment
        waitForView(SdkR.id.payment_option_list, 20_000)
        onView(withId(SdkR.id.payment_option_list))
            .check(matches(isDisplayed()))

        // 4️⃣ Core UI checks
        onView(withText("Select a payment option"))
            .check(matches(isDisplayed()))

        onView(withId(SdkR.id.merchant_name_title))
            .check(matches(isDisplayed()))

        onView(withId(SdkR.id.txt_amount))
            .check(matches(isDisplayed()))
    }

    protected fun assertFailureScreen() {
        waitForView(SdkR.id.failure_root, 10_000)
        onView(withId(SdkR.id.failure_root))
            .check(matches(isDisplayed()))
    }

//    protected fun performSdkExit() {
//        onView(withId(SdkR.id.btn_continue_to_merchant))
//            .perform(click())
//    }

    protected fun assertSdkClosed() {
        // Back to merchant app
        waitForView(R.id.btn_start_sdk, 30_000)
        onView(withId(R.id.btn_start_sdk))
            .check(matches(isDisplayed()))
    }
}
