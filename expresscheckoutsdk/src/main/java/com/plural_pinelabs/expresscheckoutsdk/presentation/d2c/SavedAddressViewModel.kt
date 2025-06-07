package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.Utils.MTAG
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddress
import com.plural_pinelabs.expresscheckoutsdk.data.model.Variables
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AddressViewModel(private val expressRepositoryImpl: ExpressRepositoryImpl) : ViewModel() {


    fun getAddressList() {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.fetchAddress(
                token = ExpressSDKObject.getToken(),
                getAddressObject()
            ).collect{
                Log.d(MTAG,"$it")
            }
        }
    }

    private fun getAddressObject(): ExpressAddress {
        val query =
            "query GetCustomerAddresses(\$customerId: String!) { getCustomerAddresses(customerId: \$customerId) { success message data { addresses { full_name address1 address2 address3 pincode city state country address_type address_category } } error } }"
        val variables = Variables(ExpressSDKObject.getFetchData()?.customerInfo?.customerId ?: "")
        val expressAddress = ExpressAddress(query, variables)
        return expressAddress
    }

}