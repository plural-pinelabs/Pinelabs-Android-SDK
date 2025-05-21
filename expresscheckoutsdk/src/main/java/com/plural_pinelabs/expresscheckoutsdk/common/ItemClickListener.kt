package com.plural_pinelabs.expresscheckoutsdk.common

interface ItemClickListener<T> {

    fun onItemClick(position: Int, item: T)
}