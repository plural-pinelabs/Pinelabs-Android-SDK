package com.plural_pinelabs.native_express_sdk.utils

import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.*
import androidx.test.espresso.web.webdriver.Locator
import android.util.Log

object BankSimulatorAndroid {

    private const val TAG = "BankSimulatorAndroid"

    fun handleAnyAcsSuccess() {
        val simulators = listOf(
            BankSimulatorAndroid::cardsSuccess,
            BankSimulatorAndroid::debitCardSuccess,
            BankSimulatorAndroid::visaOtpSuccess,
            BankSimulatorAndroid::fillOtpPayNextSimulator,
            BankSimulatorAndroid::netbankingSuccess,
            BankSimulatorAndroid::cardsManualFailure
        )

        for (sim in simulators) {
            try {
                Log.d(TAG, "Trying simulator: ${sim.name}")
                sim()
                Log.d(TAG, "Simulator SUCCESS: ${sim.name}")
                return
            } catch (e: Exception) {
                Log.d(TAG, "Simulator FAILED: ${sim.name} → ${e.message}")
            }
        }

        throw RuntimeException("❌ No ACS simulator matched this flow")
    }

    // ---------- DEFAULT SIMULATOR ----------
    fun cardsSuccess() {
        Thread.sleep(8000)

        onWebView()
            .withElement(findElement(Locator.XPATH, "//input[contains(@placeholder,'Code')]"))
            .perform(webKeys("1234"))

        onWebView()
            .withElement(findElement(Locator.XPATH, "//button[contains(text(),'SUBMIT')]"))
            .perform(webClick())

        Thread.sleep(5000)
    }

    // ---------- NETBANKING ----------
    fun netbankingSuccess() {
        Thread.sleep(5000)

        onWebView()
            .withElement(findElement(Locator.XPATH, "//*[contains(text(),'Status')]"))
            .perform(webClick())

        onWebView()
            .withElement(findElement(Locator.XPATH, "//button[contains(text(),'Success')]"))
            .perform(webClick())

        onWebView()
            .withElement(findElement(Locator.XPATH, "//button[contains(text(),'Submit')]"))
            .perform(webClick())

        Thread.sleep(5000)
    }

    // ---------- MANUAL FAILURE ----------
    fun cardsManualFailure() {
        Thread.sleep(6000)

        onWebView()
            .withElement(findElement(Locator.XPATH, "//button[contains(text(),'CANCEL')]"))
            .perform(webClick())

        Thread.sleep(2000)
    }

    // ---------- DEBIT CARD (EDGE CHECKOUT) ----------
    fun debitCardSuccess() {
        Thread.sleep(6000)

        onWebView()
            .withElement(findElement(Locator.XPATH, "//input"))
            .perform(webKeys("000000"))

        onWebView()
            .withElement(findElement(Locator.XPATH, "//button[contains(text(),'Submit')]"))
            .perform(webClick())

        Thread.sleep(5000)

        onWebView()
            .withElement(findElement(Locator.XPATH, "//button[contains(text(),'Confirm')]"))
            .perform(webClick())

        Thread.sleep(6000)
    }

    // ---------- VISA 3DS ----------
    fun visaOtpSuccess() {
        Thread.sleep(6000)

        onWebView()
            .withElement(findElement(Locator.XPATH, "//*[contains(text(),'Email')]"))
            .perform(webClick())

        onWebView()
            .withElement(findElement(Locator.XPATH, "//button[contains(text(),'Next')]"))
            .perform(webClick())

        Thread.sleep(5000)

        onWebView()
            .withElement(findElement(Locator.XPATH, "//input[@type='password' or @type='tel']"))
            .perform(webKeys("000000"))

        onWebView()
            .withElement(findElement(Locator.XPATH, "//button[contains(text(),'submit')]"))
            .perform(webClick())

        Thread.sleep(5000)
    }

    // ---------- PAYNEXT ----------
    fun fillOtpPayNextSimulator() {
        Thread.sleep(10000)

        onWebView()
            .withElement(findElement(Locator.XPATH, "//input"))
            .perform(webKeys("123456"))

        onWebView()
            .withElement(findElement(Locator.XPATH, "//button[contains(@class,'selected')]"))
            .perform(webClick())

        onWebView()
            .withElement(findElement(Locator.XPATH, "//button[contains(text(),'Submit')]"))
            .perform(webClick())

        Thread.sleep(5000)
    }
}
