package com.plural_pinelabs.expresscheckoutsdk.presentation.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.OtpInputView
import com.plural_pinelabs.expresscheckoutsdk.common.SaveCardOTPFragmentViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.TimerManager
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpdateOrderDetails
import kotlinx.coroutines.launch

class SaveCardOTPFragment : Fragment() {
    private lateinit var verifyBtn: Button
    private lateinit var otpInputView: OtpInputView
    private lateinit var resendTimerView: TextView
    private lateinit var resendAction: TextView
    private lateinit var skipSaveCardActionView: TextView
    private lateinit var customerSendOtpDescription: TextView
    private lateinit var errorTv: TextView
    private var otpId: String? = null
    private var last4DigitsCard: String? = null

    private lateinit var viewModel: SavedCardOTPViewModel
    private var bottomSheetDialog: BottomSheetDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(
            this,
            SaveCardOTPFragmentViewModelFactory(NetworkHelper(requireContext()))
        )[SavedCardOTPViewModel::class.java]
        return inflater.inflate(R.layout.fragment_save_card_o_t_p, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        last4DigitsCard = arguments?.getString("last4Digits", "")
        setViews(view)
        observeViewModel()
        sendOTP()
    }

    private fun sendOTP() {
        val otpRequest = OTPRequest(
            customerId = ExpressSDKObject.getFetchData()?.customerInfo?.customerId
        )

        viewModel.generateOTP(ExpressSDKObject.getToken(), otpRequest)
    }

    private fun setViews(view: View) {
        verifyBtn = view.findViewById(R.id.continue_btn)
        otpInputView = view.findViewById(R.id.otp_input_view)
        resendTimerView = view.findViewById(R.id.resend_otp_tv)
        resendAction = view.findViewById(R.id.resend_otp_action)
        skipSaveCardActionView = view.findViewById(R.id.skip_card_saved_otp)
        customerSendOtpDescription = view.findViewById(R.id.customer_send_otp_description)
        errorTv = view.findViewById(R.id.error_view)
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }
        customerSendOtpDescription.text = String.format(
            getString(R.string.save_card_verify_msg),
            ExpressSDKObject.getFetchData()?.customerInfo?.mobileNo ?: "your number",
            last4DigitsCard?.toInt() ?: 0
        )

        verifyBtn.setOnClickListener {
            val otp = otpInputView.getOtp()
            if (otp.isNotEmpty() && otp.length == 6) {
                val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
                if (customerInfo != null) {
//                    customerInfo.email_id = customerInfo.emailId
//                    customerInfo.mobile_number = customerInfo.mobileNumber
//                    customerInfo.country_code = customerInfo.countryCode
//                    customerInfo.is_edit_customer_details_allowed =
//                        customerInfo.isEditCustomerDetailsAllowed
//                    customerInfo.first_name = customerInfo.firstName
//                    customerInfo.last_name = customerInfo.lastName

                    val updateOrderDetails = UpdateOrderDetails(CustomerInfo(customerId =customerInfo.customer_id?:customerInfo.customerId ))
                    val otpRequest = OTPRequest(
                        null,
                        otp,
                        customerInfo.customerId,
                        otpId,
                        updateOrderDetails
                    )
                    viewModel.validateUpdateOrderDetails(ExpressSDKObject.getToken(), otpRequest)
                } else {
                    val otpRequest = OTPRequest(
                        null,
                        otp,
                        ExpressSDKObject.getFetchData()?.customerInfo?.customer_id,
                        otpId,
                        null
                    )
                    viewModel.submitOtp(ExpressSDKObject.getToken(), otpRequest)
                }
            }
        }

        resendAction.setOnClickListener {
            sendOTP()
        }
        skipSaveCardActionView.setOnClickListener {
            passResultBackForSuccessFailure(false)
        }
    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.sendOTPResult.collect { result ->
                when (result) {
                    is BaseResult.Loading -> {
                        if (result.isLoading) {
                            bottomSheetDialog = Utils.showProcessPaymentDialog(requireContext())
                        }
                    }

                    is BaseResult.Success -> {
                        otpId = result.data.otpId
                        showResendTimer()
                        bottomSheetDialog?.dismiss()
                    }

                    is BaseResult.Error -> {
                        bottomSheetDialog?.dismiss()
                        passResultBackForSuccessFailure(false)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.verifyOTPResult.collect { result ->
                when (result) {
                    is BaseResult.Loading -> {
                        if (result.isLoading) {
                            bottomSheetDialog = Utils.showProcessPaymentDialog(requireContext())
                        }
                        // Show loading state
                    }

                    is BaseResult.Success -> {
                        if (result.data.status.equals("NOT_VALIDATED", true)) {
                            bottomSheetDialog?.dismiss()
                            errorTv.visibility = View.VISIBLE
                        } else {
                            errorTv.visibility = View.GONE
                            passResultBackForSuccessFailure(true)
                        }
                        // Handle success
                    }

                    is BaseResult.Error -> {
                        // Handle error
                        passResultBackForSuccessFailure(false)
                        bottomSheetDialog?.dismiss()
                    }
                }
            }
        }
    }

    private fun passResultBackForSuccessFailure(isSuccess: Boolean) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set("success", isSuccess)
        findNavController().popBackStack()
    }

    private fun showResendTimer() {
        val timer = TimerManager
        timer.startTimer(120000)
        timer.timeLeft.observe(viewLifecycleOwner) {
            val timeLeft = Utils.formatTimeInMinutes(requireContext(), it)
            if (it <= 0) {
                resendAction.visibility = View.VISIBLE
                resendTimerView.visibility = View.GONE
            } else {
                resendAction.visibility = View.GONE
                resendTimerView.visibility = View.VISIBLE
                resendTimerView.text = String.format(getString(R.string.resend_otp_in), timeLeft)
            }
        }
    }
}