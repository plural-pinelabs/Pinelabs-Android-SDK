package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.D2CViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.showProcessPaymentDialog
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddressResponse
import kotlinx.coroutines.launch

class SavedAddressFragment : Fragment() {

    // In the first fragment of the flow
    private val viewModel: D2CViewModel by activityViewModels {
        D2CViewModelFactory(NetworkHelper(requireContext()))
    }
    private var bottomSheetDialog: BottomSheetDialog? = null

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
        fetchAddresses()
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

    }

    private fun fetchAddresses() {
        viewModel.getAddressList()
    }

}