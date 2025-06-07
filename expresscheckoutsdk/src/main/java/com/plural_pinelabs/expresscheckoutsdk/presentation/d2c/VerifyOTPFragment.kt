package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.OtpInputView
import com.plural_pinelabs.expresscheckoutsdk.common.TimerManager
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.VerifyOTPFragmentViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpdateOrderDetails
import kotlinx.coroutines.launch


class VerifyOTPFragment : Fragment() {
    private lateinit var phoneNumberTv: TextView
    private lateinit var resentOTPTv: TextView
    private lateinit var otpInputView: OtpInputView
    private lateinit var verifyOtpBtn: Button
    private lateinit var resendTimer: TimerManager

    private lateinit var viewModel: VerifyOTPFragmentViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var otpId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(
            this,
            VerifyOTPFragmentViewModelFactory(NetworkHelper(requireContext()))
        )[VerifyOTPFragmentViewModel::class.java]
        return inflater.inflate(R.layout.fragment_verify_o_t_p, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView(view)
      //  observeViewModel()
        // showProcessPaymentDialog()
      //  sendOTP()
        view.findViewById<Button>(R.id.verify_otp_btn).setOnClickListener {
            findNavController().navigate(R.id.action_verifyOTPFragment_to_savedAddressFragment)
        }
    }

    private fun setView(view: View) {
        phoneNumberTv = view.findViewById(R.id.sent_phone_number_tv)
        resentOTPTv = view.findViewById(R.id.resend_otp_tv)
        otpInputView = view.findViewById(R.id.otp_input_view)
        verifyOtpBtn = view.findViewById(R.id.verify_otp_btn)
        setPhoneNumber()
        startResendOtpTimer()
        handleClickListener()
    }

    private fun handleClickListener() {
        resentOTPTv.setOnClickListener {
            // TODO resend otp
        }

        verifyOtpBtn.setOnClickListener {
            // TODO verify otp
            findNavController().navigate(R.id.action_verifyOTPFragment_to_newAddressFormFragment)
            return@setOnClickListener
            val otp = otpInputView.getOtp()
            val token = ExpressSDKObject.getToken()
            if (otp.isEmpty() || otp.length < 5) {
                // set error to show correct otp
            } else {
                val customerId = ExpressSDKObject.getFetchData()?.customerInfo?.customerId
                    ?: ExpressSDKObject.getFetchData()?.customerInfo?.customer_id
                val otpRequest = OTPRequest(null, otp, customerId)
                val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
                if (customerInfo != null) {
                    customerInfo.email_id = customerInfo.emailId
                    customerInfo.mobile_number = customerInfo?.mobileNumber
                    customerInfo?.country_code = customerInfo?.countryCode
                    customerInfo?.is_edit_customer_details_allowed =
                        customerInfo?.isEditCustomerDetailsAllowed
                    customerInfo?.first_name = customerInfo?.firstName
                    customerInfo?.last_name = customerInfo?.lastName

                    val updateOrderDetails = UpdateOrderDetails(customerInfo)

                    val otpRequest = OTPRequest(null, otp, customerId, otpId, updateOrderDetails)
                    viewModel.validateUpdateOrderDetails(token, otpRequest)
                } else {
                    val otpRequest = OTPRequest(null, otp, customerId, otpId, null)
                    viewModel.submitOTP(token, otpRequest)
                }

            }
        }
    }

    private fun startResendOtpTimer() {
        resendTimer = TimerManager
        resendTimer.startTimer(120 * 60)
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

    private fun showProcessPaymentDialog() {
        bottomSheetDialog = Utils.showProcessPaymentDialog(
            requireContext()
        )
    }

    private fun sendOTP() {
        val customerId = ExpressSDKObject.getFetchData()?.customerInfo?.customerId
            ?: ExpressSDKObject.getFetchData()?.customerInfo?.customer_id
        val otpRequest = OTPRequest(null, null, customerId, null, null)
        viewModel.requestOTP(otpRequest)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.requestOTPResult.collect { result ->
                    when (result) {
                        is BaseResult.Error -> {
                            result.errorCode.let { exception ->
                                Log.e("Error", exception)
                            }

                        }

                        is BaseResult.Success<SavedCardResponse> -> {
                            result.data.let { it ->
                                //  otp sent successfully
                                otpId = it.otpId
                                bottomSheetDialog?.dismiss()
                            }
                        }

                        is BaseResult.Loading -> {
                            // handle loading
                            Log.d("Loading", "Loading data...")
                            result.isLoading
                        }
                    }
                }
            }
        }

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

                        is BaseResult.Success<SavedCardResponse> -> {
                            result.data.let { it ->
                                //  otp sent successfully
                                bottomSheetDialog?.dismiss()
                            }
                        }

                        is BaseResult.Loading -> {
                            // handle loading
                            Log.d("Loading", "Loading data...")
                            result.isLoading
                        }
                    }
                }
            }
        }
    }
}