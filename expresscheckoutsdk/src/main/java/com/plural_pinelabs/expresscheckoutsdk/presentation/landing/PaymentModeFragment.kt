package com.plural_pinelabs.expresscheckoutsdk.presentation.landing

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PAY_BY_POINTS_ID
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.PaymentModes
import com.plural_pinelabs.expresscheckoutsdk.data.model.PaymentMode
import com.plural_pinelabs.expresscheckoutsdk.presentation.utils.DividerItemDecoration

class PaymentModeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.payment_option_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                ContextCompat.getDrawable(requireContext(), R.drawable.recycler_view_divider)
            )
        )
        val paymentModes = getPaymentModes()
        paymentModes?.let {
            val adapter =
                PaymentModeRecyclerViewAdapter(
                    requireContext(),
                    it,
                    isPBPEnabled(paymentModes),
                    getPaymentModeSelectionCallback(requireContext())
                )
            recyclerView.adapter = adapter
        }


    }

    private fun getPaymentModes(): List<PaymentMode>? {
        return ExpressSDKObject.getFetchData()?.paymentModes
    }

    private fun isPBPEnabled(paymentModes: List<PaymentMode>): Boolean {
        return paymentModes.any { it.paymentModeId == PAY_BY_POINTS_ID }
    }

    private fun getPaymentModeSelectionCallback(context: Context): ItemClickListener<PaymentMode>? {
        return object : ItemClickListener<PaymentMode> {
            override fun onItemClick(position: Int, item: PaymentMode) {
                //TODO navigate to different fragments based on the payment mode selected
                when (item.paymentModeId) {
                    PaymentModes.CREDIT_DEBIT.paymentModeID -> {
                        findNavController().navigate(R.id.action_paymentModeFragment_to_cardFragment)
                    }

                    PaymentModes.UPI.paymentModeID -> {
                        // Handle UPI selection
                    }

                    PaymentModes.NET_BANKING.paymentModeID -> {
                        // Handle Netbanking selection
                    }

                    PaymentModes.WALLET.paymentModeID -> {
                        // Handle Wallet selection
                    }

                    else -> {
                        // Handle other selections
                    }
                }

            }
        }
    }

}
