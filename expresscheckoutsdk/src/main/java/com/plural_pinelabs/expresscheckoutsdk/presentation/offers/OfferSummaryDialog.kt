package com.plural_pinelabs.expresscheckoutsdk.presentation.offers

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.CleverTapUtil
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetail
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class OfferSummaryDialog : DialogFragment() {

    private lateinit var bankLogoMap: HashMap<String, String>
    private lateinit var banKTitleToCodeMap: HashMap<String, String>
    private lateinit var bankNameKeyList: List<String>
    private lateinit var allTv: TextView
    private lateinit var creditTv: TextView
    private lateinit var debitTv: TextView
    private var icbFloatingDialog: Dialog? = null
    private var icbStatusBottomSheetDialog: BottomSheetDialog? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.offer_summary_layout)

        val window = dialog.window
        window?.apply {
            val metrics = Resources.getSystem().displayMetrics
            val screenHeight = metrics.heightPixels
            val desiredHeight = (screenHeight * 0.90).toInt()
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, desiredHeight)
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setGravity(Gravity.BOTTOM)

        }
        setupUI(dialog.findViewById(R.id.offer_summary_parent_layout))


        return dialog
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI(view: View) {

        val offerRecyclerView = view.findViewById<RecyclerView>(R.id.offers_recycler_view)
        val closeIcon = view.findViewById<ImageView>(R.id.x_icon)
        allTv = view.findViewById(R.id.all_filter_chip)
        creditTv = view.findViewById(R.id.credit_filter_chip)
        debitTv = view.findViewById(R.id.debit_filter_chip)
        mapBanKLogo()


        val filters = listOf(allTv, creditTv, debitTv)

        // Set default selection
        allTv.isSelected = true
        filterData("", offerRecyclerView)
        val offerList = getListOfTenure("")
        allTv.text = "All (${offerList.size})"
        if (offerList.count { it.type.contains("CC_bank", true) } == 0) {
            creditTv.visibility = View.GONE
        }
        if (offerList.count { it.type.contains("DC_Bank", true) } == 0) {
            debitTv.visibility = View.GONE
        }
        creditTv.text = "Credit Cards (${offerList.count { it.type.contains("CC_bank", true) }})"
        debitTv.text = "Debit Cards (${offerList.count { it.type.contains("DC_Bank", true) }})"

        filters.forEach { view ->
            view.setOnClickListener {
                filters.forEach { it.isSelected = false }
                view.isSelected = true

                when (view.id) {
                    R.id.all_filter_chip -> {
                        filterData("", offerRecyclerView)
                        allTv.isSelected = true
                        //   allTv.setBackgroundResource(R.drawable.input_field_border)
                    }

                    R.id.credit_filter_chip -> {
                        filterData("CC_bank", offerRecyclerView)
                        creditTv.isSelected = true
                    }

                    R.id.debit_filter_chip -> {
                        filterData("DC_Bank", offerRecyclerView)
                        debitTv.isSelected = true
                    }
                }
            }
        }


        closeIcon.setOnClickListener {
            dismiss()
        }
    }


    private fun filterData(type: String, offerRecyclerView: RecyclerView) {
        Log.d("Filter", "Filtering for: $type")

        offerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = OfferRVAdapter(
            requireContext(), getListOfTenure(type), getItemListener(), bankLogoMap,
            bankNameKeyList,
            banKTitleToCodeMap,
        )
        offerRecyclerView.adapter = adapter
        // Your filtering logic here
    }

    private fun getItemListener(): ItemClickListener<OfferDetail> {
        return object : ItemClickListener<OfferDetail> {
            override fun onItemClick(position: Int, item: OfferDetail) {
                showIcbFloatingDialog(item)
            }
        }
    }

    private fun showIcbFloatingDialog(item: OfferDetail) {
        icbFloatingDialog?.dismiss()

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.upi_icb_floating_dialog_layout)
        dialog.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(
                (Resources.getSystem().displayMetrics.widthPixels * 0.88f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
        }

        val savingsAmount = getOfferSavingsAmount(item)
        val title = dialog.findViewById<TextView>(R.id.icb_floating_title)
        val subtitle = dialog.findViewById<TextView>(R.id.icb_floating_subtitle)
        val message = dialog.findViewById<TextView>(R.id.icb_floating_message)
        val okCta = dialog.findViewById<TextView>(R.id.icb_floating_ok_cta)
        val closeIcon = dialog.findViewById<ImageView>(R.id.icb_floating_close_icon)

        title.text = getString(R.string.upi_icb_floating_title)
        subtitle.text = if (savingsAmount > 0) {
            getString(
                R.string.upi_icb_floating_subtitle_amount,
                Utils.convertToRupeesWithSymobl(requireContext(), savingsAmount)
            )
        } else {
            getString(R.string.upi_icb_floating_subtitle)
        }
        message.text = getString(R.string.upi_icb_floating_message)

        closeIcon.setOnClickListener {
            dialog.dismiss()
        }
        okCta.setOnClickListener {
            dialog.dismiss()
            showIcbStatusBottomSheet(item)
        }

        dialog.show()
        icbFloatingDialog = dialog
    }

    private fun showIcbStatusBottomSheet(item: OfferDetail) {
        icbStatusBottomSheetDialog?.dismiss()
        val sheetDialog = BottomSheetDialog(requireContext())
        val sheetView =
            layoutInflater.inflate(R.layout.upi_icb_status_bottom_sheet_layout, null)
        sheetDialog.setContentView(sheetView)
        sheetDialog.show()
        icbStatusBottomSheetDialog = sheetDialog

        bindIcbBottomSheetState(sheetView, item, isApiCompleted = false)

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1500)
            if (!isAdded || icbStatusBottomSheetDialog?.isShowing != true) return@launch
            bindIcbBottomSheetState(sheetView, item, isApiCompleted = true)
        }
    }

    private fun bindIcbBottomSheetState(
        sheetView: View,
        item: OfferDetail,
        isApiCompleted: Boolean
    ) {
        val statusIcon = sheetView.findViewById<ImageView>(R.id.icb_status_icon)
        val statusTitle = sheetView.findViewById<TextView>(R.id.icb_status_title)
        val statusSubtitle = sheetView.findViewById<TextView>(R.id.icb_status_subtitle)
        val statusBannerText = sheetView.findViewById<TextView>(R.id.icb_status_banner_text)
        val statusFetchingText = sheetView.findViewById<TextView>(R.id.icb_status_fetching_text)
        val statusFoundText = sheetView.findViewById<TextView>(R.id.icb_status_found_text)
        val statusFoundRow = sheetView.findViewById<View>(R.id.icb_status_found_row)
        val statusTpapIcon = sheetView.findViewById<ImageView>(R.id.icb_status_vpa_tpap_icon)
        val statusBhimNoticeRow = sheetView.findViewById<View>(R.id.icb_status_bhim_notice_row)
        val closeIcon = sheetView.findViewById<ImageView>(R.id.icb_status_close_icon)
        val continueCta = sheetView.findViewById<TextView>(R.id.icb_status_continue_cta)
        val resolvedMobile = resolveCustomerMobile()
        val resolvedVpa = resolvePreferredVpa()
        val tpapIconRes = resolveTpapIconForVpa(resolvedVpa)

        statusFetchingText.text = getString(
            R.string.upi_icb_status_fetching_vpa,
            resolvedMobile ?: getString(R.string.upi_icb_status_unknown_mobile)
        )
        statusFoundText.text = buildFoundVpaText(resolvedVpa)
        if (tpapIconRes != null) {
            statusTpapIcon.setImageResource(tpapIconRes)
            statusTpapIcon.visibility = View.VISIBLE
        } else {
            statusTpapIcon.visibility = View.GONE
        }

        closeIcon.setOnClickListener {
            icbStatusBottomSheetDialog?.dismiss()
        }

        if (isApiCompleted) {
            statusIcon.setImageResource(R.drawable.ic_upi)
            statusTitle.text = getString(R.string.upi_icb_status_success_title)
            statusSubtitle.text = getString(R.string.upi_icb_status_success_subtitle)
            statusBannerText.text = getString(R.string.upi_icb_status_success_banner)
            statusFoundRow.visibility = View.VISIBLE
            statusBhimNoticeRow.visibility = if (isBhimVpa(resolvedVpa)) View.VISIBLE else View.GONE
            continueCta.visibility = View.VISIBLE
            continueCta.text = getString(R.string.upi_icb_status_continue_cta)
            continueCta.setOnClickListener {
                icbStatusBottomSheetDialog?.dismiss()
                proceedWithSelectedOffer(item)
            }
        } else {
            statusIcon.setImageResource(R.drawable.ic_upi)
            statusTitle.text = getString(R.string.upi_icb_status_pending_title)
            statusSubtitle.text = getString(R.string.upi_icb_status_pending_subtitle)
            statusBannerText.text = getString(R.string.upi_icb_status_pending_banner)
            statusFoundRow.visibility = View.GONE
            statusBhimNoticeRow.visibility = View.GONE
            continueCta.visibility = View.VISIBLE
            continueCta.text = getString(R.string.upi_icb_status_change_mobile_cta)
            continueCta.setOnClickListener {
                icbStatusBottomSheetDialog?.dismiss()
            }
        }
    }

    private fun buildFoundVpaText(vpa: String?): CharSequence {
        val prefix = getString(R.string.upi_icb_status_found_vpa_prefix)
        val value = vpa ?: getString(R.string.upi_icb_status_unknown_vpa)
        return SpannableStringBuilder().apply {
            append(prefix)
            val start = length
            append(value)
            setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(Color.parseColor("#003323")),
                start,
                length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun resolvePreferredVpa(): String? {
        return ExpressSDKObject.getFetchData()
            ?.customerInfo
            ?.lastUsedPaymode
            ?.upi
            ?.lastUsedVPAs
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun resolveCustomerMobile(): String? {
        val customerInfo = ExpressSDKObject.getFetchData()?.customerInfo
        return customerInfo?.mobile_number
            ?: customerInfo?.mobileNo
            ?: customerInfo?.mobileNumber
    }

    private fun resolveTpapIconForVpa(vpa: String?): Int? {
        val handle = vpa?.substringAfter('@', missingDelimiterValue = "")
            ?.lowercase()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        // Extend this mapping as API contracts for TPAP handles are finalized.
        return when (handle) {
            "upi" -> R.drawable.ic_bhim_upi
            else -> null
        }
    }

    private fun isBhimVpa(vpa: String?): Boolean {
        val handle = vpa?.substringAfter('@', missingDelimiterValue = "")
            ?.lowercase()
            ?.trim()
            ?: return false
        return handle == "upi"
    }

    private fun getOfferSavingsAmount(item: OfferDetail): Int {
        return if (item.isInstantSaving) {
            item.tenureOffers?.find { it.tenureId == "7" }
                ?.let { it.discountAmount + it.cashbackAmount } ?: 0
        } else {
            item.maxSaving
        }
    }

    private fun proceedWithSelectedOffer(item: OfferDetail) {
        ExpressSDKObject.setSelectedOfferDetail(item)
        dismiss()
        if (item.isInstantSaving) {
            findNavController().navigate(R.id.action_offerSummaryDialog_to_EMICardDetailsFragment)
        } else {
            findNavController().navigate(R.id.action_offerSummaryDialog_to_tenureSelectionFragment)
        }
        CleverTapUtil.sdkOfferApplied(
            CleverTapUtil.getInstance(requireContext()),
            ExpressSDKObject.getFetchData(),
            item.name,
            item.type,
            item.tenureOffers?.firstOrNull()?.offers?.firstOrNull()?.discount?.value.toString(),
            false,
            item.offerTitle ?: "",
            "offer_summary ${item.isInstantSaving}"
        )
    }

    override fun onDestroyView() {
        icbFloatingDialog?.dismiss()
        icbFloatingDialog = null
        icbStatusBottomSheetDialog?.dismiss()
        icbStatusBottomSheetDialog = null
        super.onDestroyView()
    }


    private fun getListOfTenure(type: String): ArrayList<OfferDetail> {
        val emiPaymentModeData = ExpressSDKObject.getEMIPaymentModeData()
        val offersList: ArrayList<OfferDetail> = arrayListOf()
        val offerDetails = if (type.isEmpty()) {
            emiPaymentModeData?.offerDetails
        } else {
            emiPaymentModeData?.offerDetails?.filter { it.type.contains(type, true) }
        }
        offerDetails?.forEach { offerDetail ->
            offerDetail.offerTitle =
                Utils.getTitleForEMI(requireContext(), offerDetail.issuer) + " EMI"
            offersList.add(offerDetail)
            val cashbackTenure = offerDetail.tenureOffers?.find { it.tenureId == "7" }
            if (cashbackTenure != null) {
                val cashBackOfferDetail = offerDetail.copy()
                cashBackOfferDetail.offerTitle =
                    Utils.getTitleForEMI(requireContext(), offerDetail.issuer)
                cashBackOfferDetail.isInstantSaving = true
                offersList.add(cashBackOfferDetail)
            }
        }
        return offersList.sortedByDescending { offerDetail ->
            val saving =
                if (offerDetail.isInstantSaving) {
                    offerDetail.tenureOffers?.find { it.tenureId == "7" }?.let {
                        it.discountAmount + it.cashbackAmount
                    } ?: 0
                } else {
                    offerDetail.maxSaving
                }
            saving
        }.let { ArrayList(it) }

    }


    private fun mapBanKLogo() {
        bankLogoMap = Utils.getBankLogoHashMap()
        bankNameKeyList = Utils.getListOfBanKTitle()
        banKTitleToCodeMap = Utils.bankTitleAndCodeMapper()
    }


}

