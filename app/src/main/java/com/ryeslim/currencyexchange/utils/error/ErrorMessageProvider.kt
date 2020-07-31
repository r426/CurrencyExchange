package com.ryeslim.currencyexchange.utils.error

import android.content.Context
import com.ryeslim.currencyexchange.R

class ErrorMessageProvider(private val context: Context){
    fun getMissingAmountError(): String {
        return  context.getString(R.string.enter_the_amount)
    }

    fun getTwoDifferentCurrenciesError(): String {
        return context.getString(R.string.radio_button_error)
    }

    fun getInsufficientFundsError(): String {
        return context.getString(R.string.insufficient_funds)
    }
}