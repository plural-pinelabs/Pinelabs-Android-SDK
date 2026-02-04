package com.plural_pinelabs.native_express_sdk.flows

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.*
import org.junit.runner.RunWith
import com.plural_pinelabs.native_express_sdk.MainActivity
import com.plural_pinelabs.native_express_sdk.base.BaseExpressSdkE2ETest
import com.plural_pinelabs.native_express_sdk.utils.TestTokens

@RunWith(AndroidJUnit4::class)
class Flow1LandingTest : BaseExpressSdkE2ETest() {

    @Rule @JvmField
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun landing_on_payment_mode() {
        startSdk(TestTokens.TOKEN_GOLDEN)
        assertPaymentLanding()
    }
}
