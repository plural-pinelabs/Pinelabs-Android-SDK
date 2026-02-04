package com.plural_pinelabs.native_express_sdk.flows

import android.webkit.WebView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.*
import androidx.test.espresso.web.webdriver.Locator
import org.junit.*
import org.junit.runner.RunWith
import com.plural_pinelabs.expresscheckoutsdk.R as SdkR
import com.plural_pinelabs.native_express_sdk.MainActivity
import com.plural_pinelabs.native_express_sdk.base.BaseExpressSdkE2ETest
import com.plural_pinelabs.native_express_sdk.utils.BankSimulatorAndroid
import com.plural_pinelabs.native_express_sdk.utils.TestTokens
import com.plural_pinelabs.native_express_sdk.utils.waitForView

@RunWith(AndroidJUnit4::class)
class Flow4CardsPaymentTest : BaseExpressSdkE2ETest() {

    @Rule
    @JvmField
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun enableWebDebug() {
        WebView.setWebContentsDebuggingEnabled(true)
    }

    @Test
    fun cards_full_payment_flow() {

        // ---------- Start SDK ----------
        startSdk(TestTokens.TOKEN_GOLDEN)
        assertPaymentLanding()

        // ---------- Open Cards ----------
        onView(withId(SdkR.id.payment_option_list))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click())
            )

        // ---------- Fill Card Details ----------
        onView(withId(SdkR.id.card_number_et))
            .perform(typeText("4000000000001091"))

        onView(withId(SdkR.id.expiry_date_et))
            .perform(typeText("12/30"))

        onView(withId(SdkR.id.cvv_et))
            .perform(typeText("123"))

        onView(withId(SdkR.id.full_name_et))
            .perform(typeText("TEST USER"), closeSoftKeyboard())

        // ---------- Continue ----------
        onView(withId(SdkR.id.continue_btn))
            .check(matches(isEnabled()))

        onView(withId(SdkR.id.continue_btn))
            .perform(click())

        // ---------- Wait for ACS WebView ----------
        waitForView(SdkR.id.web_acs, 10_000)

        // ---------- ACS Simulator Page Automation ----------
        // iOS equivalent:
        // app.textFields[" Enter Code Here"].typeText("1234")
        // app.buttons["SUBMIT"].tap()

        Thread.sleep(8000) // wait for simulator HTML load

        BankSimulatorAndroid.handleAnyAcsSuccess()

        // ---------- Wait for redirect ----------
        Thread.sleep(5000)

        // ---------- Final Assertion ----------
        // Success screen OR SDK close
        // depending on your flow design
        //assertSuccessScreen()
        // OR
        // assertSdkClosed()
    }
}
