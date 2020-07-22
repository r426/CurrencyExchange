package com.ryeslim.currencyexchange.dataclass

import java.math.BigDecimal

data class InfoMessage(
    val amountToConvert: BigDecimal,
    val currencyCodeFrom: String,
    val balance: BigDecimal,
    val balanceCurrencyCode: String,
    val commission: BigDecimal,
    val commissionCurrencyCode: String
)