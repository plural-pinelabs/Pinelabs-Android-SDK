package com.plural_pinelabs.expresscheckoutsdk.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.plural_pinelabs.expresscheckoutsdk.data.repository.ExpressRepositoryImpl
import com.plural_pinelabs.expresscheckoutsdk.data.retrofit.RetrofitBuilder
import com.plural_pinelabs.expresscheckoutsdk.presentation.acs.ACSFragmentViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.card.CardFragmentViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.card.SavedCardOTPViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.d2c.AddressViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.d2c.NewAddressFragmentViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.d2c.VerifyOTPFragmentViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.emi.TenureSelectionViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.nativeotp.NativeOTPViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.netbanking.NetBankingViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.splash.SplashViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.upi.UPIViewModel
import com.plural_pinelabs.expresscheckoutsdk.presentation.wallets.WalletViewModel

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

class CardFragmentViewModelFactory(private val networkHelper: NetworkHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardFragmentViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.commonApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}

class NativeOTPFragmentViewModelFactory(private val networkHelper: NetworkHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NativeOTPViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.commonApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}


class VerifyOTPFragmentViewModelFactory(private val networkHelper: NetworkHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VerifyOTPFragmentViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.commonApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}

class SaveCardOTPFragmentViewModelFactory(private val networkHelper: NetworkHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SavedCardOTPViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.commonApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}


class UPIViewModelFactory(private val networkHelper: NetworkHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UPIViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.commonApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}

class NewAddressFragmentViewModelFactory(private val networkHelper: NetworkHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewAddressFragmentViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.commonApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}


class AddressViewModelFactory(private val networkHelper: NetworkHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddressViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.expressApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}


class ACSViewModelFactory(private val networkHelper: NetworkHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ACSFragmentViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.commonApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}

class WalletViewModelFactory(private val networkHelper: NetworkHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WalletViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.commonApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}


class NetBankingViewModelFactory(private val networkHelper: NetworkHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NetBankingViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.commonApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}


class TenureSelectionViewModelFactory(private val networkHelper: NetworkHelper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TenureSelectionViewModel(
            ExpressRepositoryImpl(
                RetrofitBuilder.commonApiService,
                networkHelper = networkHelper
            )
        ) as T
    }
}

