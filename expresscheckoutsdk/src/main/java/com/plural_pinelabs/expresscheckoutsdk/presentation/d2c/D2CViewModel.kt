package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.Address
import com.plural_pinelabs.expresscheckoutsdk.data.model.AddressRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.AddressResponse
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
    private val commonRepositoryImpl: ExpressRepositoryImpl,
    private val checkoutRepositoryImpl: ExpressRepositoryImpl
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

    private val _addressFetchResponse =
        MutableStateFlow<BaseResult<ExpressAddressResponse>>(BaseResult.Loading(false))
    val addressFetchResponse: StateFlow<BaseResult<ExpressAddressResponse>> = _addressFetchResponse

    private val _addressDeleteResponse =
        MutableStateFlow<BaseResult<ExpressAddressResponse>>(BaseResult.Loading(false))
    val addressDeleteResponse: StateFlow<BaseResult<ExpressAddressResponse>> =
        _addressDeleteResponse

    private val _addressSaveResponse =
        MutableStateFlow<BaseResult<ExpressAddressResponse>>(BaseResult.Loading(false))
    val addressSaveResponse: StateFlow<BaseResult<ExpressAddressResponse>> = _addressSaveResponse

    private val _updateAddressResponse =
        MutableStateFlow<BaseResult<AddressResponse>>(BaseResult.Loading(false))
     val updateAddressResponse: StateFlow<BaseResult<AddressResponse>> =
        _updateAddressResponse

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

    fun updateAddress(address: Address?) {
        viewModelScope.launch(Dispatchers.IO) {
            checkoutRepositoryImpl.getUpdateAddress(
                token = ExpressSDKObject.getToken(),
                AddressRequest(address)
            ).collect {
                _updateAddressResponse.value = it
            }
        }
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
            expressRepositoryImpl.graphQl(
                token = ExpressSDKObject.getToken(),
                getAddressObject(customerToken)
            ).collect {
                _addressResponse.value = it
            }
        }
    }

    fun getUpdatedAddressList(customerToken: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.graphQl(
                token = ExpressSDKObject.getToken(),
                getAddressObject(customerToken)
            ).collect {
                _addressFetchResponse.value = it
            }
        }
    }

    fun saveAddress(address: Address?) {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.graphQl(
                token = ExpressSDKObject.getToken(),
                getSaveAddressObject(address)
            ).collect {
                _addressSaveResponse.value = it
            }
        }
    }


    fun deleteAddress(address: Address?) {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepositoryImpl.graphQl(
                token = ExpressSDKObject.getToken(),
                getDeleteAddressObject(address)
            ).collect {
                _addressDeleteResponse.value = it
            }
        }
    }

    private fun getDeleteAddressObject(address: Address?): ExpressAddress {

        val deleteCustomerAddressMutation =
            "mutation DeleteCustomerAddress(\$customerId: String!, \$addressId: String!) { deleteCustomerAddress(customerId: \$customerId, addressId: \$addressId) { success message error } }"
        val variables =
            Variables(customerId = ExpressSDKObject.getCustomerId(), addressId = address?.id)
        val expressAddress =
            ExpressAddress("DeleteCustomerAddress", deleteCustomerAddressMutation, variables)
        return expressAddress
    }

    private fun getSaveAddressObject(address: Address?): ExpressAddress {

        val addCustomerAddressesMutation =
            "mutation AddCustomerAddresses(\$customerId: String!, \$addresses: [CustomerAddressInput]!) { addCustomerAddresses(customerId: \$customerId, addresses: \$addresses) { success message data { addresses { customer_id full_name address1 address2 address3 pincode city state country address_type address_category } } error } }"
        val addresses: ArrayList<Address> = arrayListOf()
        if (address != null) {
            addresses.add(address)
        }
        val variables =
            Variables(customerId = ExpressSDKObject.getCustomerId(), addresses = addresses)
        val expressAddress =
            ExpressAddress("AddCustomerAddresses", addCustomerAddressesMutation, variables)
        return expressAddress
    }

    private fun getAddressObject(customerToken: String?): ExpressAddress {
        val query = "\n" +
                "query GetCustomerAddressesByMobile(\$customerToken: String!, \$merchantId: String) { getCustomerAddressesByMobile(customerToken: \$customerToken, merchantId: \$merchantId) { success message data { addresses { id customer_id full_name address1 address2 address3 pincode city state country address_type address_category } } error } }"
        val variables = Variables(customerToken ?: "")
        val expressAddress = ExpressAddress(null, query, variables)
        return expressAddress
    }

    fun resetCreateInactiveResult(){
        _createInactiveUserResult.value = BaseResult.Loading(false)
    }

    fun resetSendOtpResult(){
        _sendOTPResult.value = BaseResult.Loading(false)
    }

}
