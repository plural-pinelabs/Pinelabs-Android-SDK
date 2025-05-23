package com.plural_pinelabs.native_express_sdk

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKCallback
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKInitializer

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ExpressSDKInitializer().initializeSDK(
            this@MainActivity,
            "V3_s7SUlQCJJehvWnoxyNVhQgBR4GabD2DnPuI3HGvdf96sXKazzCiE1Q5GLDfE3gnZ\n",
            object : ExpressSDKCallback {
                override fun onError(
                    errorCode: String?,
                    errorMessage: String?,
                    errorDescription: String?
                ) {
                    Log.e("ExpressSDK", "Error: $errorCode, $errorMessage, $errorDescription")
                }

                override fun onSuccess(
                    responseCode: String?,
                    responseMessage: String?,
                    responseDescription: String?
                ) {
                    Log.i(
                        "ExpressSDK",
                        "Success: $responseCode, $responseMessage, $responseDescription"
                    )
                }

                override fun onCancel(
                    responseCode: String?,
                    responseMessage: String?,
                    responseDescription: String?
                ) {
                    Log.d(
                        "ExpressSDK",
                        "Cancelled: $responseCode, $responseMessage, $responseDescription"
                    )
                }
            }
        )
    }
}