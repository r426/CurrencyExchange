package com.ryeslim.currencyexchangeretrofitcoroutines

import com.ryeslim.currencyexchangeretrofitcoroutines.dataclass.Currency
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface CurrencyApi {
  @GET
  fun getCurrencyAsync(@Url url: String?): Deferred<Response<Currency>>
}