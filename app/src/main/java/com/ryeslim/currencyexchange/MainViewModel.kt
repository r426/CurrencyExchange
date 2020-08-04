package com.ryeslim.currencyexchange


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryeslim.currencyexchange.commission.CommissionCalculator
import com.ryeslim.currencyexchange.dataclass.Currency
import com.ryeslim.currencyexchange.dataclass.InfoMessage
import com.ryeslim.currencyexchange.dataclass.SelectedCurrency
import com.ryeslim.currencyexchange.utils.error.ErrorMessageProvider
import com.ryeslim.currencyexchange.utils.initial.InitialBalanceProvider
import com.ryeslim.currencyexchange.utils.initial.InitialCommissionProvider
import kotlinx.coroutines.*
import java.math.BigDecimal

class MainViewModel(
    private val currencyService: CurrencyApi,
    private val commissionCalculator: CommissionCalculator,
    private val errorMessageProvider: ErrorMessageProvider,
    initialBalanceProvider: InitialBalanceProvider,
    initialCommissionProvider: InitialCommissionProvider
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

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _eurCommission = MutableLiveData<BigDecimal>()
    val eurCommission: LiveData<BigDecimal> = _eurCommission

    private val _usdCommission = MutableLiveData<BigDecimal>()
    val usdCommission: LiveData<BigDecimal> = _usdCommission

    private val _jpyCommission = MutableLiveData<BigDecimal>()
    val jpyCommission: LiveData<BigDecimal> = _jpyCommission


    private var eurBalance = initialBalanceProvider.getInitialEurBalance()
    private var usdBalance = initialBalanceProvider.getInitialUsdBalance()
    private var jpyBalance = initialBalanceProvider.getInitialJpyBalance()


    private var eurComm = initialCommissionProvider.getInitialEurCommission()
    private var usdComm = initialCommissionProvider.getInitialUsdCommission()
    private var jpyComm = initialCommissionProvider.getInitialJpyCommission()

    init {
        _eur.postValue(eurBalance)
        _eurCommission.postValue(eurComm)

        _usd.postValue(usdBalance)
        _usdCommission.postValue(usdComm)

        _jpy.postValue(jpyBalance)
        _jpyCommission.postValue(jpyComm)
    }

    private lateinit var currencyLiveDataFrom: MutableLiveData<Currency>
    private lateinit var currencyLiveDataTo: MutableLiveData<Currency>
    private lateinit var commissionLiveDataFrom: MutableLiveData<BigDecimal>

    private var amountToConvert = (-1).toBigDecimal()
    private var selectedCurrencyFrom: SelectedCurrency? = null
    private var selectedCurrencyTo: SelectedCurrency? = null

    private var currencyFrom: Currency = Currency(0.toBigDecimal(), "")
    private var currencyTo: Currency = Currency(0.toBigDecimal(), "")
    var numberOfOperations = 1
    var commissionFrom = 0.toBigDecimal()
    var thisCommission = 0.toBigDecimal()

    private var url = ""

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private fun launchDataLoad() {
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
                    numberOfOperations++
                }
                if (response.body() == null) {
                    _errorMessage.postValue(errorMessageProvider.getGenericError())
                }
            }

        } catch (e: Exception) {
            _errorMessage.postValue(errorMessageProvider.getGenericError())
        }
    }

    private fun makeUrl() {
        url =
            "$amountToConvert-${(selectedCurrencyFrom)}/${(selectedCurrencyTo)}/latest"
    }

    private fun calculateValues(currency: Currency) {

        currencyFrom.balanceValue =
            currencyFrom.balanceValue.minus(amountToConvert).minus(thisCommission)
        currencyFrom.currencyCode = currencyFrom.currencyCode

        currencyTo.balanceValue =
            currencyTo.balanceValue.plus(currency.balanceValue)
        currencyTo.currencyCode = currencyTo.currencyCode

        commissionFrom = commissionFrom.plus(thisCommission)

        // force postValue to notify Observers
        // postValue posts a task to a main thread to set the given values
        currencyLiveDataFrom.postValue(currencyFrom)
        currencyLiveDataTo.postValue(currencyTo)
        commissionLiveDataFrom.postValue(commissionFrom)
    }

    fun convert(
        amountToConvert: BigDecimal,
        selectedCurrencyFrom: SelectedCurrency?,
        selectedCurrencyTo: SelectedCurrency?
    ) {
        this.amountToConvert = amountToConvert
        this.selectedCurrencyFrom = selectedCurrencyFrom
        this.selectedCurrencyTo = selectedCurrencyTo

        if (noInputErrors()) {
            when (selectedCurrencyFrom) {
                SelectedCurrency.EUR -> {
                    currencyFrom = eurBalance
                    currencyLiveDataFrom = _eur
                    commissionFrom = eurComm
                    commissionLiveDataFrom = _eurCommission
                }
                SelectedCurrency.USD -> {
                    currencyFrom = usdBalance
                    currencyLiveDataFrom = _usd
                    commissionFrom = usdComm
                    commissionLiveDataFrom = _usdCommission
                }
                SelectedCurrency.JPY -> {
                    currencyFrom = jpyBalance
                    currencyLiveDataFrom = _jpy
                    commissionFrom = jpyComm
                    commissionLiveDataFrom = _jpyCommission
                }
            }
            when (selectedCurrencyTo) {
                SelectedCurrency.EUR -> {
                    currencyTo = eurBalance
                    currencyLiveDataTo = _eur
                }
                SelectedCurrency.USD -> {
                    currencyTo = usdBalance
                    currencyLiveDataTo = _usd
                }
                SelectedCurrency.JPY -> {
                    currencyTo = jpyBalance
                    currencyLiveDataTo = _jpy
                }
            }
            calculateCommission()
            if (sufficientFunds()) {
                makeUrl()
                launchDataLoad()
            }
        }
    }

    private fun noInputErrors(): Boolean {
        return if (
            selectedCurrencyFrom == null ||
            selectedCurrencyTo == null ||
            selectedCurrencyFrom == selectedCurrencyTo
        ) {
            _errorMessage.postValue(errorMessageProvider.getTwoDifferentCurrenciesError())
            false
        } else if (amountToConvert < 0.toBigDecimal()) {
            _errorMessage.postValue(errorMessageProvider.getMissingAmountError())
            false
        } else true
    }

    private fun sufficientFunds(): Boolean {
        return if (amountToConvert + thisCommission > currencyFrom.balanceValue) {
            _errorMessage.postValue(errorMessageProvider.getInsufficientFundsError())
            false
        } else true
    }

    private fun calculateCommission() {
        //no extra conditions
        thisCommission = commissionCalculator.calculate(amountToConvert, numberOfOperations)
    }

    private fun makeInfoMessage(currency: Currency) {
        _infoMessage.postValue(
            InfoMessage(
                amountToConvert,
                currencyFrom.currencyCode,
                currency.balanceValue,
                currencyTo.currencyCode,
                thisCommission,
                currencyFrom.currencyCode
            )
        )
    }
}