package com.plural_pinelabs.expresscheckoutsdk.presentation.emi

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.plural_pinelabs.expresscheckoutsdk.common.CleverTapUtil
import com.plural_pinelabs.expresscheckoutsdk.common.Constants
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BASE_IMAGES
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BROWSER_ACCEPT_ALL
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.BROWSER_USER_AGENT_ANDROID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.EMI_DC_TYPE
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.ISSUE_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.TENURE_ID
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.PdfDownloader
import com.plural_pinelabs.expresscheckoutsdk.common.TenureSelectionViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.customSorted
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.markBestValueInPlace
import com.plural_pinelabs.expresscheckoutsdk.data.model.Amount
import com.plural_pinelabs.expresscheckoutsdk.data.model.ConvenienceFeesInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.DeviceInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.EMIPaymentModeData
import com.plural_pinelabs.expresscheckoutsdk.data.model.EmiData
import com.plural_pinelabs.expresscheckoutsdk.data.model.Extra
import com.plural_pinelabs.expresscheckoutsdk.data.model.Issuer
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetails
import com.plural_pinelabs.expresscheckoutsdk.data.model.ProcessPaymentRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.Tenure
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import com.plural_pinelabs.expresscheckoutsdk.presentation.ordersummary.TopSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.closeQuietly
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
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
    private lateinit var viewEMIDetails: TextView

    private var selectedIssuerId: String? = null
    private var issuer: Issuer? = null
    private var emiPaymentModeData: EMIPaymentModeData? = null
    private var selectedTenure: Tenure? = null
    private lateinit var bankLogoMap: HashMap<String, String>
    private lateinit var banKTitleToCodeMap: HashMap<String, String>
    private lateinit var bankNameKeyList: List<String>
    private var bottomSheetDialog: BottomSheetDialog? = null

    private var pageIndex = 0
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor

    private lateinit var viewModel: TenureSelectionViewModel
    private var isFromOfferDetails: Boolean = false


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
        handleConvenienceFees(null)
    }

    private fun setEMIIssuer() {
        emiPaymentModeData = ExpressSDKObject.getEMIPaymentModeData()
        isFromOfferDetails = ExpressSDKObject.getSelectedOfferDetail() != null
        selectedIssuerId = arguments?.getString(ISSUE_ID)
        if (isFromOfferDetails) {
            // If coming from offer details, get the issuer from selected offer detail
            val selectedOfferDetail = ExpressSDKObject.getSelectedOfferDetail()
            selectedIssuerId = selectedOfferDetail?.issuerId ?: selectedIssuerId
        }
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
        viewEMIDetails = view.findViewById(R.id.emi_details_cta)
        loadBankLogo()
        continueBtn.setOnClickListener {
            handleContinueButtonClick()
        }
        goBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
        viewEMIDetails.setOnClickListener {
            val topFragment = TopSheetDialogFragment()
            topFragment.show(requireActivity().supportFragmentManager, "TopSheetDialogFragment")
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
                findNavController().navigate(R.id.action_tenureSelectionFragment_to_failureFragment)
                return
            }
            val filteredList =
                if (isFromOfferDetails) {
                    val tenureIds =
                        ExpressSDKObject.getSelectedOfferDetail()?.tenureOffers?.map { it.tenureId }
                    it.filter { tenure ->
                        tenureIds?.contains(tenure.tenure_id) == true && (tenure.tenure_value != 0 || !tenure.name.contains(
                            "No EMI",
                            true
                        ))
                    }.customSorted()

                } else {
                    it.filter { tenure ->
                        tenure.tenure_value != 0 || !tenure.name.contains("No EMI", true)
                    }.customSorted()
                }
            val adapter = EMITenureListAdapter(
                requireContext(),
                filteredList.markBestValueInPlace(selectedIssuerId),
                getTenureClickListener()
            )
            tenureListRecyclerView.adapter = adapter
        }
    }

    private fun getTenureClickListener(): ItemClickListener<Tenure?> {
        return object : ItemClickListener<Tenure?> {
            override fun onItemClick(position: Int, item: Tenure?) {
                handleConvenienceFees(item)
                selectedTenure = item
                ExpressSDKObject.setSelectedTenure(selectedTenure)
                emiPayingInMonthsTv.text =
                    String.format(
                        getString(R.string.paying_in_emi_of_x_months),
                        item?.tenure_value.toString()
                    )
                continueBtn.text = String.format(
                    getString(R.string.pay_emi_x_per_month),
                    Utils.convertToRupeesWithSymobl(
                        requireContext(),
                        item?.monthly_emi_amount?.value
                    )
                )
                footerLayout.visibility = View.VISIBLE
                val totalSaving = (item?.total_discount_amount?.value
                    ?: 0) + (item?.total_subvention_amount?.value ?: 0)
                if (totalSaving != 0) {
                    val amount = Utils.convertInRupees(totalSaving)
                    saveInfoText.text = String.format(
                        getString(R.string.you_re_saving_x_on_this_order), amount
                    )
                } else {
                    saveInfoText.visibility = View.GONE

                }
            }
        }
    }

    private fun handleContinueButtonClick() {
        if (selectedTenure == null) {
            return
        }
        if (issuer?.issuer_type?.equals(EMI_DC_TYPE) == true) {
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

        CleverTapUtil.emiOptionSelected(
            CleverTapUtil.getInstance(requireContext()),
            ExpressSDKObject.getFetchData(),
            selectedTenure?.tenure_value.toString(),
            selectedTenure?.interest_rate_percentage.toString(),
            selectedTenure?.subvention?.subvention_type ?: "",
            selectedTenure?.monthly_emi_amount?.amount.toString(),
            false
        )
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

    private fun downloadAndRenderPdf(pdfUrl: String, imageView: ImageView) {
        pageIndex = 0
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(pdfUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val file = File(requireActivity().cacheDir, "downloaded.pdf")
                val inputStream = BufferedInputStream(connection.inputStream)
                val outputStream = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var count: Int
                while (inputStream.read(buffer).also { count = it } != -1) {
                    outputStream.write(buffer, 0, count)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                withContext(Dispatchers.Main) {
                    openPdfRenderer(file)
                    showPage(0, imageView = imageView)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // TODO unable load pdf
                }
            }
        }
    }

    private fun openPdfRenderer(file: File) {
        try {
            parcelFileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
        } catch (e: Exception) {
            e.printStackTrace()
            //TODO error loading pdf
        }
    }

    @SuppressLint("UseKtx")
    private fun showPage(index: Int, imageView: ImageView) {
        // Close previous page if initialized
        if (::currentPage.isInitialized) {
            currentPage.close()
        }

        // Open and render new page
        currentPage = pdfRenderer.openPage(index)

        val bitmap = Bitmap.createBitmap(
            currentPage.width,
            currentPage.height,
            Bitmap.Config.ARGB_8888
        )
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        imageView.setImageBitmap(bitmap)
    }

    @SuppressLint("InflateParams")
    private fun showKFSConsentBottomSheet(
        context: Context,
        url: String?,
        isError: Boolean
    ) {
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
        bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.kfs_view_layout, null)
        //        // View bindings
        val languageTextView: TextView = view.findViewById(R.id.kfs_language)
        val downloadPDfButton: TextView = view.findViewById(R.id.download_pdf)
        val cancelButton: ImageView = view.findViewById(R.id.cancel_btn)
        val progressBarContainer: LinearLayout = view.findViewById(R.id.progress_bar_container)
        val errorLayout: LinearLayout = view.findViewById(R.id.error_layout)
        val retryButton: Button = view.findViewById(R.id.retry_btn)
        val consentCheckBox: CheckBox = view.findViewById(R.id.terms_checkbox_consent)
        val continueButton: Button = view.findViewById(R.id.continue_btn)

        val kfsImageView: ImageView = view.findViewById(R.id.kfs_image_view)
        val previousPage: ImageView = view.findViewById(R.id.prev_page)
        val nextPage: ImageView = view.findViewById(R.id.next_page)
        consentCheckBox.isEnabled = false

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
            kfsImageView.visibility = View.VISIBLE
            pdfUrl = url
        }

        consentCheckBox.setOnCheckedChangeListener { _, isChecked ->
            Utils.handleCTAEnableDisable(requireContext(), isChecked, continueButton)
        }
        bottomSheetDialog?.setCancelable(false)
        bottomSheetDialog?.setCanceledOnTouchOutside(false)
        bottomSheetDialog?.show()
        downloadAndRenderPdf(pdfUrl, kfsImageView)


        languageTextView.setOnClickListener {
            showLanguagePopup(languageTextView)
        }

        downloadPDfButton.setOnClickListener {
            PdfDownloader(requireContext()).downloadPdf(pdfUrl)
        }

        cancelButton.setOnClickListener {
            bottomSheetDialog?.dismiss()
        }

        retryButton.setOnClickListener {
            downloadAndRenderPdf(pdfUrl, kfsImageView)
            errorLayout.visibility = View.GONE
            kfsImageView.visibility = View.VISIBLE
            progressBarContainer.visibility = View.VISIBLE
        }

        previousPage.setOnClickListener {
            if (pageIndex > 0) {
                showPage(--pageIndex, kfsImageView)
            }
        }

        nextPage.setOnClickListener {
            if (pageIndex < pdfRenderer.pageCount - 1) {
                showPage(++pageIndex, kfsImageView)
            }
            if (pageIndex == pdfRenderer.pageCount - 1) {
                consentCheckBox.isEnabled = true
            }
        }

        continueButton.setOnClickListener {
            if (consentCheckBox.isChecked) {
                bottomSheetDialog?.dismiss()
                val authType = issuer?.issuer_data?.auth_type ?: ""
                val bundle = Bundle()
                bundle.putString(ISSUE_ID, selectedIssuerId)
                bundle.putString(TENURE_ID, selectedTenure?.tenure_id)
                if (authType.equals("OTP", true)) {
                    findNavController().navigate(
                        R.id.action_tenureSelectionFragment_to_DCEMICardDetailsFragment,
                        bundle
                    )
                } else {
                    findNavController().navigate(
                        R.id.action_tenureSelectionFragment_to_EMICardDetailsFragment,
                        bundle
                    )
                }

            }
        }

        if (isError) {
            progressBarContainer.visibility = View.GONE
            kfsImageView.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
        } else {
            //  progressBarContainer.visibility = View.VISIBLE
            kfsImageView.visibility = View.VISIBLE
            errorLayout.visibility = View.GONE
        }

        bottomSheetDialog?.show() // Show the dialog first
        bottomSheetDialog?.setOnDismissListener {

            try {
                if (::pdfRenderer.isInitialized) {
                    currentPage.close()
                    pdfRenderer.close()
                    parcelFileDescriptor
                    parcelFileDescriptor.closeQuietly()
                }
            } catch (_: Exception) {
                // TODO do nothing
            }
        }
    }


    private fun handleConvenienceFees(tenure: Tenure?) {
        val convenienceFeeBreakdown = tenure?.convenience_fee_breakdown
        if (convenienceFeeBreakdown == null) {
            (requireActivity() as LandingActivity).showHideConvenienceFessMessage(false)
            return
        }
        val convenienceFeesInfo = ConvenienceFeesInfo(
            paymentAmount = Amount(
                currency = tenure.loan_amount?.currency ?: "INR",
                amount = tenure.loan_amount?.value ?: 0,
                value = 0
            ),
            convenienceFeesGSTAmount = Amount(
                currency = convenienceFeeBreakdown.tax_amount?.currency ?: "INR",
                amount = convenienceFeeBreakdown.tax_amount?.value ?: 0,
                value = 0
            ),

            convenienceFeesAmount = Amount(
                currency = convenienceFeeBreakdown.fee_amount?.currency ?: "INR",
                amount = convenienceFeeBreakdown.fee_amount?.value ?: 0,
                value = 0
            ),
            convenienceFeesApplicableFeeAmount = Amount(
                currency = convenienceFeeBreakdown.applicable_fee_amount?.currency ?: "INR",
                amount = convenienceFeeBreakdown.applicable_fee_amount?.value ?: 0,
                value = 0
            ),
            convenienceFeesMaximumFeeAmount = Amount(
                currency = convenienceFeeBreakdown.maximum_fee_amount?.currency ?: "INR",
                amount = convenienceFeeBreakdown.maximum_fee_amount?.value ?: 0,
                value = 0
            ),
            originalTxnAmount = Amount(
                currency = convenienceFeeBreakdown.fee_calculated_on_amount?.currency ?: "INR",
                amount = convenienceFeeBreakdown.fee_calculated_on_amount?.value ?: 0,
                value = 0
            ),
            convenienceFeesAdditionalAmount = Amount(
                currency = convenienceFeeBreakdown.additional_fee_amount?.currency ?: "INR",
                amount = convenienceFeeBreakdown.additional_fee_amount?.value ?: 0,
                value = 0
            ),
        )
        viewModel.selectedConvenienceFee = convenienceFeesInfo
        (requireActivity() as LandingActivity).showHideConvenienceFessMessage(
            true,
            viewModel.selectedConvenienceFee
        )
    }

}