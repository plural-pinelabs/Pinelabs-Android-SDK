package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.AddressViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper

class SavedAddressFragment : Fragment() {
    private lateinit var addressViewModel: AddressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addressViewModel = ViewModelProvider(
            this,
            AddressViewModelFactory(NetworkHelper(requireContext()))
        )[AddressViewModel::class.java]
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addressViewModel.getAddressList()
    }
}