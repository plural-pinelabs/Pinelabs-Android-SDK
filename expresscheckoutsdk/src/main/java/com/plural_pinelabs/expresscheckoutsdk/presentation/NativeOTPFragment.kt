package com.plural_pinelabs.expresscheckoutsdk.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.AppSignatureHelper
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.NativeOTPFragmentViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.Palette
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import kotlinx.coroutines.launch

class NativeOTPFragment : Fragment() {
    private lateinit var backButton: ImageView
    private lateinit var otpInputField: EditText
    private lateinit var resendOtpText: TextView
    private lateinit var autoReadMessage: TextView
    private lateinit var errorMessageOtp: TextView
    private lateinit var continueButton: AppCompatButton
    private lateinit var bankWebsiteText: TextView
    private var token: String? = null
    private lateinit var paymentId: String
    private lateinit var orderId: String
    private var resendEnable: Boolean? = false
    private var resendTimer: String? = "180"
    private var paymentRequest: ProcessPaymentRequest? = null
    private var palette: Palette? = null
    private lateinit var viewModel: NativeOTPViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(
            this,
            NativeOTPFragmentViewModelFactory(NetworkHelper(requireContext()))
        )[NativeOTPViewModel::class.java]
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_native_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
        handleAutoReadOtp()
        handleBackButtonClick()
        observeViewModel()
        callRequestOTP()
        handleContinueButtonClick()


    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.otpRequestResult.collect { result ->
                    when (result) {
                        is BaseResult.Loading -> {
                            // Show loading state
                        }

                        is BaseResult.Success -> {
                            // Handle success TODO start the timer for resend OTP and initialize the UI accordingly

                        }

                        is BaseResult.Error -> {
                            // TODO Handle error
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.otpSubmitResult.collect { result ->
                    when (result) {
                        is BaseResult.Loading -> {
                            // Show loading state
                        }

                        is BaseResult.Success -> {
                            // Handle success TODO OTP submitted successfully, navigate to next screen

                        }

                        is BaseResult.Error -> {
                            // TODO Handle error
                        }
                    }
                }
            }
        }
    }

    private fun callRequestOTP() {
        paymentId = ExpressSDKObject.getProcessPaymentResponse()?.payment_id ?: ""
        val otpRequest = OTPRequest(payment_id = paymentId)
        viewModel.generateOTP(token, otpRequest)
    }

    private fun submitOTP() {
        val otpRequest = OTPRequest(paymentId, otpInputField.text.toString(), null, null, null)
        viewModel.submitOtp(ExpressSDKObject.getToken(), otpRequest)
    }

    private fun handleAutoReadOtp() {
        val appSignatureHelper = AppSignatureHelper(requireActivity())
        val client = SmsRetriever.getClient(requireActivity())
        client.startSmsUserConsent(null)
        val retriever = client.startSmsRetriever()
    }

    private fun setUpViews(view: View) {
        backButton = view.findViewById(R.id.back_button)
        otpInputField = view.findViewById(R.id.otp_input_view)
        resendOtpText = view.findViewById(R.id.resent_otp_text)
        autoReadMessage = view.findViewById(R.id.auto_read_message)
        errorMessageOtp = view.findViewById(R.id.error_message_otp)
        continueButton = view.findViewById(R.id.continue_btn)
        bankWebsiteText = view.findViewById(R.id.bank_Website_)
    }

    private fun handleBackButtonClick() {
        backButton.setOnClickListener {
            // Handle back button click
            findNavController().popBackStack()
        }
    }

    private fun handleContinueButtonClick() {
        continueButton.setOnClickListener {
            // Handle continue button click
            val otp = otpInputField.text.toString()
            if (otp.isNotEmpty()) {
                submitOTP()
            } else {
                errorMessageOtp.visibility = View.VISIBLE
                errorMessageOtp.text = getString(R.string.wrong_otp_please_re_enter)
            }
        }
    }
}