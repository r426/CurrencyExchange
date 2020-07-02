package com.ryeslim.currencyexchangeretrofitcoroutines

import java.math.BigDecimal
import java.math.RoundingMode

interface CalculateCommission {
    fun calculate(amount: BigDecimal, numberOfOperations: Int, extraCondition: Boolean = true): BigDecimal {
        return 0.toBigDecimal()
    }
}

class SevenPercent : CalculateCommission {
    override fun calculate(amount: BigDecimal, numberOfOperations: Int, extraCondition: Boolean): BigDecimal {
        return if (numberOfOperations > 5 && extraCondition) {
            (amount * (0.7 / 100).toBigDecimal()).setScale(2, RoundingMode.HALF_EVEN)
        } else {
            0.toBigDecimal()
        }
    }
}