package com.ryeslim.currencyexchange.commission

import java.math.BigDecimal

interface CommissionCalculator {
    fun calculate(
        amount: BigDecimal,
        numberOfOperations: Int,
        extraCondition: Boolean = true
    ): BigDecimal {
        return 0.toBigDecimal()
    }
}
