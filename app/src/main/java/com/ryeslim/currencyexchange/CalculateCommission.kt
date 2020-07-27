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

class SevenPercent : CalculateCommission {
    override fun calculate(
        amount: BigDecimal,
        numberOfOperations: Int,
        extraCondition: Boolean
    ): BigDecimal {
        val numberOfFreeOperations = 5
        val commissionsPercent = 0.7
        return if (numberOfOperations > numberOfFreeOperations && extraCondition) {
            (amount * (commissionsPercent / 100).toBigDecimal()).setScale(2, RoundingMode.HALF_EVEN)
        } else {
            0.toBigDecimal()
        }
    }
}