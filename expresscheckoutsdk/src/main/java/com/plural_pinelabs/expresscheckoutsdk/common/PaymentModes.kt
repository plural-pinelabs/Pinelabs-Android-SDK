package com.plural_pinelabs.expresscheckoutsdk.common

import com.plural_pinelabs.expresscheckoutsdk.R
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.CREDIT_DEBIT_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.NET_BANKING_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.UPI_ID
import com.plural_pinelabs.expresscheckoutsdk.common.Constants.WALLET_ID

enum class PaymentModes(
    val paymentModeImage: Int,
    val paymentModeName: Int,
    val paymentModeID: String,
    val paymentModeDescription: Int,
) {
    CREDIT_DEBIT(
        R.drawable.ic_cards_payment_icon,
        R.string.credit_debit_label,
        CREDIT_DEBIT_ID,
        R.string.credit_debit_label_description
    ),
    NET_BANKING(
        R.drawable.ic_netbanking_payment_icon,
        R.string.net_banking_label,
        NET_BANKING_ID,
        R.string.net_banking_label_description
    ),
    UPI(R.drawable.ic_upi, R.string.upi_label, UPI_ID, R.string.upi_label_description),
    WALLET(
        R.drawable.ic_wallets_payment_icon,
        R.string.wallet_label,
        WALLET_ID,
        R.string.wallet_label_description
    ),
    ALL_PAYMENT(
        R.drawable.ic_cards_payment_icon,
        R.string.all_payment_methods_label,
        "",
        R.string.all_payment_methods_label_description
    ),
}

enum class TransactionMode {
    REDIRECT
}

enum class DeviceType {
    WEB,
    MOBILE,
    TABLET
}

enum class PaymentModeId(val id: Int) {
    CREDIT_DEBIT(1),
    NETBANKING(3),
    UPI(10),
    EMI(4),
    WALLET(11),
    PBP(15),
    DEBIT_EMI(14),
    CARDLESS_EMI(19)
}
