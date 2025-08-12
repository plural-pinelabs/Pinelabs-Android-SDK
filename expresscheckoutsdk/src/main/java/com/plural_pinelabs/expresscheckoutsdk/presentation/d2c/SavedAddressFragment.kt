package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.D2CViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.Address
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddressResponse
import kotlinx.coroutines.launch

class SavedAddressFragment : Fragment() {

    // In the first fragment of the flow
    private val viewModel: D2CViewModel by activityViewModels {
        D2CViewModelFactory(NetworkHelper(requireContext()))
    }
    private var bottomSheetDialog: BottomSheetDialog? = null

    private lateinit var addNewBtn: TextView
    private lateinit var recyclerview: androidx.recyclerview.widget.RecyclerView
    private lateinit var continueBtn: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.addressResponse.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            //TODO handle failure
                            bottomSheetDialog?.dismiss()
                        }

                        is BaseResult.Loading -> {
                            if (it.isLoading)
                                bottomSheetDialog = showProcessPaymentDialog(requireContext())
                        }

                        is BaseResult.Success<ExpressAddressResponse?> -> {
                            //TODO load list and show data
                        }
                    }
                }
            }
        }
    }

    private fun setViews(view: View) {
        addNewBtn = view.findViewById(R.id.add_new_btn)
        recyclerview = view.findViewById(R.id.address_list_rv)
        continueBtn = view.findViewById(R.id.continue_btn)
        recyclerview.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun getItemClickListener() = object : ItemClickListener<Address> {
        override fun onItemClick(position: Int, item: Address) {
            // handle item click

            //add customer info to order details
        }


    }

}