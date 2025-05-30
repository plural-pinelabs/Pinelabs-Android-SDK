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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.clevertap.android.sdk.isNotNullAndBlank
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Utils

class PhoneNumberFragment : Fragment() {
    private lateinit var phoneNumberEt: EditText
    private lateinit var countryCodeTv: TextView
    private lateinit var countryFlagImage: ImageView
    private lateinit var continueBtn: Button
    private var isPhoneNumberValid: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phone_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //TODO handle the country flag and country code dynamically
        setView(view)
        setClickListeners()
        setUpPHoneNumberValidation()
    }

    private fun setUpPHoneNumberValidation() {
        phoneNumberEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                isPhoneNumberValid = s?.let {
                    it.length == 10 && Utils.isValidPhoneNumber(it.toString())
                } ?: false
            }

        })
    }

    private fun setClickListeners() {
        continueBtn.setOnClickListener {
//            if (!isPhoneNumberValid) {
//                return@setOnClickListener
//            }
//            ExpressSDKObject.setPhoneNumber(phoneNumberEt.text.toString())
            findNavController().navigate(R.id.action_phoneNumberFragment_to_verifyOTPFragment)


            // if validated phone number move ahead
        }
    }

    private fun setView(view: View) {
        phoneNumberEt = view.findViewById(R.id.editTextMobileNumber)
        countryCodeTv = view.findViewById(R.id.textViewCountryCode)
        countryFlagImage = view.findViewById(R.id.imageViewFlag)
        continueBtn = view.findViewById(R.id.continue_btn)
        val existingPhoneNumber = ExpressSDKObject.getPhoneNumber()
        if (existingPhoneNumber.isNotNullAndBlank()) {
            phoneNumberEt.text = Editable.Factory.getInstance().newEditable(existingPhoneNumber)
        }
    }

}