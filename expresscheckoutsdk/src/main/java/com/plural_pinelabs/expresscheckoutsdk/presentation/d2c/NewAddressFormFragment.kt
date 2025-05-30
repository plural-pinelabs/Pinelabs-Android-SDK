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
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.plural_pinelabs.expresscheckoutsdk.R

class NewAddressFormFragment : Fragment() {
    private lateinit var addressSaveDescriptionHyperLink: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_address_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addressSaveDescriptionHyperLink = view.findViewById(R.id.save_address_description)
        //  setSaveAddressHyperLinkForTerms()
        findNavController().navigate(R.id.action_newAddressFormFragment_to_paymentModeFragment)
    }


    private fun setSaveAddressHyperLinkForTerms() {
        val textView = addressSaveDescriptionHyperLink

        // Get the full text from strings.xml (without HTML tags for the links)
        val fullText = getString(R.string.securely_save_as_per_plural_s_terms_and_privacy_policy)
        val spannableString = SpannableString(fullText)

        // Define the "Terms" link
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
        // This line is still crucial! It makes the ClickableSpans within the TextView clickable.
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}