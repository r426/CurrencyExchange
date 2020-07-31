package com.ryeslim.currencyexchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryeslim.currencyexchange.commission.CommissionCalculator
import com.ryeslim.currencyexchange.retrofit.ServiceFactory

class MainViewModelFactory(
    private val currencyService: CurrencyApi,
    private var commission: CommissionCalculator
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(currencyService, commission) as T
    }
} 