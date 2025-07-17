package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.D2CViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.OtpInputView
import com.plural_pinelabs.expresscheckoutsdk.common.TimerManager
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfoResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpdateOrderDetails
import kotlinx.coroutines.launch


class VerifyOTPFragment : Fragment() {
    private lateinit var phoneNumberTv: TextView
    private lateinit var resentOTPTv: TextView
    private lateinit var otpInputView: OtpInputView
    private lateinit var verifyOtpBtn: Button
    private lateinit var resendTimer: TimerManager
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var isOtpVerified: Boolean = false

    private val viewModel: D2CViewModel by activityViewModels {
        D2CViewModelFactory(NetworkHelper(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verify_o_t_p, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView(view)
        observeViewModel()
        startResendOtpTimer()
    }

    private fun setView(view: View) {
        phoneNumberTv = view.findViewById(R.id.sent_phone_number_tv)
        resentOTPTv = view.findViewById(R.id.resend_otp_tv)
        otpInputView = view.findViewById(R.id.otp_input_view)
        verifyOtpBtn = view.findViewById(R.id.verify_otp_btn)
        setPhoneNumber()
        startResendOtpTimer()
        handleVerifyOtpButton()
        handleClickListener()
        otpInputView.setOtpCompleteListener {
            if (otpInputView.getOtp().length >= 5) {
                // Handle OTP input completion
                isOtpVerified = true
                Utils.handleCTAEnableDisable(requireContext(), true, verifyOtpBtn)
            }
        }
    }

    private fun handleVerifyOtpButton() {

    }

    private fun handleClickListener() {
        resentOTPTv.setOnClickListener {
            // TODO resend otp
            val otpRequest = OTPRequest(customerId = viewModel.customerInfo?.customerId)
            viewModel.sendOtp(
                ExpressSDKObject.getToken(),
                otpRequest
            ) // TODO observe change and show bottomsheet
        }

        verifyOtpBtn.setOnClickListener {
            // TODO verify otp
            findNavController().navigate(R.id.action_verifyOTPFragment_to_newAddressFormFragment)
            return@setOnClickListener
            val otp = otpInputView.getOtp()
            val token = ExpressSDKObject.getToken()
            val otpId = viewModel.otpId ?: ""
            val customerId = viewModel.customerInfo?.customerId ?: ""
            val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
            customerInfo?.mobile_number = ExpressSDKObject.getPhoneNumber()
            customerInfo?.customer_id = customerId
            customerInfo?.country_code = viewModel.countryCode // harcoded for now
            val updateOrderDetails = UpdateOrderDetails(
                customerInfo
            )
            val otpRequest = OTPRequest(
                otp = otp,
                otpId = otpId,
                customerId = customerId,
                updateOrderDetails = updateOrderDetails
            )
            viewModel.validateUpdateOrderDetails(ExpressSDKObject.getToken(), otpRequest)

        }
    }

    private fun startResendOtpTimer() {
        resendTimer = TimerManager
        resendTimer.startTimer(100000) // 10 seconds for demo, adjust as needed
        resendTimer.timeLeft.observe(viewLifecycleOwner) { timeLeft ->
            if (timeLeft > 0) {

                resentOTPTv.text = getString(
                    R.string.resend_otp_in, Utils.formatTimeInMinutes(
                        requireContext(),
                        timeLeft
                    )
                )
                resentOTPTv.isEnabled = false
            } else {
                resentOTPTv.text = getString(R.string.resend_otp)
                resentOTPTv.isEnabled = true
            }
        }
    }

    private fun setPhoneNumber() {
        val sendOtpWithPhoneNumber = getString(
            R.string.enter_the_otp_sent_to,
            "${ExpressSDKObject.getPhoneNumber()}",
        )
        phoneNumberTv.text = sendOtpWithPhoneNumber
    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.submitOTPResult.collect { result ->
                    when (result) {
                        is BaseResult.Error -> {
                            result.errorCode.let { exception ->
                                Log.e("Error", exception)
                            }

                        }

                        is BaseResult.Success<CustomerInfoResponse> -> {
                            //TODO handle success flow
                        }

                        is BaseResult.Loading -> {
                        }
                    }
                }
            }
        }
    }
}