package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.D2CViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.data.model.Address

class NewAddressFormFragment : Fragment() {

    private val viewModel: D2CViewModel by activityViewModels {
        D2CViewModelFactory(NetworkHelper(requireContext()))
    }

    private lateinit var fullNameEt: EditText
    private lateinit var pinCodeEt: EditText
    private lateinit var cityEt: EditText
    private lateinit var stateEt: EditText
    private lateinit var addressLine1Et: EditText
    private lateinit var addressLine2Et: EditText
    private lateinit var addressSaveDescriptionHyperLink: TextView
    private lateinit var addressType: RadioGroup
    private lateinit var saveBtn: Button
    private var selectedAddressType: String = "Home" // Default value
    private var isEditMode: Boolean = false // Flag to check if it's in edit mode

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_address_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
        if (bundle != null) {
            isEditMode = bundle.getBoolean("isEditAddress", false)
        }
        setViews(view)
        if (isEditMode) {
            val existingAddress = ExpressSDKObject.getSelectedAddress()
            // Populate the fields with existing address data if in edit mode
            // For example, you can set the text of EditTexts with existing address values
            fullNameEt.setText(existingAddress?.full_name)
            pinCodeEt.setText(existingAddress?.pincode)
            cityEt.setText(existingAddress?.city)
            stateEt.setText(existingAddress?.state)
            addressLine1Et.setText(existingAddress?.address1)
            addressLine2Et.setText(existingAddress?.address2)
            when (existingAddress?.address_type) {
                "Home" -> addressType.check(R.id.home_radio_button)
                "Work" -> addressType.check(R.id.work_radio_button)
                "Other" -> addressType.check(R.id.other_radio_button)
                else -> addressType.check(R.id.home_radio_button) // Default to Home if no match
            }
        }
    }


    private fun setSaveAddressHyperLinkForTerms() {
        val textView = addressSaveDescriptionHyperLink
        val fullText = getString(R.string.securely_save_as_per_plural_s_terms_and_privacy_policy)
        val spannableString = SpannableString(fullText)

        val termsText = getString(R.string.terms) // "Terms"
        val termsStart = fullText.indexOf(termsText)
        if (termsStart != -1) {
            val termsEnd = termsStart + termsText.length
            val termsClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, "https://www.example.com/terms".toUri())
                    startActivity(browserIntent)
                }
            }
            spannableString.setSpan(
                termsClickableSpan,
                termsStart,
                termsEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val privacyText = getString(R.string.privacy_policy) // "Privacy Policy"
        val privacyStart = fullText.indexOf(privacyText)
        if (privacyStart != -1) {
            val privacyEnd = privacyStart + privacyText.length
            val privacyClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Custom action for "Privacy Policy" click
                    // Optionally, open a URL
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, "https://www.example.com/privacy".toUri())
                    startActivity(browserIntent)
                }
            }
            spannableString.setSpan(
                privacyClickableSpan,
                privacyStart,
                privacyEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setViews(view: View) {
        addressSaveDescriptionHyperLink = view.findViewById(R.id.save_address_description)
        fullNameEt = view.findViewById(R.id.full_name_edit_text)
        pinCodeEt = view.findViewById(R.id.pin_code_edit_text)
        cityEt = view.findViewById(R.id.city_edit_text)
        stateEt = view.findViewById(R.id.state_edit_text)
        addressLine1Et = view.findViewById(R.id.address_line_1_edit_text)
        addressLine2Et = view.findViewById(R.id.address_line_2_edit_text)
        addressType = view.findViewById(R.id.address_type_radio_group)
        saveBtn = view.findViewById(R.id.continue_btn)
        view.findViewById<RadioButton>(R.id.home_radio_button).isChecked = true // Default selection
        addressType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.home_radio_button -> selectedAddressType = "Home"
                R.id.work_radio_button -> selectedAddressType = "Work"
                R.id.other_radio_button -> selectedAddressType = "Other"
            }
        }
        setSaveAddressHyperLinkForTerms()
        setFocusChangeListeners()
        saveBtn.setOnClickListener {
            if (isAllFieldValid()) {
                val address = Address(
                    full_name = fullNameEt.text.toString().trim(),
                    pincode = pinCodeEt.text.toString().trim(),
                    city = cityEt.text.toString().trim(),
                    state = stateEt.text.toString().trim(),
                    address1 = addressLine1Et.text.toString().trim(),
                    address2 = addressLine2Et.text.toString().trim(),
                    address_type = selectedAddressType,
                    address_category = "D2C",
                    country = "India",
                )
                viewModel.saveAddress(address)
            } else {
                // Show error message
            }
        }
    }


    private fun setFocusChangeListeners() {
        fullNameEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                fullNameEt.error = null
            } else if (fullNameEt.text.isEmpty()) {
                //show error
            }
        }
        pinCodeEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                pinCodeEt.error = null
            } else if (pinCodeEt.text.isEmpty()) {
                //show error
            }
        }
        cityEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                cityEt.error = null
            } else if (cityEt.text.isEmpty()) {
                //show error
            }
        }
        stateEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                stateEt.error = null
            } else if (stateEt.text.isEmpty()) {
                //show error
            }
        }
        addressLine1Et.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                addressLine1Et.error = null
            } else if (addressLine1Et.text.isEmpty()) {
                //show error
            }
        }
        addressLine2Et.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                addressLine2Et.error = null
            } else if (addressLine2Et.text.isEmpty()) {
                //show error
            }
        }
    }

    private fun isAllFieldValid(): Boolean {
        return fullNameEt.text.isNotEmpty() &&
                pinCodeEt.text.isNotEmpty() &&
                cityEt.text.isNotEmpty() &&
                stateEt.text.isNotEmpty() &&
                addressLine1Et.text.isNotEmpty() &&
                addressLine2Et.text.isNotEmpty()

    }


}