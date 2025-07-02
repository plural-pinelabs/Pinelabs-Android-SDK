package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.Constants
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_IMAGES
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BROWSER_ACCEPT_ALL
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BROWSER_USER_AGENT_ANDROID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EMI_DC_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ISSUE_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TENURE_ID
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.KFSWebView
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.PdfActivity
import com.plural_pinelabs.expresscheckoutsdk.common.TenureSelectionViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.MTAG
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.customSorted
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.markBestValueInPlace
import com.plural_pinelabs.expresscheckoutsdk.data.model.DeviceInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.EMIPaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.EmiData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetails
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.Locale

class TenureSelectionFragment : Fragment() {

    private lateinit var goBackButton: ImageView
    private lateinit var tenureListRecyclerView: RecyclerView
    private lateinit var emiPayingInMonthsTv: TextView
    private lateinit var logo: ImageView
    private lateinit var selectedBanKTitle: TextView
    private lateinit var continueBtn: Button
    private lateinit var footerLayout: LinearLayout
    private lateinit var saveInfoText: TextView

    private var selectedIssuerId: String? = null
    private var issuer: Issuer? = null
    private var emiPaymentModeData: EMIPaymentModeData? = null
    private var selectedTenure: Tenure? = null
    private lateinit var bankLogoMap: HashMap<String, String>
    private lateinit var banKTitleToCodeMap: HashMap<String, String>
    private lateinit var bankNameKeyList: List<String>
    private var bottomSheetDialog: BottomSheetDialog? = null

    private lateinit var viewModel: TenureSelectionViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(
            this,
            TenureSelectionViewModelFactory(NetworkHelper(requireContext()))
        )[TenureSelectionViewModel::class.java]
        return inflater.inflate(R.layout.fragment_tenure_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        mapBanKLogo()
        setEMIIssuer()
        setViews(view)
        showListOfTenure()
    }

    private fun setEMIIssuer() {
        emiPaymentModeData = ExpressSDKObject.getEMIPaymentModeData()
        selectedIssuerId = arguments?.getString(TENURE_ID)
        selectedIssuerId?.let {
            emiPaymentModeData?.issuers?.filter {
                it.id == selectedIssuerId
            }?.let { issuerList ->
                if (issuerList.isNotEmpty()) {
                    issuer = issuerList[0]
                }
            }
        }
    }

    private fun setViews(view: View) {
        goBackButton = view.findViewById(R.id.back_button)
        tenureListRecyclerView = view.findViewById(R.id.tenure_list_rv)
        emiPayingInMonthsTv = view.findViewById(R.id.emi_info_text)
        selectedBanKTitle = view.findViewById(R.id.issuer_title)
        logo = view.findViewById(R.id.logo)
        continueBtn = view.findViewById(R.id.continue_btn)
        footerLayout = view.findViewById(R.id.footer)
        saveInfoText = view.findViewById(R.id.save_info_text)
        loadBankLogo()
        continueBtn.setOnClickListener {
            handleContinueButtonClick()
        }
        goBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        selectedBanKTitle.text = Utils.getTitleForEMI(requireContext(), issuer)
    }

    private fun loadBankLogo() {
        issuer?.let { item ->
            val imageTitle =
                bankNameKeyList.find {
                    it.contains(
                        item.display_name.removeSuffix(" BANK"),
                        ignoreCase = true
                    )
                }

            if (imageTitle != null) {
                val imageUrl = BASE_IMAGES + bankLogoMap[banKTitleToCodeMap[imageTitle]]
                val imageLoader = ImageLoader.Builder(requireContext())
                    .components {
                        add(SvgDecoder.Factory())
                    }
                    .crossfade(true)
                    .build()
                val request = ImageRequest.Builder(requireContext())
                    .data(imageUrl)
                    .target(logo)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
                imageLoader.enqueue(request)
            }

        }
    }

    private fun showListOfTenure() {
        tenureListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val list = issuer?.tenures?.sortedBy { it.tenure_value }
        list?.let {
            if (it.isEmpty()) {
                return // TODO handle error no tenure in there
            }
            val filteredList = it.filter { tenure ->
                tenure.tenure_value != 0 || !tenure.name.contains("No EMI", true)
            }.customSorted()
            val adapter = EMITenureListAdapter(
                requireContext(), filteredList.markBestValueInPlace(), getTenureClickListener()
            )
            tenureListRecyclerView.adapter = adapter
        }
    }

    private fun getTenureClickListener(): ItemClickListener<Tenure?> {
        return object : ItemClickListener<Tenure?> {
            override fun onItemClick(position: Int, item: Tenure?) {
                //TODO handle click of wallet item
                selectedTenure = item
                emiPayingInMonthsTv.text =
                    String.format(
                        getString(R.string.paying_in_emi_of_x_months),
                        item?.tenure_value.toString()
                    )
                continueBtn.text = String.format(
                    getString(R.string.pay_emi_x_per_month),
                    Utils.convertToRupees(requireContext(), item?.monthly_emi_amount?.value)
                )
                footerLayout.visibility = View.VISIBLE
                val amount = Utils.convertInRupees(
                    (item?.total_discount_amount?.value
                        ?: 0) + (item?.total_subvention_amount?.value ?: 0)
                )
                saveInfoText.text = String.format(
                    getString(R.string.you_re_saving_x_on_this_order), amount.toString()
                )
            }
        }
    }

    private fun handleContinueButtonClick() {
        if (selectedTenure == null) {
            // TODO handle error no tenure selected
            return
        }
        if (issuer?.issuer_type?.equals(EMI_DC_TYPE) == true) {
            //user has choosen a Debit card show kfs and once consent is given navigate to card details
            viewModel.getKFS(ExpressSDKObject.getToken(), createProcessPaymentRequest())
        } else {
            val bundle = Bundle()
            bundle.putString(ISSUE_ID, selectedIssuerId)
            bundle.putString(TENURE_ID, selectedTenure?.tenure_id)
            findNavController().navigate(
                R.id.action_tenureSelectionFragment_to_EMICardDetailsFragment,
                bundle
            )
        }
    }

    private fun mapBanKLogo() {
        bankLogoMap = Utils.getBankLogoHashMap()
        bankNameKeyList = Utils.getListOfBanKTitle()
        banKTitleToCodeMap = Utils.bankTitleAndCodeMapper()
    }


    private fun observeViewModel() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.kfsRequestResult.collect { result ->
                    when (result) {
                        is BaseResult.Loading -> {
                            if (result.isLoading)
                                showKFSConsentBottomSheet(requireContext(), null, false)
                        }

                        is BaseResult.Success -> {
                            val url = result.data.key_fact_pdf_url
                            showKFSConsentBottomSheet(requireContext(), url, false)
                        }

                        is BaseResult.Error -> {
                            showKFSConsentBottomSheet(requireContext(), null, true)
                        }
                    }
                }
            }
        }
    }

    private fun createProcessPaymentRequest(): ProcessPaymentRequest {
        val tenureId = selectedTenure?.tenure_id ?: ""
        val offerDetails =
            ExpressSDKObject.getEMIPaymentModeData()?.offerDetails?.find { it.issuerId == selectedIssuerId }
        val offerTenure = offerDetails?.tenureOffers?.find { it.tenureId == tenureId }


        val paymentData = ExpressSDKObject.getFetchData()?.paymentData
        if (paymentData == null) {
            //TODO notify of payment failure
            findNavController().navigate(R.id.action_EMICardDetailsFragment_to_failureFragment)
        }
        val amount = issuer?.tenures?.find { it.tenure_id == tenureId }?.loan_amount?.value
            ?: 0
        val currency = paymentData?.originalTxnAmount?.currency


        val paymentMode = arrayListOf<String>()
        paymentMode.add(Constants.DEBIT_EMI_ID)
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        val pixelFormat = requireActivity().windowManager.defaultDisplay.pixelFormat

        val screenSize: String = width.toString() + "x" + height.toString()
        val deviceInfo = DeviceInfo(
            null,
            BROWSER_USER_AGENT_ANDROID,
            BROWSER_ACCEPT_ALL,
            Locale.getDefault().language,
            height.toString(),
            width.toString(),
            Utils.getTimeOffset().toString(),
            screenSize,
            Utils.getColorDepth(pixelFormat).toString(),
            true,
            true,
            Utils.getDeviceId(requireActivity()),
            Utils.getLocalIpAddress().toString()
        )

        val cardDataExtra = Extra(
            paymentMode,
            amount,
            currency,
            null,
            null, //TODO redeemableAmount pass this from reward points api
            null,
            null,
            deviceInfo,
            null,
            null,// dccstatus pass this from dcc api call
            Utils.createSDKData(requireActivity()),
            order_amount = ExpressSDKObject.getAmount(),
            language = "ENGLISH"
        )
        val emiData = EmiData(
            OfferDetails(
                id = issuer?.id,
                name = issuer?.name,
                display_name = issuer?.display_name,
                issuer_type = issuer?.issuer_type,
                priority = issuer?.priority,
                issuer_data = issuer?.issuer_data,
                label = offerTenure?.offerLabel,
                subventionType = offerTenure?.emiType,
                isMultiCartEmi = false,
                issuerName = issuer?.name ?: "",
                isSplitEmi = false,
                tenure = issuer?.tenures?.find { it.tenure_id == tenureId },
                tenures = issuer?.tenures
            )
        )
        val processPaymentRequest = ProcessPaymentRequest(
            null,
            null,
            null,
            upi_data = null,
            null,
            null,
            cardDataExtra,
            null,
            null,
            emi_data = emiData
        )
        return processPaymentRequest
    }

//    private fun showKFSConsentBottomSheet(context: Context, url: String?, isError: Boolean) {
//        bottomSheetDialog?.dismiss()
//        bottomSheetDialog = BottomSheetDialog(context)
//        val view = LayoutInflater.from(context).inflate(R.layout.kfs_view_layout, null, true)
//        bottomSheetDialog?.setContentView(view)
//
//        // ✅ Apply height and behavior after dialog is shown
//        bottomSheetDialog?.setOnShowListener { dialogInterface ->
//            val dialog = dialogInterface as BottomSheetDialog
//            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
//            bottomSheet?.let {
//                val behavior = BottomSheetBehavior.from(it)
//                val screenHeight = Resources.getSystem().displayMetrics.heightPixels
//                val desiredHeight = (screenHeight * 0.75).toInt()
//
//                val layoutParams = it.layoutParams
//                layoutParams.height = desiredHeight
//                it.layoutParams = layoutParams
//
//                behavior.peekHeight = desiredHeight
//                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
//                behavior.isFitToContents = false
//                behavior.skipCollapsed = true
//
//                it.setBackgroundColor(Color.WHITE) // ✅ Ensure solid background
//            }
//        }
//
//        // View bindings
//        val languageTextView: TextView = view.findViewById(R.id.kfs_language)
//        val downloadPDfButton: TextView = view.findViewById(R.id.download_pdf)
//        val cancelButton: ImageView = view.findViewById(R.id.cancel_btn)
//        val progressBarContainer: LinearLayout = view.findViewById(R.id.progress_bar_container)
//        val errorLayout: LinearLayout = view.findViewById(R.id.error_layout)
//        val retryButton: Button = view.findViewById(R.id.retry_btn)
//        val webView: WebView = view.findViewById(R.id.kfs_webview)
//        val consentCheckBox: CheckBox = view.findViewById(R.id.terms_checkbox_consent)
//        val continueButton: Button = view.findViewById(R.id.continue_btn)
//
//        var pdfUrl = ""
//        if (url != null) {
//            webView.visibility = View.VISIBLE
//            pdfUrl = "https://docs.google.com/gview?embedded=true&url=" + URLEncoder.encode(url, "UTF-8")
//        }
//
//        consentCheckBox.setOnCheckedChangeListener { _, isChecked ->
//            continueButton.isEnabled = isChecked
//        }
//
//        val kfsWebView = KFSWebView(
//            context = requireContext(),
//            webView = webView,
//            progressBarContainer = progressBarContainer,
//            errorView = errorLayout,
//            onScrollEnd = {
//                // Enable checkbox or other logic
//            }
//        )
//
//        bottomSheetDialog?.setCancelable(false)
//        bottomSheetDialog?.setCanceledOnTouchOutside(false)
//
//        // ❌ Removed window background override to prevent floating
//        // bottomSheetDialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
//
//        kfsWebView.loadUrl(pdfUrl)
//        bottomSheetDialog?.show()
//
//        languageTextView.setOnClickListener {
//            showLanguagePopup(languageTextView)
//        }
//
//        downloadPDfButton.setOnClickListener {
//            PdfDownloader(requireContext()).downloadPdf(pdfUrl)
//        }
//
//        cancelButton.setOnClickListener {
//            bottomSheetDialog?.dismiss()
//        }
//
//        retryButton.setOnClickListener {
//            kfsWebView.loadUrl(pdfUrl)
//        }
//
//        continueButton.setOnClickListener {
//            if (consentCheckBox.isChecked) {
//                bottomSheetDialog?.dismiss()
//                findNavController().navigate(R.id.action_tenureSelectionFragment_to_DCEMICardDetailsFragment)
//            }
//        }
//
//        if (isError) {
//            progressBarContainer.visibility = View.GONE
//            webView.visibility = View.GONE
//            errorLayout.visibility = View.VISIBLE
//        } else {
//            progressBarContainer.visibility = View.VISIBLE
//            webView.visibility = View.GONE
//            errorLayout.visibility = View.GONE
//        }
//    }


    private fun showKFSConsentBottomSheet(
        context: Context,
        url: String?,
        isError: Boolean
    ) {
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = BottomSheetDialog(context)

        val view = LayoutInflater.from(context).inflate(R.layout.kfs_view_layout, null)
        //        // View bindings
        val languageTextView: TextView = view.findViewById(R.id.kfs_language)
        val downloadPDfButton: TextView = view.findViewById(R.id.download_pdf)
        val cancelButton: ImageView = view.findViewById(R.id.cancel_btn)
        val progressBarContainer: LinearLayout = view.findViewById(R.id.progress_bar_container)
        val errorLayout: LinearLayout = view.findViewById(R.id.error_layout)
        val retryButton: Button = view.findViewById(R.id.retry_btn)
        val webView: WebView = view.findViewById(R.id.kfs_webview)
        val consentCheckBox: CheckBox = view.findViewById(R.id.terms_checkbox_consent)
        val continueButton: Button = view.findViewById(R.id.continue_btn)
        bottomSheetDialog?.setCancelable(false)
        bottomSheetDialog?.setCanceledOnTouchOutside(false)
        bottomSheetDialog?.setContentView(view)

        val bottomSheet =
            bottomSheetDialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {

            val behavior = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            val displayMetrics = Resources.getSystem().displayMetrics
            val screenHeight = displayMetrics.heightPixels
            layoutParams.height = (screenHeight * 0.95).toInt()
            it.layoutParams = layoutParams
            behavior.expandedOffset =
                (screenHeight * 0.05).toInt() // Set expanded offset to 15% of screen height
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
            behavior.isFitToContents = false
            behavior.skipCollapsed = true
        }

        var pdfUrl = ""
        if (url != null) {
            webView.visibility = View.VISIBLE
            pdfUrl = "https://docs.google.com/gview?embedded=true&url=" + URLEncoder.encode(
                url,
                "UTF-8"
            )

        }

        consentCheckBox.setOnCheckedChangeListener { _, isChecked ->
            continueButton.isEnabled = isChecked
        }

        val kfsWebView = KFSWebView(
            context = requireContext(),
            webView = webView,
            progressBarContainer = progressBarContainer,
            errorView = errorLayout,
            onScrollEnd = {
                // Enable checkbox or other logic
                Log.i(MTAG, "Scroll reached ")
            }
        )

        bottomSheetDialog?.setCancelable(false)
        bottomSheetDialog?.setCanceledOnTouchOutside(false)

        kfsWebView.loadUrl(pdfUrl)
        bottomSheetDialog?.show()

        languageTextView.setOnClickListener {
            showLanguagePopup(languageTextView)
        }

        downloadPDfButton.setOnClickListener {
            //PdfDownloader(requireContext()).downloadPdf(pdfUrl)

            //TODO dummy code
            val intent = Intent(requireActivity(), PdfActivity::class.java)
            intent.putExtra("pdf_url", url)
            startActivity(intent)
        }

        cancelButton.setOnClickListener {
            bottomSheetDialog?.dismiss()
        }

        retryButton.setOnClickListener {
            kfsWebView.loadUrl(pdfUrl)
        }

        continueButton.setOnClickListener {
            if (consentCheckBox.isChecked) {
                bottomSheetDialog?.dismiss()
                val bundle = Bundle()
                bundle.putString(ISSUE_ID, selectedIssuerId)
                bundle.putString(TENURE_ID, selectedTenure?.tenure_id)
                findNavController().navigate(
                    R.id.action_tenureSelectionFragment_to_DCEMICardDetailsFragment,
                    bundle
                )
            }
        }

        if (isError) {
            progressBarContainer.visibility = View.GONE
            webView.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
        } else {
            progressBarContainer.visibility = View.VISIBLE
            webView.visibility = View.GONE
            errorLayout.visibility = View.GONE
        }

        bottomSheetDialog?.show() // Show the dialog first
    }


    private fun showLanguagePopup(anchor: TextView) {
        val languages = listOf("English", "Hindi", "Spanish", "French", "German")
        val popupMenu = PopupMenu(anchor.context, anchor)

        languages.forEachIndexed { index, language ->
            popupMenu.menu.add(0, index, index, language)
        }

        popupMenu.setOnMenuItemClickListener { item ->
            val selectedLanguage = languages[item.itemId]
            (anchor as? TextView)?.text = selectedLanguage
            //TODO pass this value when selecting the langugage
            true
        }

        popupMenu.show()
    }

}