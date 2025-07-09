package com.plural_pinelabs.expresscheckoutsdk.presentation.ordersummary

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.data.model.OrderSummary

class TopSheetDialogFragment : DialogFragment() {

    class TopSheetDialogFragment : DialogFragment() {

        companion object {
            private const val ARG_ORDER = "arg_order"

            fun newInstance(orderSummary: OrderSummary): TopSheetDialogFragment {
                val fragment = TopSheetDialogFragment()
                val args = Bundle()
                args.putParcelable(ARG_ORDER, orderSummary)
                fragment.arguments = args
                return fragment
            }
        }

        private var orderSummary: OrderSummary? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            orderSummary = arguments?.getParcelable(ARG_ORDER)
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.order_summary_layout)

            dialog.window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setGravity(Gravity.TOP)
                setBackgroundDrawable(Color.WHITE.toDrawable())
            }

            setupUI(dialog.findViewById(R.id.order_summary_layout_parent), orderSummary)

            return dialog
        }

        private fun setupUI(root: View, orderSummary: OrderSummary?) {
            val productsRecyclerView: RecyclerView = root.findViewById(R.id.product_recycler_view)
            val subtotalRecyclerView: RecyclerView = root.findViewById(R.id.subtotal_recycler_view)
            val closeButton: View = root.findViewById(R.id.order_close_icon)

            closeButton.setOnClickListener {
                dismiss()
            }
            val data = ExpressSDKObject.getFetchData()
            val cartItems = data?.cartDetails?.cart_items
            if (cartItems.isNullOrEmpty()) {
                productsRecyclerView.visibility = View.GONE
            } else {
                val productAdapter = ProductRVAdapter(requireContext(), cartItems)

            }
        }
    }


}
