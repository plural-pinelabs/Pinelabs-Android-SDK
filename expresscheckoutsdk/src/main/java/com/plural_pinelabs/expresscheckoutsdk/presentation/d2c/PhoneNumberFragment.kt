package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.clevertap.android.sdk.isNotNullAndBlank
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.D2CViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import kotlinx.coroutines.launch

class PhoneNumberFragment : Fragment() {
    private lateinit var phoneNumberEt: EditText
    private lateinit var countryCodeTv: TextView
    private lateinit var countryFlagImage: ImageView
    private lateinit var emailEt: EditText
    private lateinit var continueBtn: Button
    private lateinit var phoneNumberParentLayout: LinearLayout
    private lateinit var emailParentLayout: LinearLayout

    private var isPhoneNumberValid: Boolean = false
    private var isEmailValid: Boolean = false
    private var bottomSheetDialog: BottomSheetDialog? = null

    // In the first fragment of the flow
    private val viewModel: D2CViewModel by activityViewModels {
        D2CViewModelFactory(NetworkHelper(requireContext()))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phone_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView(view)
        setClickListeners()
        setUpPHoneNumberValidation()
        setUpEmailValidation()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.createInactiveUserResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            //TODO handle failure
                            bottomSheetDialog?.dismiss()
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading)
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())
                        }

                        is BaseResult.Success<CustomerInfo?> -> {
                            val customerId = it.data?.customer_id
                            viewModel.customerInfo = it.data
                            val otpRequest = OTPRequest(customerId = customerId)
                            viewModel.sendOtp(
                                token = ExpressSDKObject.getToken(),
                                request = otpRequest
                            )
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sendOTPResult.collect { result ->
                    when (result) {
                        is BaseResult.Loading -> {
                        }

                        is BaseResult.Success -> {
                            //RESULT IS SUCCESS
                            viewModel.otpId = result.data.otpId
                            bottomSheetDialog?.dismiss()
                            findNavController().navigate(R.id.action_phoneNumberFragment_to_verifyOTPFragment)

                        }

                        is BaseResult.Error -> {
                            bottomSheetDialog?.dismiss()

                        }
                    }
                }
            }
        }
    }

    private fun setUpEmailValidation() {
        emailEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                isEmailValid = s?.let {
                    it.length == 10 && Utils.isValidEmail(it.toString())
                } ?: false
                Utils.showRemoveErrorBackground(
                    requireContext(),
                    null,
                    R.drawable.input_field_border,
                    isEmailValid,
                    viewGroup = emailParentLayout
                )
                Utils.handleCTAEnableDisable(requireContext(), !isPhoneNumberValid, continueBtn)

            }
        })

        emailEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                Utils.showRemoveErrorBackground(
                    requireContext(),
                    null,
                    R.drawable.input_field_border,
                    false,
                    true,
                    viewGroup = emailParentLayout
                )
            } else {
                if (isEmailValid) {
                    Utils.showRemoveErrorBackground(
                        requireContext(),
                        null,
                        R.drawable.input_field_border,
                        isEmailValid,
                        viewGroup = emailParentLayout

                    )
                    Utils.handleCTAEnableDisable(requireContext(), !isPhoneNumberValid, continueBtn)

                }
            }
        }
    }

    private fun setUpPHoneNumberValidation() {
        phoneNumberEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
               onNumberChange(s)
            }
        })

        phoneNumberEt.setOnFocusChangeListener { _, hasFocus ->
           if (hasFocus) {
                Utils.showRemoveErrorBackground(
                    requireContext(),
                    null,
                    R.drawable.input_field_border,
                    false,
                    true,
                    viewGroup = phoneNumberParentLayout
                )
            } else {
                Utils.showRemoveErrorBackground(
                    requireContext(),
                    null,
                    R.drawable.input_field_border,
                    !isPhoneNumberValid,
                    false,
                    viewGroup = phoneNumberParentLayout
                )
                Utils.handleCTAEnableDisable(requireContext(), isPhoneNumberValid, continueBtn)

            }
        }
    }

    private fun setClickListeners() {
        continueBtn.setOnClickListener {
            if (!isPhoneNumberValid) {
                return@setOnClickListener
            }
            if (emailEt.text.isNotEmpty() && isEmailValid) {
                Utils.showRemoveErrorBackground(
                    requireContext(),
                    null,
                    R.drawable.input_field_border,
                    isEmailValid,
                    false,
                    viewGroup = emailParentLayout
                )
                return@setOnClickListener
            }

            ExpressSDKObject.setPhoneNumber(phoneNumberEt.text.toString())
            if ((ExpressSDKObject.getFetchData()?.customerInfo?.customerToken) != null
            ) {
                  findNavController().navigate(R.id.action_phoneNumberFragment_to_verifyOTPFragment)
            }
            else{
            viewModel.phoneNumber = phoneNumberEt.text.toString()
            viewModel.countryCode = "91"
            viewModel.email = emailEt.text.toString()
            val customerInfo = CustomerInfo(
                mobileNumber = viewModel.phoneNumber,
                countryCode = "91",
                emailId = emailEt.text.toString() // Assuming +91 as default country code since we are not using a country picker here for now
            )
                viewModel.createInactiveUser(ExpressSDKObject.getToken(), customerInfo)
            }
        }
    }

    private fun setView(view: View) {
        phoneNumberEt = view.findViewById(R.id.editTextMobileNumber)
        emailEt = view.findViewById(R.id.editTextEmail)
        countryCodeTv = view.findViewById(R.id.textViewCountryCode)
        countryFlagImage = view.findViewById(R.id.imageViewFlag)
        continueBtn = view.findViewById(R.id.continue_btn)
        phoneNumberParentLayout = view.findViewById(R.id.phone_parent_layout)
        emailParentLayout = view.findViewById(R.id.email_et_layout)
        val existingPhoneNumber = ExpressSDKObject.getFetchData()?.customerInfo?.mobileNo
        val email = ExpressSDKObject.getFetchData()?.customerInfo?.emailId
        if (existingPhoneNumber.isNotNullAndBlank()) {
            phoneNumberEt.text = Editable.Factory.getInstance().newEditable(existingPhoneNumber)
            onNumberChange(phoneNumberEt.text)
        }
        if (email.isNotNullAndBlank()) {
            emailEt.text = Editable.Factory.getInstance().newEditable(email)
        }

    }

    private fun onNumberChange(s: Editable?) {
        isPhoneNumberValid = s.toString()
            .isNotNullAndBlank() && s.toString().length == 10 && Utils.isValidPhoneNumber(s.toString())
        continueBtn.isEnabled = isPhoneNumberValid
        Utils.showRemoveErrorBackground(
            requireContext(),
            null,
            R.drawable.input_field_border,
            !isPhoneNumberValid,
            phoneNumberEt.hasFocus(),
            viewGroup = phoneNumberParentLayout
        )
        Utils.handleCTAEnableDisable(requireContext(), isPhoneNumberValid, continueBtn)
    }

}