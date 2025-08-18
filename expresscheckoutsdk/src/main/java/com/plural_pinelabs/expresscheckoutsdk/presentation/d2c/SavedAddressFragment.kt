package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.D2CViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.ItemClickListener
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.handleCTAEnableDisable
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
    private lateinit var continueBtn: Button

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
                viewModel.addressFetchResponse.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            //TODO handle failure
                            bottomSheetDialog?.dismiss()
                        }

                        is BaseResult.Loading -> {
                        }

                        is BaseResult.Success<ExpressAddressResponse?> -> {
                            bottomSheetDialog?.dismiss()
                            val list = it.data?.data?.getCustomerAddressesByMobile?.data?.addresses
                            if (!list.isNullOrEmpty()) {

                                val updatedList = list.toMutableList() ?: mutableListOf()
                                if (ExpressSDKObject.getFetchData()?.shippingAddress?.address1 != null) {
                                    val shippingAddress =
                                        ExpressSDKObject.getFetchData()?.shippingAddress
                                    shippingAddress?.let { it1 -> updatedList.add(0, it1) }
                                }
                                ExpressSDKObject.setAddressList(updatedList)
                            }
                            recyclerview.adapter =
                                AddressListAdapter(
                                    requireContext(),
                                    ExpressSDKObject.getAddressList() ?: emptyList(),
                                    getItemClickListener()
                                )
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.addressDeleteResponse.collect {
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
                            viewModel.getUpdatedAddressList(ExpressSDKObject.getCustomerToken())
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
        val adapter = AddressListAdapter(
            requireContext(),
            ExpressSDKObject.getAddressList() ?: emptyList(),
            getItemClickListener()
        )
        recyclerview.adapter = adapter

        continueBtn.setOnClickListener {
            if (ExpressSDKObject.getSelectedAddress() != null) {
                // Navigate to next step
                // For example, you can use findNavController().navigate(R.id.action_savedAddressFragment_to_nextFragment)
                viewModel.updateAddress(ExpressSDKObject.getSelectedAddress())
                findNavController().navigate(R.id.action_savedAddressFragment_to_paymentModeFragment)
            }
        }

        addNewBtn.setOnClickListener {
            // Navigate to new address form
            findNavController().navigate(
                R.id.action_savedAddressFragment_to_newAddressFormFragment,)
        }

    }

    private fun getItemClickListener() = object : ItemClickListener<Address?> {
        override fun onItemClick(position: Int, item: Address?) {
            // handle item click
            when (position) {
                0 -> {
                    //item is selected enable continue button
                    handleCTAEnableDisable(requireContext(), true, continueBtn)
                    ExpressSDKObject.setSelectedAddress(address = item)
                  viewModel.updateAddress(ExpressSDKObject.getSelectedAddress())
                }

                1 -> {
                    //edit
                    ExpressSDKObject.setSelectedAddress(address = item)
                    val bundle = Bundle()
                    bundle.putBoolean("isEditAddress", true)
                    findNavController().navigate(
                        R.id.action_savedAddressFragment_to_newAddressFormFragment,
                        bundle
                    )
                }

                2 -> {
                    viewModel.deleteAddress(item)
                }
            }
            //add customer info to order details
        }


    }
}