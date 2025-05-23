package com.plural_pinelabs.expresscheckoutsdk.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.RetrofitBuilder
import com.plural_pinelabs.expresscheckoutsdk.presentation.card.CardFragmentViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.splash.SplashViewModel

class SplashViewModelFactory(private val networkHelper: NetworkHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SplashViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.fetchApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}

class CardFragmentViewModelFactory(private val networkHelper: NetworkHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardFragmentViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.cardApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}