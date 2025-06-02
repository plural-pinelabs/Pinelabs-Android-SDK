package com.plural_pinelabs.native_express_sdk

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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

        setViews()

    }

    private fun setViews() {
        val tokenEt = findViewById<EditText>(R.id.edt_redirect_url)
        val startSDKBtn = findViewById<Button>(R.id.btn_start_sdk)
        startSDKBtn.setOnClickListener {
            ExpressSDKInitializer().initializeSDK(
                this@MainActivity,
                "${tokenEt.text}",
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
}