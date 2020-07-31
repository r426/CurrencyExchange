package com.ryeslim.currencyexchange.commission

import java.math.BigDecimal
import java.math.RoundingMode

class SevenPercentCommissionCalculator :
    CommissionCalculator {
    override fun calculate(
        amount: BigDecimal,
        numberOfOperations: Int,
        extraCondition: Boolean
    ): BigDecimal {
        return if (numberOfOperations > NUMBER_OF_FREE_OPERATIONS && extraCondition) {
            (amount * (COMMISSIONS_PERCENT / 100).toBigDecimal())
                .setScale(2, RoundingMode.HALF_EVEN)
        } else {
            0.toBigDecimal()
        }
    }

    companion object {
        val NUMBER_OF_FREE_OPERATIONS = 5
        val COMMISSIONS_PERCENT = 0.7
    }
}