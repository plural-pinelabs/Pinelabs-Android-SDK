package com.plural_pinelabs.expresscheckoutsdk.presentation.splash

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.clevertap.android.sdk.isNotNullAndBlank
import com.plural_pinelabs.expresscheckoutsdk.ExpressSDKObject
import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.BaseResult
import com.plural_pinelabs.expresscheckoutsdk.common.CleverTapUtil
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_ATTEMPTED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_FAILED
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_PENDING
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.PROCESSED_STATUS
import com.plural_pinelabs.expresscheckoutsdk.common.NetworkHelper
import com.plural_pinelabs.expresscheckoutsdk.common.SplashViewModelFactory
import com.plural_pinelabs.expresscheckoutsdk.data.model.FetchResponseDTO
import com.plural_pinelabs.expresscheckoutsdk.data.model.TransactionStatusResponse
import com.plural_pinelabs.expresscheckoutsdk.presentation.LandingActivity
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {
    private var isDataFetched = false

    private var isRevealShown = false
    private var logoAnimation: LottieAnimationView? = null

    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            SplashViewModelFactory(NetworkHelper(requireContext()))
        )[SplashViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLottieAnimation(view)
        observeViewModel()

        viewModel.fetchData(ExpressSDKObject.getToken() ?: "")
        CleverTapUtil.sdkInitialized(CleverTapUtil.getInstance(requireContext()), requireContext())

    }

    private fun setLottieAnimation(view: View) {

        logoAnimation = view.findViewById(R.id.img_logo)
        if (logoAnimation == null) {
            return
        }
        logoAnimation?.setAnimation(R.raw.reveal)
        logoAnimation?.playAnimation()


        logoAnimation?.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                if (isDataFetched) {
                    logoAnimation?.pauseAnimation()
                }
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {
                if (isDataFetched) {
                    logoAnimation?.pauseAnimation()
                }
                if (!isRevealShown) {
                    isRevealShown = true
                    logoAnimation?.setAnimation(R.raw.logo)
                    logoAnimation?.playAnimation()
                }

            }

        })

    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchDataResult.collect { result ->
                    when (result) {
                        is BaseResult.Error -> {
                            isDataFetched = true
                            // Han
                            // dle error
                            result.errorCode.let { exception ->
                                Log.d("Error", "Error fetching data: $exception")
                                findNavController().navigate(R.id.action_splashFragment_to_failureFragment)
                                // Log the error or show a message to the user
                                Log.e("Error", exception)
                                // For example, navigate to an error screen or show a dialog
                            }

                        }

                        is BaseResult.Success<FetchResponseDTO> -> {
                            isDataFetched = true
                            // Handle success
                            result.data.let { it ->
                                // Process the data
                                ExpressSDKObject.setFetchData(it)
                                (activity as? LandingActivity)?.updateValueForHeaderLayout(it)
                                CleverTapUtil.sdkCheckoutRendered(
                                    CleverTapUtil.getInstance(requireContext()),
                                    it
                                )
                                if (it?.transactionInfo?.orderStatus?.equals(
                                        PROCESSED_STATUS,
                                        true
                                    ) == true
                                ) {
                                    // order is already processed check with the inquiry to confirm
                                    viewModel.getTransactionStatus(ExpressSDKObject.getToken())
                                } else {
                                    // directly handle nav
                                    handleNav()
                                }
//
                            }
                        }

                        is BaseResult.Loading -> {
                            // handle loading
                            Log.d("Loading", "Loading data...")
                            result.isLoading
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.transactionStatusResult.collect {
                    when (it) {
                        is BaseResult.Error -> {
                            //TODO  discuss in case of error what to do
                            findNavController().navigate(R.id.action_splashFragment_to_successFragment)
                        }

                        is BaseResult.Loading -> {
                            // nothing to do since we already show the process payment dialog
                        }

                        is BaseResult.Success<TransactionStatusResponse> -> {
                            val status = it.data.data.status
                            when (status) {
                                PROCESSED_PENDING, PROCESSED_ATTEMPTED -> {
                                    // continue
                                    handleNav()
                                }

                                PROCESSED_STATUS -> {
                                    findNavController().navigate(R.id.action_splashFragment_to_successFragment)
                                }

                                PROCESSED_FAILED -> {
                                    findNavController().navigate(R.id.action_splashFragment_to_successFragment)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleNav() {
        // TODO Finalize the condition for the d2c flow
//                                if (it.customerInfo?.customerId.isNullOrEmpty()) {
//                                    // no customer id new user
//                                    //  findNavController().navigate(R.id.action_splashFragment_to_phoneNumberFragment)
        //   findNavController().navigate(R.id.action_splashFragment_to_paymentModeFragment)
//                                } else
//                                // TODO UNCOMMENT NAVIGATION TO LANDING AND REMOVE THIS
        //    findNavController().navigate(R.id.action_splashFragment_to_paymentModeFragment)
        //    findNavController().navigate(R.id.action_splashFragment_to_phoneNumberFragment)
        navD2C()
    }

    private fun navD2C() {
        val mobileNo = ExpressSDKObject.getFetchData()?.customerInfo?.mobileNo
        val address = ExpressSDKObject.getFetchData()?.shippingAddress
        val addressCollectionFlag =
            ExpressSDKObject.getFetchData()?.merchantMetadata?.express_checkout_allowed_action?.contains(
                "checkoutCollectAddress"
            )
        val mobileCollectionFlag =
            ExpressSDKObject.getFetchData()?.merchantMetadata?.express_checkout_allowed_action?.contains(
                "checkoutCollectMobile"
            )

        if (address?.address1 != null && mobileNo.isNotNullAndBlank()) {
            ExpressSDKObject.setSelectedAddress(ExpressSDKObject.getFetchData()?.shippingAddress)
            findNavController().navigate(R.id.action_splashFragment_to_paymentModeFragment)
        } else if (address?.id == null && addressCollectionFlag == true) {
            findNavController().navigate(R.id.action_splashFragment_to_phoneNumberFragment)
            // navigate to phone
        } else if (mobileNo.isNullOrBlank() && mobileCollectionFlag == true) {
            // navigate to phone
            findNavController().navigate(R.id.action_splashFragment_to_phoneNumberFragment)
        } else {
            findNavController().navigate(R.id.action_splashFragment_to_paymentModeFragment)
            // navigate to payment
        }
    }

}