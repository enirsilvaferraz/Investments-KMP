package com.eferraz.presentation.features.history

import androidx.compose.runtime.Composable
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.presentation.features.assets.displayCategory
import com.eferraz.presentation.features.assets.displayName
import com.eferraz.presentation.features.assets.displaySubCategory
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.presentation.helpers.toPercentage
import kotlinx.datetime.LocalDate

internal class HoldingHistoryRow(
    val viewData: ViewData,
    val formatted: Formatted,
    val currentHistory: HoldingHistoryEntry,
) {

    data class Formatted(
        val maturity: String,
        val previousValue: String,
        val currentValue: String,
    )

    data class ViewData(
        val brokerage: String,
        val category: String,
        val subCategory: String,
        val description: String,
        val observations: String,
        val maturity: LocalDate?,
        val issuer: String,
        val previousValue: Double,
        val currentValue: Double,
        val appreciation: String,
        val appreciationValue: Double?, // Valor numérico para cálculo de cores
        val situation: String,
        val editable: Boolean,
    )

    companion object Companion {

        @Composable
        fun create(
            holding: AssetHolding,
            currentEntry: HoldingHistoryEntry,
            previousEntry: HoldingHistoryEntry,
        ): HoldingHistoryRow {

            val asset = holding.asset
            val maturity = when (asset) {
                is FixedIncomeAsset -> asset.expirationDate
                is InvestmentFundAsset -> asset.expirationDate
                is VariableIncomeAsset -> null
                else -> null
            }

            val previousValue = previousEntry.endOfMonthValue * previousEntry.endOfMonthQuantity
            val currentValue = currentEntry.endOfMonthValue * currentEntry.endOfMonthQuantity

            val appreciationValue = calculateAppreciationValue(currentValue, previousValue)

            val viewData = ViewData(
                brokerage = holding.brokerage.name,
                category = asset.displayCategory(),
                subCategory = asset.displaySubCategory(),
                description = asset.displayName(),
                observations = asset.observations.orEmpty(),
                maturity = maturity,
                issuer = asset.issuer.name,
                previousValue = previousValue,
                currentValue = currentValue,
                appreciation = formatAppreciation(currentValue, previousValue),
                appreciationValue = appreciationValue,
//                situation = formatSituation(
//                    previousQuantity = previousQuantity,
//                    currentQuantity = currentQuantity
//                ),
                situation = "",
                editable = holding.asset !is VariableIncomeAsset
            )

            val formatted = Formatted(
                maturity = maturity.formated(),
                previousValue = previousValue.currencyFormat(),
                currentValue = currentValue.currencyFormat(),
            )

            return HoldingHistoryRow(
                formatted = formatted,
                viewData = viewData,
                currentHistory = currentEntry,
            )
        }

        internal fun formatAppreciation(
            currentValue: Double?,
            previousValue: Double?,
        ): String {
            if (previousValue == null || previousValue <= 0.0) return ""
            if (currentValue == null || currentValue == 0.0) return ""
            val appreciation = ((currentValue / previousValue) - 1.0) * 100.0
            return appreciation.toPercentage()
        }

        internal fun calculateAppreciationValue(
            currentValue: Double?,
            previousValue: Double?,
        ): Double? {
            if (previousValue == null || previousValue <= 0.0) return null
            if (currentValue == null || currentValue == 0.0) return null
            return ((currentValue / previousValue) - 1.0) * 100.0
        }

        internal fun formatSituation(
            previousQuantity: Double?,
            currentQuantity: Double?,
            hasCurrentEntry: Boolean = true,
        ): String {
            if (!hasCurrentEntry && previousQuantity != null && previousQuantity > 0) {
                return "Não Registrado"
            }
            if (previousQuantity == null || previousQuantity == 0.0) {
                return if (currentQuantity != null && currentQuantity > 0) "Compra" else "-"
            }
            if (currentQuantity == null) {
                return "Não Registrado"
            }
            return when {
                currentQuantity == 0.0 -> "Venda Total"
                currentQuantity < previousQuantity -> "Venda Parcial"
                currentQuantity > previousQuantity -> "Aporte"
                currentQuantity == previousQuantity -> "Manutenção"
                else -> "—"
            }
        }
    }
}

