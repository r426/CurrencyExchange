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
import java.math.BigDecimal
import java.math.RoundingMode


class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(createCurrencyService(), createCommissionCalculator())
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
        viewModel.error.observe(this, Observer { showErrorMessage() })
        binding.convert.setOnClickListener { manageConversion() }
    }

    private fun showErrorMessage() {
        binding.infoMessage.text = getString(R.string.error_message)
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
        var errorMessage: String? = null

        val amountToConvert = getAmountToConvert()
        val currencyFrom = getCurrencyFrom()
        val currencyTo = getCurrencyTo()
        viewModel.calculateCommission(amountToConvert, currencyFrom, currencyTo)

        //Error check
        if (viewModel.amountToConvert < 0.toBigDecimal()) {
            errorMessage = getString(R.string.enter_the_amount)
        } else if (binding.radioGroupFrom.checkedRadioButtonId == -1
            || binding.radioGroupTo.checkedRadioButtonId == -1
            || viewModel.indexFrom == viewModel.indexTo
        ) {
            errorMessage = getString(R.string.radio_button_error)
        } else if (viewModel.amountToConvert + viewModel.thisCommission > viewModel.currencies[viewModel.indexFrom].balanceValue) {
            errorMessage = getString(R.string.insufficient_funds)
        }

        //Error message
        if (errorMessage != null) {
            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.numberOfOperations--
        } else {
            //if no errors
            viewModel.makeUrl()
            viewModel.launchDataLoad()
            clearButtons()
        }
    }

    private fun clearButtons() {
        binding.radioGroupFrom.clearCheck()
        binding.radioGroupTo.clearCheck()
        binding.amountToConvert.text.clear()
    }
}