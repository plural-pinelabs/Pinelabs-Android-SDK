package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.Address
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfoResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddress
import com.plural_pinelabs.expresscheckoutsdk.data.model.ExpressAddressResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.Variables
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class D2CViewModel(
    private val expressRepositoryImpl: ExpressRepositoryImpl,
    private val commonRepositoryImpl: ExpressRepositoryImpl
) : ViewModel() {


    private val _createInactiveUserResult =
        MutableStateFlow<BaseResult<CustomerInfo?>>(BaseResult.Loading(false))
    val createInactiveUserResult: StateFlow<BaseResult<CustomerInfo?>> = _createInactiveUserResult

    private val _sendOTPResult =
        MutableStateFlow<BaseResult<SavedCardResponse>>(BaseResult.Loading(false))
    val sendOTPResult: StateFlow<BaseResult<SavedCardResponse>> = _sendOTPResult


    private val _submitOTPResult =
        MutableStateFlow<BaseResult<CustomerInfoResponse>>(BaseResult.Loading(false))
    val submitOTPResult: StateFlow<BaseResult<CustomerInfoResponse>> = _submitOTPResult


    private val _addressResponse =
        MutableStateFlow<BaseResult<ExpressAddressResponse>>(BaseResult.Loading(false))
    val addressResponse: StateFlow<BaseResult<ExpressAddressResponse>> = _addressResponse


    var phoneNumber: String? = null
    var email: String? = null
    var isOtpVerified: Boolean = false
    var addressList: List<Address>? = null
    var selectedAddress: Address? = null
    var otpId: String? = null
    var customerInfo: CustomerInfo? = null
    var countryCode: String? = null


    fun resetFlow() {
        phoneNumber = null
        email = null
        isOtpVerified = false
        addressList = null
        selectedAddress = null
    }

    fun createInactiveUser(token: String?, request: CustomerInfo?) =
        viewModelScope.launch(Dispatchers.IO) {
            commonRepositoryImpl.createInactiveUser(token, request).collect {
                _createInactiveUserResult.value = it
            }
        }

    fun sendOtp(token: String?, request: OTPRequest?) =
        viewModelScope.launch(Dispatchers.IO) {
            commonRepositoryImpl.sendOTPCustomer(token, request).collect {
                _sendOTPResult.value = it
            }
        }

    fun validateUpdateOrderDetails(token: String?, otpRequest: OTPRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            commonRepositoryImpl.validateUpdateOrder(
                token = token,
                otpRequest
            )
                .collect {
                    _submitOTPResult.value = it
                }
        }
    }

    fun getAddressList(customerToken: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.fetchAddress(
                token = ExpressSDKObject.getToken(),
                getAddressObject(customerToken)
            ).collect {
                _addressResponse.value = it
            }
        }
    }

    fun saveAddress(customerId: String?,address: Address?) {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.addCustomerAddresses(
                token = ExpressSDKObject.getToken(),
                getAddCustomerAddressObject(customerId,address)
            ).collect {
                _addressResponse.value = it
            }
        }
    }

    private fun getAddressObject(customerToken: String?): ExpressAddress {
        val query =
            "query GetCustomerAddresses(\$customerId: String!) { getCustomerAddresses(customerId: \$customerId) { success message data { addresses { full_name address1 address2 address3 pincode city state country address_type address_category } } error } }"
        val variables = Variables(customerToken ?: "")
        val expressAddress = ExpressAddress(null, query, variables)
        return expressAddress
    }


    private fun getAddCustomerAddressObject(
        customerId: String? = null,
        address: Address?
    ): ExpressAddress {
        val query =
            "mutation AddCustomerAddresses(\$customerId: String!, \$addresses: [CustomerAddressInput]!) { addCustomerAddresses(customerId: \$customerId, addresses: \$addresses) { success message data { addresses { full_name address1 city state country address_type address_category } } error } }"
        val addressList: ArrayList<Address> = arrayListOf()
        address?.let { addressList.add(it) }

        val variables = Variables(customerId = customerId, addresses = addressList)
        return ExpressAddress(null, query, variables)
    }

}
