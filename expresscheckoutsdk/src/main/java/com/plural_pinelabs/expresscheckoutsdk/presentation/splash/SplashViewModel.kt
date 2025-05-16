package com.plural_pinelabs.expresscheckoutsdk.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.domain.repository.ExpressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(private val expressRepository: ExpressRepository) : ViewModel() {

    private val _fetchDataResult =
        MutableStateFlow<BaseResult<FetchResponseDTO>>(BaseResult.Loading(true))
    val fetchDataResult: StateFlow<BaseResult<FetchResponseDTO>> = _fetchDataResult

    fun fetchData(token:String) {
        viewModelScope.launch(Dispatchers.IO) {
            expressRepository.fetchData(token = token).collect {
                when (it) {
                    is BaseResult.Error -> {
                        _fetchDataResult.value = it
                    }

                    is BaseResult.Loading -> {
                        _fetchDataResult.value = it
                    }

                    is BaseResult.Success<*> -> {
                        _fetchDataResult.value = it
                    }

                    else -> {
                        _fetchDataResult.value = it
                    }
                }
            }

        }

    }
}