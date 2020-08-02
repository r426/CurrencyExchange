package com.ryeslim.currencyexchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryeslim.currencyexchange.commission.CommissionCalculator
import com.ryeslim.currencyexchange.utils.error.ErrorMessageProvider
import com.ryeslim.currencyexchange.utils.initial.InitialBalanceProvider
import com.ryeslim.currencyexchange.utils.initial.InitialCommissionProvider

class MainViewModelFactory(
    private val currencyService: CurrencyApi,
    private val commission: CommissionCalculator,
    private val errorMessageProvider: ErrorMessageProvider,
    private val initialBalanceProvider: InitialBalanceProvider,
    private val initialCommissionProvider: InitialCommissionProvider
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(
            currencyService,
            commission,
            errorMessageProvider,
            initialBalanceProvider,
            initialCommissionProvider
        ) as T
    }
} 