package com.ryeslim.currencyexchange.utils.initial

import com.ryeslim.currencyexchange.dataclass.Currency

class InitialBalanceProvider {
    fun getInitialEurBalance(): Currency {
        return Currency(
            EUR_BALANCE_INITIAL.toBigDecimal(),
            EUR_CURRENCY_CODE
        )
    }

    fun getInitialUsdBalance(): Currency {
        return Currency(
            USD_BALANCE_INITIAL.toBigDecimal(),
            USD_CURRENCY_CODE
        )
    }

    fun getInitialJpyBalance(): Currency {
        return Currency(
            JPY_BALANCE_INITIAL.toBigDecimal(),
            JPY_CURRENCY_CODE
        )
    }

    companion object {
        const val EUR_BALANCE_INITIAL = 1000
        const val USD_BALANCE_INITIAL = 0
        const val JPY_BALANCE_INITIAL = 0
        const val EUR_CURRENCY_CODE = "EUR"
        const val USD_CURRENCY_CODE = "USD"
        const val JPY_CURRENCY_CODE = "JPY"
    }
}