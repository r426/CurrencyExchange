package com.ryeslim.currencyexchange


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryeslim.currencyexchange.retrofit.ServiceFactory
import com.ryeslim.currencyexchange.dataclass.Currency
import com.ryeslim.currencyexchange.dataclass.InfoMessage
import kotlinx.coroutines.*
import java.math.BigDecimal

class MainViewModel : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var response: retrofit2.Response<Currency>? = null

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

    private val _eurCommission = MutableLiveData<BigDecimal>()
    val eurCommission: LiveData<BigDecimal> = _eurCommission

    private val _usdCommission = MutableLiveData<BigDecimal>()
    val usdCommission: LiveData<BigDecimal> = _usdCommission

    private val _jpyCommission = MutableLiveData<BigDecimal>()
    val jpyCommission: LiveData<BigDecimal> = _jpyCommission

    private val eurValue = Currency(
        1000.toBigDecimal(),
        "EUR"
    )

    private val usdValue = Currency(
        0.toBigDecimal(),
        "USD"
    )

    private val jpyValue = Currency(
        0.toBigDecimal(),
        "JPY"
    )

    private val eurCommissionValue = 0.toBigDecimal()
    private val usdCommissionValue = 0.toBigDecimal()
    private val jpyCommissionValue = 0.toBigDecimal()

    init {
        _eur.postValue(eurValue)
        _eurCommission.postValue(eurCommissionValue)


        _usd.postValue(usdValue)
        _usdCommission.postValue(usdCommissionValue)


        _jpy.postValue(jpyValue)
        _jpyCommission.postValue(jpyCommissionValue)
    }

    val currencies = arrayOf(eurValue, usdValue, jpyValue)
    val currenciesLiveData = arrayOf(_eur, _usd, _jpy)
    val commissions = arrayOf(eurCommissionValue, usdCommissionValue, jpyCommissionValue)
    val commissionsLiveData = arrayOf(_eurCommission, _usdCommission, _jpyCommission)

    var amountToConvert = (-1).toBigDecimal()
    var indexFrom = -1
    var indexTo = -1
    var numberOfOperations = 0
    var thisCommission: BigDecimal = 0.toBigDecimal()
    private var commission = SevenPercent()
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
                response = ServiceFactory.createRetrofitService(
                    CurrencyApi::class.java,
                    "http://api.evp.lt/currency/commercial/exchange/"
                )
                    .getCurrencyAsync(url).await()
            }

            if (response!!.body() != null) {
                calculateValues()
                makeInfoMessage()
            } else {
                _error.postValue(Unit)
            }

        } catch (e: Exception) {
            _error.postValue(Unit)
        }
    }

    fun makeUrl() {
        url =
            "$amountToConvert-${(currencies[indexFrom]).currencyCode}/${(currencies[indexTo]).currencyCode}/latest"
    }

    private fun calculateValues() {

        val tempCurrencyFrom = Currency(0.toBigDecimal(), "")
        val tempCurrencyTo = Currency(0.toBigDecimal(), "")
        val tempCommission: BigDecimal?

        tempCurrencyFrom.balanceValue =
            currencies[indexFrom].balanceValue.minus(amountToConvert).minus(thisCommission)
        tempCurrencyFrom.currencyCode = currencies[indexFrom].currencyCode

        tempCurrencyTo.balanceValue =
            currencies[indexTo].balanceValue.plus(response!!.body()!!.balanceValue)
        tempCurrencyTo.currencyCode = currencies[indexTo].currencyCode

        tempCommission = commissions[indexFrom].plus(thisCommission)

        // force postValue to notify Observers
        // postValue posts a task to a main thread to set the given values
        currenciesLiveData[indexFrom].postValue(tempCurrencyFrom)
        currenciesLiveData[indexTo].postValue(tempCurrencyTo)
        commissionsLiveData[indexFrom].postValue(tempCommission)
    }

    fun calculateCommission() {
        //no extra conditions
        thisCommission = commission.calculate(amountToConvert, numberOfOperations)
    }

    private fun makeInfoMessage() {
        _infoMessage.postValue(
            InfoMessage(
                amountToConvert,
                currencies[indexFrom].currencyCode,
                response!!.body()!!.balanceValue,
                currencies[indexTo].currencyCode,
                thisCommission,
                currencies[indexFrom].currencyCode
            )
        )
    }
}