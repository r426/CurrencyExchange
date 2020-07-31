package com.ryeslim.currencyexchange.utils.initial

import java.math.BigDecimal

class InitialCommissionProvider {
    fun getInitialEurCommission(): BigDecimal {
        return EUR_COMMISSION_INITIAL
    }

    fun getInitialUsdCommission(): BigDecimal {
        return USD_COMMISSION_INITIAL
    }

    fun getInitialJpyCommission(): BigDecimal {
        return JPY_COMMISSION_INITIAL
    }

    companion object {
        val EUR_COMMISSION_INITIAL = 0.toBigDecimal()
        val USD_COMMISSION_INITIAL = 0.toBigDecimal()
        val JPY_COMMISSION_INITIAL = 0.toBigDecimal()
    }
}