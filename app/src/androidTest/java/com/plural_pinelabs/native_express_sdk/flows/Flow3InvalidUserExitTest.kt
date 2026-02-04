package com.plural_pinelabs.native_express_sdk.flows

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.*
import org.junit.runner.RunWith
import com.plural_pinelabs.expresscheckoutsdk.R as SdkR
import com.plural_pinelabs.native_express_sdk.MainActivity
import com.plural_pinelabs.native_express_sdk.base.BaseExpressSdkE2ETest
import com.plural_pinelabs.native_express_sdk.utils.TestTokens

@RunWith(AndroidJUnit4::class)
class Flow3InvalidUserExitTest : BaseExpressSdkE2ETest() {

    @Rule @JvmField
    val rule = ActivityScenarioRule(MainActivity::class.java)

    //TODO uncomment later when code merged from release
//    @Test
//    fun invalid_token_user_exit() {
//        startSdk(TestTokens.TOKEN_INVALID)
//        assertFailureScreen()
//
//        onView(withId(SdkR.id.continue_btn))
//            .perform(click())
//
//        assertSdkClosed()
//    }
}
