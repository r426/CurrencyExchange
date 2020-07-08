package com.ryeslim.currencyexchangeretrofitcoroutines


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryeslim.currencyexchangeretrofitcoroutines.retrofit.ServiceFactory
import com.ryeslim.currencyexchangeretrofitcoroutines.dataclass.Currency
import kotlinx.coroutines.*
import java.math.BigDecimal

class MainViewModel : ViewModel() {

  private val viewModelJob = SupervisorJob()

  private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

  private var response: retrofit2.Response<Currency>? = null

  private val _eur = MutableLiveData<Currency>()
  val eur: LiveData<Currency>
    get() = _eur

  private val _usd = MutableLiveData<Currency>()
  val usd: LiveData<Currency>
    get() = _usd

  private val _jpy = MutableLiveData<Currency>()
  val jpy: LiveData<Currency>
    get() = _jpy

  private val _infoMessage = MutableLiveData<String>()
  val infoMessage: LiveData<String>
    get() = _infoMessage

  private val _eurCommission = MutableLiveData<BigDecimal>()
  val eurCommission: LiveData<BigDecimal>
    get() = _eurCommission

  private val _usdCommission = MutableLiveData<BigDecimal>()
  val usdCommission: LiveData<BigDecimal>
    get() = _usdCommission

  private val _jpyCommission = MutableLiveData<BigDecimal>()
  val jpyCommission: LiveData<BigDecimal>
    get() = _jpyCommission

  init {
    _eur.value = Currency(
        1000.toBigDecimal(),
        "EUR"
    )
    _eurCommission.value = 0.toBigDecimal()

    _usd.value = Currency(
        0.toBigDecimal(),
        "USD"
    )
    _usdCommission.value = 0.toBigDecimal()

    _jpy.value = Currency(
        0.toBigDecimal(),
        "JPY"
    )
    _jpyCommission.value = 0.toBigDecimal()

    _infoMessage.value = ""
  }

  val currencies = arrayOf(_eur, _usd, _jpy)
  private val commissions = arrayOf(_eurCommission, _usdCommission, _jpyCommission)

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

  suspend private fun fetchData() = withContext(Dispatchers.Default) {
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
        _infoMessage.postValue("Error")
      }

    } catch (e: Exception) {
      e.stackTrace
      _infoMessage.postValue("Error")
    }
  }

  fun makeUrl() {
    url =
        "$amountToConvert-${(currencies[indexFrom].value)?.currencyCode}/${(currencies[indexTo].value)?.currencyCode}/latest"
  }

  private fun calculateValues() {

    val tempCurrencyFrom = Currency(0.toBigDecimal(), "")
    val tempCurrencyTo = Currency(0.toBigDecimal(), "")
    val tempCommission: BigDecimal?

    tempCurrencyFrom.balanceValue =
        currencies[indexFrom].value!!.balanceValue.minus(amountToConvert).minus(thisCommission)
    tempCurrencyFrom.currencyCode = currencies[indexFrom].value!!.currencyCode

    tempCurrencyTo.balanceValue =
        currencies[indexTo].value!!.balanceValue.plus(response!!.body()!!.balanceValue)
    tempCurrencyTo.currencyCode = currencies[indexTo].value!!.currencyCode

    tempCommission = commissions[indexFrom].value!!.plus(thisCommission)

    // force postValue to notify Observers
    // postValue posts a task to a main thread to set the given values
    currencies[indexFrom].postValue(tempCurrencyFrom)
    currencies[indexTo].postValue(tempCurrencyTo)
    commissions[indexFrom].postValue(tempCommission)
  }

  fun calculateCommission() {
    //no extra conditions
    thisCommission = commission.calculate(amountToConvert, numberOfOperations)

    //conversions under 200 free
    //thisCommission = commission.calculate(amountToConvert, numberOfOperations, amountFrom <= 200.toBigDecimal())

    //every 10th conversion free
    //thisCommission = commission.calculate(amountToConvert, numberOfOperations, numberOfOperations % 10 == 0)
  }

  private fun makeInfoMessage() {
    _infoMessage.postValue(
        String.format(
            "You converted %.2f %s to %.2f %s. Commission paid: %.2f %s",
            amountToConvert,
            currencies[indexFrom].value?.currencyCode,
            response!!.body()!!.balanceValue,
            currencies[indexTo].value?.currencyCode,
            thisCommission,
            currencies[indexFrom].value?.currencyCode
        )
    )
  }
}