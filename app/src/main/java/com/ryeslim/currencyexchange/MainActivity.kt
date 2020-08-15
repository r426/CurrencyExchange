package com.ryeslim.currencyexchange

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ryeslim.currencyexchange.commission.CommissionCalculator
import com.ryeslim.currencyexchange.commission.SevenPercentCommissionCalculator
import com.ryeslim.currencyexchange.databinding.ActivityMainBinding
import com.ryeslim.currencyexchange.dataclass.InfoMessage
import com.ryeslim.currencyexchange.dataclass.SelectedCurrency
import com.ryeslim.currencyexchange.retrofit.ServiceFactory
import com.ryeslim.currencyexchange.utils.error.ErrorMessageProvider
import com.ryeslim.currencyexchange.utils.initial.InitialBalanceProvider
import com.ryeslim.currencyexchange.utils.initial.InitialCommissionProvider
import java.math.BigDecimal
import java.math.RoundingMode


class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            createCurrencyService(),
            createCommissionCalculator(),
            createErrorMessageProvider(),
            createInitialBalanceProvider(),
            createInitialCommissionProvider()
        )
    }

    private fun createInitialCommissionProvider(): InitialCommissionProvider {
        return InitialCommissionProvider()
    }

    private fun createInitialBalanceProvider(): InitialBalanceProvider {
        return InitialBalanceProvider()
    }

    private fun createErrorMessageProvider(): ErrorMessageProvider {
        return ErrorMessageProvider(this)
    }

    private fun createCommissionCalculator(): CommissionCalculator {
        return SevenPercentCommissionCalculator()
    }

    private fun createCurrencyService(): CurrencyApi {
        return ServiceFactory.createRetrofitService(
            CurrencyApi::class.java,
            "http://api.evp.lt/currency/commercial/exchange/"
        )
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Setting up LiveData observation relationship
        viewModel.eur.observe(this, Observer { newEur ->
            binding.eurBalanceValue.text = String.format("%.2f", newEur.balanceValue)
        })
        viewModel.eurCommission.observe(this, Observer { newEurCommission ->
            binding.eurCommissionsValue.text = String.format("%.2f", newEurCommission)
        })
        viewModel.usd.observe(this, Observer { newUsd ->
            binding.usdBalanceValue.text = String.format("%.2f", newUsd.balanceValue)
        })
        viewModel.usdCommission.observe(this, Observer { newUsdCommission ->
            binding.usdCommissionsValue.text = String.format("%.2f", newUsdCommission)
        })
        viewModel.jpy.observe(this, Observer { newJpy ->
            binding.jpyBalanceValue.text = String.format("%.2f", newJpy.balanceValue)
        })
        viewModel.jpyCommission.observe(this, Observer { newJpyCommission ->
            binding.jpyCommissionsValue.text = String.format("%.2f", newJpyCommission)
        })
        viewModel.infoMessage.observe(this, Observer { newInfoMessage ->
            showInfoMessage(newInfoMessage)
        })
        viewModel.errorMessage.observe(this, Observer { newErrorMessage ->
            showErrorMessage(newErrorMessage)
        })

        binding.convert.setOnClickListener { manageConversion() }
    }

    private fun showErrorMessage(errorMessage: String) {
        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
        clearButtons()
    }

    private fun showInfoMessage(newInfoMessage: InfoMessage) {
        binding.infoMessage.text =
            getString(
                R.string.info_message,
                newInfoMessage.amountToConvert,
                newInfoMessage.currencyCodeFrom,
                newInfoMessage.balance,
                newInfoMessage.balanceCurrencyCode,
                newInfoMessage.commission,
                newInfoMessage.commissionCurrencyCode
            )
        clearButtons()
    }

    private fun getAmountToConvert(): BigDecimal {
        return if (binding.amountToConvert.text.toString().trim().isNotEmpty()) {
            binding.amountToConvert.text.toString().toBigDecimal()
                .setScale(2, RoundingMode.HALF_EVEN)
        } else {
            (-1).toBigDecimal()
        }
    }

    private fun getCurrencyFrom(): SelectedCurrency? {
        return when (binding.radioGroupFrom.checkedRadioButtonId) {
            binding.fromEur.id -> SelectedCurrency.EUR
            binding.fromUsd.id -> SelectedCurrency.USD
            binding.fromJpy.id -> SelectedCurrency.JPY
            else -> null
        }
    }

    private fun getCurrencyTo(): SelectedCurrency? {
        return when (binding.radioGroupTo.checkedRadioButtonId) {
            binding.toEur.id -> SelectedCurrency.EUR
            binding.toUsd.id -> SelectedCurrency.USD
            binding.toJpy.id -> SelectedCurrency.JPY
            else -> null
        }
    }

    private fun manageConversion() {
        val amountToConvert = getAmountToConvert()
        val selectedCurrencyFrom = getCurrencyFrom()
        val selectedCurrencyTo = getCurrencyTo()
        viewModel.convert(amountToConvert, selectedCurrencyFrom, selectedCurrencyTo)
    }

    private fun clearButtons() {
        binding.radioGroupFrom.clearCheck()
        binding.radioGroupTo.clearCheck()
        binding.amountToConvert.text.clear()
    }
}