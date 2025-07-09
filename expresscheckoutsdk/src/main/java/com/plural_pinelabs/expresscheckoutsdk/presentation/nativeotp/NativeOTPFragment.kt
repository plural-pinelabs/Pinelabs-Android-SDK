package com.plural_pinelabs.expresscheckoutsdk.presentation.nativeotp

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.AppSignatureHelper
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.REQ_USER_CONSENT
import com.plural_pinelabs.expresscheckoutsdk.common.NativeOTPFragmentViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.SmsBroadcastReceiver
import com.plural_pinelabs.expresscheckoutsdk.common.TimerManager
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.Palette
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern

class NativeOTPFragment : Fragment() {
    private lateinit var backButton: ImageView
    private lateinit var otpInputField: EditText
    private lateinit var resendOtpText: TextView
    private lateinit var autoReadMessage: TextView
    private lateinit var errorMessageOtp: TextView
    private lateinit var continueButton: AppCompatButton
    private lateinit var bankWebsiteText: TextView
    private lateinit var otpSentMessage: TextView
    private lateinit var smsBroadcastReceiver: SmsBroadcastReceiver
    private lateinit var paymentId: String
    private var resendEnable: Boolean? = false
    private var resendTimer: String? = "180"
    private var palette: Palette? = null
    private lateinit var viewModel: NativeOTPViewModel
    private var timer = TimerManager
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var otpId: String? = null


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
        resendTimer = arguments?.getString("resend_after")
        otpId = arguments?.getString("otp_id")
        paymentId = ExpressSDKObject.getProcessPaymentResponse()?.payment_id ?: ""
        setUpViews(view)
        handleAutoReadOtp()
        handleBackButtonClick()
        observeViewModel()
        handleContinueButtonClick()
        initTimer(resendTimer?.toLong())
        setOTPTextListener()
    }

    private fun setOTPTextListener() {
        otpInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty() && s.length >= 4) {
                    errorMessageOtp.visibility = View.GONE
                    continueButton.isEnabled = true
                    Utils.handleCTAEnableDisable(requireContext(), true, continueButton)
                } else {
                    continueButton.isEnabled = false
                    Utils.handleCTAEnableDisable(requireContext(), false, continueButton)
                }
            }
        })
    }

    private fun initTimer(timeInMillis: Long?) {
        timer.startTimer(timeInMillis?.times(1000) ?: 18000)
        timer.timeLeft.observe(viewLifecycleOwner) { timeLeft ->
            if (timeLeft > 0) {
                resendOtpText.text = getString(
                    R.string.resend_otp_in,
                    Utils.formatTimeInMinutes(requireContext(), timeLeft)
                )
                resendOtpText.isEnabled = false
            } else {
                resendOtpText.text = getString(R.string.resend_otp)
                resendOtpText.isEnabled = true
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.otpSubmitResult.collect { result ->
                    when (result) {
                        is BaseResult.Loading -> {
                            if (result.isLoading){
                                bottomSheetDialog = Utils.showProcessPaymentDialog(
                                    requireContext(),
                                )
                            }

                        }

                        is BaseResult.Success -> {
                            bottomSheetDialog?.dismiss()
                            viewModel.getTransactionStatus(ExpressSDKObject.getToken())
                        }

                        is BaseResult.Error -> {
                            bottomSheetDialog?.dismiss()
                            findNavController().navigate(R.id.action_nativeOTPFragment_to_retryFragment)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transactionStatusResult.collect { result ->
                    when (result) {
                        is BaseResult.Loading -> {
                        }

                        is BaseResult.Success -> {
                            result.data.data.let { data ->
                                if (data.status == PROCESSED_STATUS) {
                                    findNavController().navigate(R.id.action_nativeOTPFragment_to_successFragment)
                                } else {
                                    if (data.is_retry_available) {
                                        findNavController().navigate(R.id.action_nativeOTPFragment_to_retryFragment)
                                    } else {
                                        findNavController().navigate(R.id.action_nativeOTPFragment_to_failureFragment)
                                    }
                                }
                            }
                        }

                        is BaseResult.Error -> {
                            findNavController().navigate(R.id.action_nativeOTPFragment_to_ACSFragment)
                        }
                    }
                }
            }
        }

    }


    private fun submitOTP(otp: String) {
        val otpRequest = OTPRequest(paymentId, otp, null, null, null)
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
        otpSentMessage = view.findViewById(R.id.otp_sent_message)
        bankWebsiteText.setOnClickListener {
            findNavController().navigate(R.id.action_nativeOTPFragment_to_ACSFragment)
        }

        val mobileNumber = ExpressSDKObject.getFetchData()?.customerInfo?.mobileNumber
            ?: ExpressSDKObject.getFetchData()?.customerInfo?.mobile_number
    }

    private fun handleBackButtonClick() {
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleContinueButtonClick() {
        continueButton.setOnClickListener {
            val otp = otpInputField.text.toString()
            if (otp.isNotEmpty()) {
                submitOTP(otp)
            } else {
                errorMessageOtp.visibility = View.VISIBLE
                errorMessageOtp.text = getString(R.string.wrong_otp_please_re_enter)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === REQ_USER_CONSENT) {
            if ((resultCode === RESULT_OK) && (data != null)) {
                val message: String? = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                getOtpFromMessage(message)
            }
        }
    }

    private fun startSmartUserConsent() {
        val client = SmsRetriever.getClient(requireActivity())
        client.startSmsUserConsent(null)
        val retriever = client.startSmsRetriever()
        /*retriever.addOnSuccessListener { message ->

        }
        retriever.addOnFailureListener { message ->

        }*/
    }

    private fun getOtpFromMessage(message: String?) {
        val otpPattern: Pattern = Pattern.compile("(|^)\\d{4,9}")
        val matcher: Matcher = otpPattern.matcher(message)
        if (matcher.find()) {
            otpInputField.setText(matcher.group(0))
        }
    }

    private fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver.smsBroadcastReceiverListener =
            object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    print("SMS Success ")
                    if (intent != null) {
                        startActivityForResult(intent, REQ_USER_CONSENT)
                    }
                }

                override fun onFailure() {
                }
            }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        requireActivity().registerReceiver(
            smsBroadcastReceiver,
            intentFilter,
            Context.RECEIVER_EXPORTED
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart();
        registerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(smsBroadcastReceiver)
    }

}

