package com.plural_pinelabs.expresscheckoutsdk.presentation.d2c

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.Address
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfo
import com.plural_pinelabs.expresscheckoutsdk.data.model.CustomerInfoResponse
import com.plural_pinelabs.expresscheckoutsdk.data.model.OTPRequest
import com.plural_pinelabs.expresscheckoutsdk.data.model.SavedCardResponse
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
            expressRepositoryImpl.createInactiveUser(token, request).collect {
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
            expressRepositoryImpl.validateUpdateOrder(
                token = token,
                otpRequest
            )
                .collect {
                    _submitOTPResult.value = it
                }
        }
    }

}
