package com.ryeslim.currencyexchange.dataclass

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Currency(
    @Expose
    @SerializedName("amount")
    var balanceValue: BigDecimal,
    @Expose
    @SerializedName("currency")
    var currencyCode: String)