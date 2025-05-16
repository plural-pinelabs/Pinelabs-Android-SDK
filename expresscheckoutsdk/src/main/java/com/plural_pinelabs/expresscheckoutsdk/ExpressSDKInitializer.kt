package com.plural_pinelabs.expresscheckoutsdk

import android.content.Context
import android.content.Intent
import android.util.Log
import com.plural_pinelabs.expresscheckoutsdk.common.ErrorCode
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.RootUtil
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.MTAG
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity

class ExpressSDKInitializer {

    fun initializeSDK(context: Context, token: String, sdkCallback: ExpressSDKCallback) {
        //TODO remove after testing
        navigateToLandingActivity( context,sdkCallback,token)
        return
        if (!NetworkHelper(context).hasInternetConnection()) {
            sdkCallback.onError(
                ErrorCode.INVALID_TOKEN.code, "-1",
                context.getString(R.string.no_internet_connection)
            )
        } else if (RootUtil.isDeviceIsRooted()) {
            sdkCallback.onError(
                ErrorCode.INVALID_TOKEN.code, "-2",
                context.getString(R.string.device_is_rooted)
            )
        } else if (token.isEmpty()) {
            sdkCallback.onError(
                ErrorCode.INVALID_TOKEN.code, "-3",
                context.getString(R.string.invalid_token)
            )
        } else {
            navigateToLandingActivity(context, sdkCallback, token)
        }
    }


    private fun navigateToLandingActivity(context: Context, sdkCallback: ExpressSDKCallback,token: String) {
        ExpressSDKObject.initialize(context, sdkCallback, token)
        Log.i(MTAG, "SDK Initialized successfully")
        val intent = Intent(context, LandingActivity::class.java)
        context.startActivity(intent)
    }
}