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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.NewAddressFragmentViewModelFactory

class NewAddressFormFragment : Fragment() {
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

    private lateinit var viewModel: NewAddressFragmentViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(
            this,
            NewAddressFragmentViewModelFactory(NetworkHelper(requireContext()))
        )[NewAddressFragmentViewModel::class.java]
        return inflater.inflate(R.layout.fragment_new_address_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      //  setViews(view)

         findNavController().navigate(R.id.action_newAddressFormFragment_to_paymentModeFragment)
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
            // Validate fields and save address
            if (isAllFieldValid()) {
            //    val request = getSaveAddressRequest()

                // Proceed with saving the address
                // You can call a method in your ViewModel to handle the save operation
                // viewModel.saveAddress(...)
            } else {
                // Show error message
            }
        }
    }

//    private fun getSaveAddressRequest(): Any {
//
//
//    }

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