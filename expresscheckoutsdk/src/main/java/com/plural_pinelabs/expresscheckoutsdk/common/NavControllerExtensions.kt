package com.plural_pinelabs.expresscheckoutsdk.common

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

fun NavController.safeNavigate(@IdRes actionId: Int, args: Bundle? = null): Boolean {
    val canNavigate = currentDestination?.getAction(actionId) != null || graph.getAction(actionId) != null
    if (!canNavigate) return false

    return runCatching {
        navigate(actionId, args)
        true
    }.getOrDefault(false)
}

fun Fragment.safeNavigate(@IdRes actionId: Int, args: Bundle? = null): Boolean {
    return findNavController().safeNavigate(actionId, args)
}

fun Fragment.safePopBackStack(): Boolean {
    return runCatching { findNavController().popBackStack() }.getOrDefault(false)
}

