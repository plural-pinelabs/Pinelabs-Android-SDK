package com.plural_pinelabs.expresscheckoutsdk.presentation.upi

import UpiAppsAdapter
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject.getAmount
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject.getCurrency
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BHIM_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CRED_UPI
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.GPAY
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAYTM
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PHONEPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_INTENT
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_INTENT_PREFIX
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiData
import com.plural_pinelabs.expresscheckoutsdk.data.model.UpiTransactionData


class UPIFragment : Fragment() {

    private lateinit var payByAnyUPIButton: TextView
    private lateinit var upiAppsRv: RecyclerView
    private lateinit var upiIdEt: EditText
    private lateinit var verifyContinueButton: Button
    private val UPI_REGEX = Regex("^[\\w.]{1,}-?[\\w.]{0,}-?[\\w.]{1,}@[a-zA-Z]{2,}$")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_u_p_i, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        setUpPayByUPIApps()
        setupUPIIdValidation()
        observeViewModel()
    }

    private fun observeViewModel() {
    }

    private fun setViews(view: View) {
        payByAnyUPIButton = view.findViewById(R.id.pay_by_any_upi)
        upiAppsRv = view.findViewById(R.id.upi_app_rv)
        verifyContinueButton = view.findViewById(R.id.verify_and_continueButton)
        upiIdEt = view.findViewById(R.id.upi_id_edittext)

        payByAnyUPIButton.setOnClickListener {
            payAction(null, UPI_INTENT)
            // Handle the click event for the "Pay by Any UPI" button
        }
        verifyContinueButton.setOnClickListener {
            // Handle the click event for the "Verify and Continue" button
            // You can add your logic here, such as navigating to another fragment or performing an action
        }

    }

    private fun setUpPayByUPIApps() {
        val installedUPIApps = getUpiAppsInstalledInDevice()
        if (installedUPIApps.isNotEmpty()) {
            payByAnyUPIButton.visibility = View.VISIBLE
            upiAppsRv.visibility = View.VISIBLE
            upiAppsRv.layoutManager = GridLayoutManager(requireContext(), 2)
            upiAppsRv.adapter = UpiAppsAdapter(installedUPIApps)

        } else {
            payByAnyUPIButton.visibility = View.GONE
            upiAppsRv.visibility = View.GONE
        }

    }

    private fun getUpiAppsInstalledInDevice(): List<String> {
        val listOfUPIPackage = listOf(
            GPAY, // Google Pay
            PHONEPE, // PhonePe
            PAYTM,
            CRED_UPI,
            BHIM_UPI// Paytm
        )
        val listOfPaymentReadyApps = mutableListOf<String>()
        //  listOfPaymentReadyApps.addAll(getListOfActiveUPIApps(listOfUPIPackage)) TODO uncomment this before going QA
        //  return listOfPaymentReadyApps
        return listOfUPIPackage
    }

    private fun getListOfActiveUPIApps(listOfUPIPackage: List<String>): List<String> {
        try { //Keeping a try-catch block to avoid crashes if no UPI apps are installed
            val upiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(UPI_INTENT_PREFIX))
            val finalUpiAppsList = mutableListOf<String>()
            val pm = requireActivity().packageManager
            val upiActivities: List<ResolveInfo> = pm.queryIntentActivities(upiIntent, 0)
            val packageNamesOfAllInstalledApps = mutableListOf<String>()
            for (app in upiActivities) {
                packageNamesOfAllInstalledApps.add(app.activityInfo.packageName.lowercase())
            }
            for (app in listOfUPIPackage) {
                if (packageNamesOfAllInstalledApps.contains(app.lowercase())) {
                    finalUpiAppsList.add(app)
                }
            }
            return finalUpiAppsList
        } catch (_: Exception) {
            // If no UPI apps are installed, return an empty list
            return emptyList()
        }
    }

    private fun setupUPIIdValidation() {
        upiIdEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val isValidUPI = UPI_REGEX.matches(s.toString())

                // Enable/disable button and change color based on validation
                // verifyContinueButton.background = AppCompatResources.getDrawable(requireContext(),R.drawable.primary_button_background)

                verifyContinueButton.isEnabled = isValidUPI
                if (verifyContinueButton.isEnabled) {
                    verifyContinueButton.alpha = 1F
                } else {
                    verifyContinueButton.alpha = 0.3F
                }
                //val color = if (isValidUPI) R.color.colorSecondary else R.color.colorPrimary
            }
        })
    }

    private fun showUpiTray(deepLink: String, upiAppPackageName: String?) {
        val upiPayIntent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.data = deepLink.toUri()
        if (upiAppPackageName != null) {
            upiPayIntent.`package` = upiAppPackageName
        }
        val chooser = Intent.createChooser(upiPayIntent, getString(R.string.upi_open_with))
        // check if intent resolves
        if (null != chooser.resolveActivity(requireActivity().packageManager)) {
            startActivity(chooser) // Launch the chooser
        } else {
            Toast.makeText(
                requireActivity(),
                "No UPI app found, please install one to continue",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun payAction(
        vpa: String? = null,
        transactionMode: String?,
    ) {
        val paymentMode = arrayListOf(UPI_ID)
        val extra = Extra(
            paymentMode,
            getAmount(),
            getCurrency(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Utils.createSDKData(requireActivity())
        )
        val upiData = UpiData(UPI_ID, vpa, transactionMode)
        val upiTxnData = UpiTransactionData(10)
        val processPaymentRequest =
            ProcessPaymentRequest(
                null,
                null,
                null,
                upiData,
                null,
                null,
                extra,
                upiTxnData,
                null
            )
    }
}