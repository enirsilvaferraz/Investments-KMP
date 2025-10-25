package com.eferraz.pokedex.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class FixedIncomeInvestmentViewModel(
    data: FixedIncomeView,
) : ViewModel() {

    private val _formData = MutableStateFlow(data)
    val formData = _formData.asStateFlow()

    fun onEvent(event: Event, value: String) {
        when (event) {
            Event.UPDATE_BROKERAGE_FIRM -> _formData.update { it.copy(brokerageFirm = value) }
            Event.UPDATE_INVESTED_AMOUNT -> _formData.update { it.copy(investedAmount = value) }
            Event.UPDATE_PURCHASE_DATE -> _formData.update { it.copy(purchaseDate = value) }
            Event.UPDATE_MATURITY_DATE -> _formData.update { it.copy(maturityDate = value) }
            Event.UPDATE_PROFITABILITY -> _formData.update { it.copy(profitability = value) }
            Event.UPDATE_INDEX -> _formData.update { it.copy(profitabilityIndex = value) }
            Event.UPDATE_INVESTMENT_TYPE -> _formData.update { it.copy(type = value) }
            Event.UPDATE_LIQUIDITY -> _formData.update { it.copy(liquidity = value) }
            Event.UPDATE_ISSUER_BANK -> _formData.update { it.copy(issuerBank = value) }
        }
    }

    enum class Event {
        UPDATE_BROKERAGE_FIRM,
        UPDATE_INVESTED_AMOUNT,
        UPDATE_PURCHASE_DATE,
        UPDATE_MATURITY_DATE,
        UPDATE_PROFITABILITY,
        UPDATE_INDEX,
        UPDATE_INVESTMENT_TYPE,
        UPDATE_LIQUIDITY,
        UPDATE_ISSUER_BANK
    }
}