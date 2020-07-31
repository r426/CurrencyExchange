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
import com.ryeslim.currencyexchange.retrofit.ServiceFactory
import com.ryeslim.currencyexchange.utils.error.ErrorMessageProvider
import java.math.BigDecimal
import java.math.RoundingMode


class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            createCurrencyService(),
            createCommissionCalculator(),
            createErrorMessageProvider()
        )
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
        viewModel.error.observe(this, Observer { showError() })
        viewModel.errorMessage.observe(this, Observer { message -> showErrorMessage(message) })

        binding.convert.setOnClickListener { manageConversion() }
    }

    private fun showError() {
        binding.infoMessage.text = getString(R.string.error_message)
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

    private fun getCurrencyFrom(): Int {
        return when (binding.radioGroupFrom.checkedRadioButtonId) {
            binding.fromEur.id -> 0
            binding.fromUsd.id -> 1
            binding.fromJpy.id -> 2
            else -> -1
        }
    }

    private fun getCurrencyTo(): Int {
        return when (binding.radioGroupTo.checkedRadioButtonId) {
            binding.toEur.id -> 0
            binding.toUsd.id -> 1
            binding.toJpy.id -> 2
            else -> -1
        }
    }

    private fun manageConversion() {
        val amountToConvert = getAmountToConvert()
        val currencyFrom = getCurrencyFrom()
        val currencyTo = getCurrencyTo()
        viewModel.calculateCommission(amountToConvert, currencyFrom, currencyTo)
    }

    private fun clearButtons() {
        binding.radioGroupFrom.clearCheck()
        binding.radioGroupTo.clearCheck()
        binding.amountToConvert.text.clear()
    }
}