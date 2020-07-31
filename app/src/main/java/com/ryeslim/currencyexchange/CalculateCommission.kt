package com.ryeslim.currencyexchange

import java.math.BigDecimal
import java.math.RoundingMode

interface CalculateCommission {
    fun calculate(
        amount: BigDecimal,
        numberOfOperations: Int,
        extraCondition: Boolean = true
    ): BigDecimal {
        return 0.toBigDecimal()
    }
}
