package com.plural_pinelabs.native_express_sdk.flows

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.plural_pinelabs.native_express_sdk.MainActivity
import com.plural_pinelabs.native_express_sdk.base.BaseExpressSdkE2ETest
import com.plural_pinelabs.native_express_sdk.utils.TestTokens
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Flow2InvalidAutoCloseTest : BaseExpressSdkE2ETest() {

    @Rule
    @JvmField
    val rule = ActivityScenarioRule(MainActivity::class.java)

    /**
     * Flow:
     * 1. Start SDK with invalid token
     * 2. SDK shows failure screen
     * 3. SDK auto-closes
     * 4. App returns to merchant screen
     */
    @Test
    fun invalid_token_auto_close() {

        // 🚀 Start SDK with invalid token
        startSdk(TestTokens.TOKEN_INVALID)

        // ❌ Failure screen must appear
        assertFailureScreen()

        assertSdkClosed()

    }
}
