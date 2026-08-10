package com.plural_pinelabs.expresscheckoutsdk.presentation.offers

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.common.Utils
import com.plural_pinelabs.expresscheckoutsdk.data.model.OfferDetail
import com.plural_pinelabs.expresscheckoutsdk.presentation.upi.resolveUpiOfferDetails


class OfferSummaryDialog : DialogFragment() {

    private lateinit var bankLogoMap: HashMap<String, String>
    private lateinit var banKTitleToCodeMap: HashMap<String, String>
    private lateinit var bankNameKeyList: List<String>
    private lateinit var allTv: TextView
    private lateinit var creditTv: TextView
    private lateinit var debitTv: TextView
    private lateinit var upiTv: TextView
    private var allOffers: List<OfferDetail> = emptyList()
    private val showUpiTag: Boolean
        get() = arguments?.getBoolean(ARG_SHOW_UPI_TAG, false) ?: false
    private var icbFloatingDialog: Dialog? = null

    private enum class OfferFilter {
        ALL,
        CREDIT_CARD,
        DEBIT_CARD,
        UPI,
    }


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
        upiTv = view.findViewById(R.id.upi_filter_chip)
        mapBanKLogo()
        allOffers = getOffersForDialog()

        val creditCount = allOffers.count { isCreditCardOffer(it) }
        val debitCount = allOffers.count { isDebitCardOffer(it) }
        val upiCount = allOffers.count { isUpiOffer(it) }

        allTv.text = getString(R.string.offer_filter_all_count, allOffers.size)
        creditTv.text = getString(R.string.offer_filter_credit_count, creditCount)
        debitTv.text = getString(R.string.offer_filter_debit_count, debitCount)
        upiTv.text = getString(R.string.offer_filter_upi_count, upiCount)

        creditTv.visibility = if (showUpiTag || creditCount == 0) View.GONE else View.VISIBLE
        debitTv.visibility = if (showUpiTag || debitCount == 0) View.GONE else View.VISIBLE
        upiTv.visibility = if (showUpiTag || upiCount == 0) View.GONE else View.VISIBLE

        val visibleFilters = mutableListOf(allTv)
        if (creditTv.visibility == View.VISIBLE) visibleFilters.add(creditTv)
        if (debitTv.visibility == View.VISIBLE) visibleFilters.add(debitTv)
        if (upiTv.visibility == View.VISIBLE) visibleFilters.add(upiTv)

        setSelectedFilterChip(visibleFilters, allTv)
        filterData(OfferFilter.ALL, offerRecyclerView)

        allTv.setOnClickListener {
            setSelectedFilterChip(visibleFilters, allTv)
            filterData(OfferFilter.ALL, offerRecyclerView)
        }
        creditTv.setOnClickListener {
            setSelectedFilterChip(visibleFilters, creditTv)
            filterData(OfferFilter.CREDIT_CARD, offerRecyclerView)
        }
        debitTv.setOnClickListener {
            setSelectedFilterChip(visibleFilters, debitTv)
            filterData(OfferFilter.DEBIT_CARD, offerRecyclerView)
        }
        upiTv.setOnClickListener {
            setSelectedFilterChip(visibleFilters, upiTv)
            filterData(OfferFilter.UPI, offerRecyclerView)
        }


        closeIcon.setOnClickListener {
            dismiss()
        }
    }


    private fun setSelectedFilterChip(filters: List<TextView>, selectedChip: TextView) {
        filters.forEach { it.isSelected = it.id == selectedChip.id }
    }

    private fun filterData(filter: OfferFilter, offerRecyclerView: RecyclerView) {
        val filteredOffers = getOffersForFilter(filter)
        offerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = OfferRVAdapter(
            requireContext(), ArrayList(filteredOffers), getItemListener(), bankLogoMap,
            bankNameKeyList,
            banKTitleToCodeMap,
            showUpiTag,
        )
        offerRecyclerView.adapter = adapter
    }

    private fun getOffersForFilter(filter: OfferFilter): List<OfferDetail> {
        if (showUpiTag) {
            return allOffers
        }

        return when (filter) {
            OfferFilter.ALL -> allOffers
            OfferFilter.CREDIT_CARD -> allOffers.filter { isCreditCardOffer(it) }
            OfferFilter.DEBIT_CARD -> allOffers.filter { isDebitCardOffer(it) }
            OfferFilter.UPI -> allOffers.filter { isUpiOffer(it) }
        }
    }

    private fun getItemListener(): ItemClickListener<OfferDetail> {
        return object : ItemClickListener<OfferDetail> {
            override fun onItemClick(position: Int, item: OfferDetail) {
                if (showUpiTag) {
                    ExpressSDKObject.setSelectedOfferDetail(item)
                    dismissOfferSummarySheet()
                    showIcbFloatingDialog(item)
                } else if (isUpiOffer(item)) {
                    proceedWithSelectedUpiOffer(item)
                } else {
                    proceedWithSelectedOffer(item)
                }
            }
        }
    }

    private fun dismissOfferSummarySheet() {
        dialog?.let { offerDialog ->
            if (offerDialog.isShowing) {
                offerDialog.hide()
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
            dismissAllowingStateLoss()
        }
        okCta.setOnClickListener {
            emitUpiIcbStatusSheetTrigger()
            dialog.dismiss()
            dismissAllowingStateLoss()
        }

        dialog.show()
        icbFloatingDialog = dialog
    }

    private fun emitUpiIcbStatusSheetTrigger() {
        parentFragmentManager.setFragmentResult(
            RESULT_KEY_UPI_ICB_ACTION,
            Bundle().apply {
                putBoolean(RESULT_KEY_SHOW_UPI_ICB_STATUS_SHEET, true)
                putBoolean(RESULT_KEY_TRIGGER_PAYMENT, true)
            },
        )
    }

    private fun getOfferSavingsAmount(item: OfferDetail): Int {
        return if (item.isInstantSaving) {
            val savings = item.tenureOffers?.find { it.tenureId == "7" }
                ?.let { it.discountAmount + it.cashbackAmount }
                ?: 0
            if (savings > 0) savings else item.maxSaving
        } else {
            item.maxSaving
        }
    }

    private fun proceedWithSelectedOffer(item: OfferDetail) {
        ExpressSDKObject.setSelectedOfferDetail(item)
        dismiss()
        if (item.isInstantSaving) {
            findNavController().navigate(R.id.action_paymentModeFragment_to_EMICardDetailsFragment)
        } else {
            findNavController().navigate(R.id.action_paymentModeFragment_to_tenureSelectionFragment)
        }
    }

    private fun proceedWithSelectedUpiOffer(item: OfferDetail) {
        ExpressSDKObject.setSelectedOfferDetail(item)
        dismiss()
        findNavController().navigate(R.id.action_paymentModeFragment_to_UPIFragment)
    }

    override fun onDestroyView() {
        icbFloatingDialog?.dismiss()
        icbFloatingDialog = null
        super.onDestroyView()
    }


    private fun getOffersForDialog(): List<OfferDetail> {
        val upiOffers = resolveUpiOfferDetails(ExpressSDKObject.getFetchData())
        if (showUpiTag) {
            return upiOffers
        }

        return (getCardOffers() + upiOffers)
            .sortedByDescending { getOfferSavingsAmount(it) }
    }

    private fun getCardOffers(): List<OfferDetail> {
        val emiPaymentModeData = ExpressSDKObject.getEMIPaymentModeData()
        val offersList: ArrayList<OfferDetail> = arrayListOf()
        val offerDetails = emiPaymentModeData?.offerDetails
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
        }

    }

    private fun isCreditCardOffer(item: OfferDetail): Boolean {
        return item.type.contains("CC_bank", true)
    }

    private fun isDebitCardOffer(item: OfferDetail): Boolean {
        return item.type.contains("DC_Bank", true)
    }

    private fun isUpiOffer(item: OfferDetail): Boolean {
        return item.type.equals(PaymentModes.UPI.paymentModeID, true)

    }


    private fun mapBanKLogo() {
        bankLogoMap = Utils.getBankLogoHashMap()
        bankNameKeyList = Utils.getListOfBanKTitle()
        banKTitleToCodeMap = Utils.bankTitleAndCodeMapper()
    }

    companion object {
        const val ARG_SHOW_UPI_TAG = "arg_show_upi_tag"
        const val RESULT_KEY_UPI_ICB_ACTION = "result_key_upi_icb_action"
        const val RESULT_KEY_SHOW_UPI_ICB_STATUS_SHEET = "result_key_show_upi_icb_status_sheet"

        // Kept for compatibility with old listener code paths.
        const val RESULT_KEY_TRIGGER_PAYMENT = "result_key_trigger_payment"

        fun newInstance(showUpiTag: Boolean = false): OfferSummaryDialog {
            return OfferSummaryDialog().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_UPI_TAG, showUpiTag)
                }
            }
        }
    }


}

