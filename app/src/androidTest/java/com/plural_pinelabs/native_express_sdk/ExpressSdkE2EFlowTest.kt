package com.plural_pinelabs.native_express_sdk

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.IdlingRegistry

import org.junit.*
import org.junit.runner.RunWith

import com.plural_pinelabs.expresscheckoutsdk.R as SdkR

// ✅ COMMON PACKAGE IMPORTS
import com.plural_pinelabs.expresscheckoutsdk.common.SdkE2ETestController
import com.plural_pinelabs.expresscheckoutsdk.common.SdkTestMode


@RunWith(AndroidJUnit4::class)
class ExpressSdkE2EFlowTest {

    @Rule
    @JvmField
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        // 🔓 Enable SDK test mode (ONLY for Espresso)
        SdkTestMode.enabled = true

        // Register idling resource
        IdlingRegistry.getInstance()
            .register(SdkE2ETestController.idlingResource)
    }

    @After
    fun tearDown() {
        // 🔒 Disable SDK test mode
        SdkTestMode.enabled = false

        // Unregister idling resource
        IdlingRegistry.getInstance()
            .unregister(SdkE2ETestController.idlingResource)
    }

//    @Test
//    fun fullSdkFlow_realBackend_e2e() {
//
//        // 1️⃣ Enter REAL token
//        onView(withId(R.id.edt_redirect_url))
//            .perform(
//                typeText("V3_WpR1nBmN7ywUXUM4GzUPMs0feWRqf3V%2BMeqyQirJzm4TMW7ZKvhIl6mSipAVHwh%2F"),
//                closeSoftKeyboard()
//            )
//
//        // 2️⃣ Start SDK
//        onView(withId(R.id.btn_start_sdk))
//            .perform(click())
//
//        // 3️⃣ LandingActivity root
//        onView(withId(SdkR.id.main))
//            .check(matches(isDisplayed()))
//
//        // 5️⃣ Payment Mode Fragment
//        onView(withId(SdkR.id.payment_option_list))
//            .check(matches(isDisplayed()))
//
//        // 6️⃣ Verify payment UI
//        onView(withText("Cards"))
//            .check(matches(isDisplayed()))
//
//        // 7️⃣ Verify backend-bound UI
//        onView(withId(SdkR.id.merchant_name_title))
//            .check(matches(isDisplayed()))
//
//        onView(withId(SdkR.id.txt_amount))
//            .check(matches(isDisplayed()))
//    }
}
