package com.ryeslim.currencyexchange


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryeslim.currencyexchange.commission.CommissionCalculator
import com.ryeslim.currencyexchange.dataclass.Currency
import com.ryeslim.currencyexchange.dataclass.InfoMessage
import com.ryeslim.currencyexchange.utils.initial.InitialBalanceProvider
import com.ryeslim.currencyexchange.utils.error.ErrorMessageProvider
import kotlinx.coroutines.*
import java.math.BigDecimal

class MainViewModel(
    private val currencyService: CurrencyApi,
    private val commissionCalculator: CommissionCalculator,
    private val errorMessageProvider: ErrorMessageProvider,
    initialBalanceProvider: InitialBalanceProvider
) : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _eur = MutableLiveData<Currency>()
    val eur: LiveData<Currency> = _eur

    private val _usd = MutableLiveData<Currency>()
    val usd: LiveData<Currency> = _usd

    private val _jpy = MutableLiveData<Currency>()
    val jpy: LiveData<Currency> = _jpy

    private val _infoMessage = MutableLiveData<InfoMessage>()
    val infoMessage: LiveData<InfoMessage> = _infoMessage

    private val _error = MutableLiveData<Unit>()
    val error: LiveData<Unit> = _error

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _eurCommission = MutableLiveData<BigDecimal>()
    val eurCommission: LiveData<BigDecimal> = _eurCommission

    private val _usdCommission = MutableLiveData<BigDecimal>()
    val usdCommission: LiveData<BigDecimal> = _usdCommission

    private val _jpyCommission = MutableLiveData<BigDecimal>()
    val jpyCommission: LiveData<BigDecimal> = _jpyCommission

    private val eurCommissionValue = 0.toBigDecimal()
    private val usdCommissionValue = 0.toBigDecimal()
    private val jpyCommissionValue = 0.toBigDecimal()

    init {
        _eur.postValue(initialBalanceProvider.getInitialEurBalance())
        _eurCommission.postValue(eurCommissionValue)


        _usd.postValue(initialBalanceProvider.getInitialUsdBalance())
        _usdCommission.postValue(usdCommissionValue)


        _jpy.postValue(initialBalanceProvider.getInitialJpyBalance())
        _jpyCommission.postValue(jpyCommissionValue)
    }

    private val currencies = arrayOf(
        initialBalanceProvider.getInitialEurBalance(),
        initialBalanceProvider.getInitialUsdBalance(),
        initialBalanceProvider.getInitialJpyBalance()
    )
    private val currenciesLiveData = arrayOf(_eur, _usd, _jpy)
    private val commissions = arrayOf(
        eurCommissionValue,
        usdCommissionValue,
        jpyCommissionValue
    )
    private val commissionsLiveData = arrayOf(
        _eurCommission,
        _usdCommission,
        _jpyCommission
    )

    private var amountToConvert = (-1).toBigDecimal()
    var indexFrom = -1
    var indexTo = -1
    var numberOfOperations = 0
    var thisCommission: BigDecimal = 0.toBigDecimal()
    private var url = ""

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun launchDataLoad() {
        coroutineScope.launch {
            fetchData()
        }
    }

    private suspend fun fetchData() = withContext(Dispatchers.Default) {
        try {
            withContext(Dispatchers.IO) {
                val response = currencyService.getCurrencyAsync(url).await()
                response.body()?.let { body ->
                    calculateValues(body)
                    makeInfoMessage(body)
                }
                if (response.body() == null) {
                    _error.postValue(Unit)
                }
            }

        } catch (e: Exception) {
            _error.postValue(Unit)
        }
    }

    fun makeUrl() {
        url =
            "$amountToConvert-${(currencies[indexFrom]).currencyCode}/${(currencies[indexTo]).currencyCode}/latest"
    }

    private fun calculateValues(currency: Currency) {

        currencies[indexFrom].balanceValue =
            currencies[indexFrom].balanceValue.minus(amountToConvert).minus(thisCommission)
        currencies[indexFrom].currencyCode = currencies[indexFrom].currencyCode

        currencies[indexTo].balanceValue =
            currencies[indexTo].balanceValue.plus(currency.balanceValue)
        currencies[indexTo].currencyCode = currencies[indexTo].currencyCode

        commissions[indexFrom] = commissions[indexFrom].plus(thisCommission)

        // force postValue to notify Observers
        // postValue posts a task to a main thread to set the given values
        currenciesLiveData[indexFrom].postValue(currencies[indexFrom])
        currenciesLiveData[indexTo].postValue(currencies[indexTo])
        commissionsLiveData[indexFrom].postValue(commissions[indexFrom])
    }

    fun calculateCommission(amountToConvert: BigDecimal, currencyFrom: Int, currencyTo: Int) {
        this.amountToConvert = amountToConvert
        this.indexFrom = currencyFrom
        this.indexTo = currencyTo
        //no extra conditions
        thisCommission = commissionCalculator.calculate(amountToConvert, numberOfOperations)

        //Error check
        if (amountToConvert < 0.toBigDecimal()) {
            _errorMessage.postValue(errorMessageProvider.getMissingAmountError())
            return
        } else if (indexTo == -1 || indexFrom == -1 || indexFrom == indexTo) {
            _errorMessage.postValue(errorMessageProvider.getTwoDifferentCurrenciesError())
            return
        } else if (amountToConvert + thisCommission > currencies[indexFrom].balanceValue) {
            _errorMessage.postValue(errorMessageProvider.getInsufficientFundsError())
            return
        }

        numberOfOperations++
        //if no errors
        makeUrl()
        launchDataLoad()
    }

    private fun makeInfoMessage(currency: Currency) {
        _infoMessage.postValue(
            InfoMessage(
                amountToConvert,
                currencies[indexFrom].currencyCode,
                currency.balanceValue,
                currencies[indexTo].currencyCode,
                thisCommission,
                currencies[indexFrom].currencyCode
            )
        )
    }
}