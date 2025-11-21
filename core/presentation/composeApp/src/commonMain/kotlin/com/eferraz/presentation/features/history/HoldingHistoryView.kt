package com.eferraz.presentation.features.history

import androidx.compose.runtime.Composable
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.FixedIncomeAssetType
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset
import com.eferraz.presentation.features.assets.AssetView
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.toPercentage
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format

internal class HoldingHistoryView(
    val brokerage: String,
    val category: String,
    val subCategory: String,
    val description: String,
    val observations: String,
    val maturity: LocalDate?,
    val issuer: String,
    val liquidity: String,
    val previousQuantity: Double?,
    val currentQuantity: Double?,
    val previousValue: Double?,
    val currentValue: Double?,
    val appreciation: String,
    val situation: String,
    val currentEntryId: Long?,
    val holdingId: Long,
) {

    companion object {

        @Composable
        fun create(
            holding: AssetHolding,
            currentEntry: HoldingHistoryEntry?,
            previousEntry: HoldingHistoryEntry?,
        ): HoldingHistoryView {

            val assetView = AssetView.create(holding.asset)

            val previousQuantity = previousEntry?.endOfMonthQuantity
            val previousValue = previousEntry?.endOfMonthValue

            val currentQuantity = currentEntry?.endOfMonthQuantity
            val currentValue = currentEntry?.endOfMonthValue

            return HoldingHistoryView(
                brokerage = holding.brokerage.name,
                category = assetView.category,
                subCategory =assetView.subCategory,
                description = assetView.name,
                observations = assetView.notes,
                maturity = assetView.maturity,
                issuer = assetView.issuer,
                liquidity = assetView.liquidity,
                previousQuantity = previousQuantity,
                currentQuantity = currentQuantity,
                previousValue = previousValue,
                currentValue = currentValue,
                appreciation = formatAppreciation(currentValue, previousValue),
                situation = formatSituation(
                    previousQuantity = previousQuantity,
                    currentQuantity = currentQuantity,
                    hasCurrentEntry = currentEntry != null
                ),
                currentEntryId = currentEntry?.id,
                holdingId = holding.id,
            )
        }

        internal fun formatAppreciation(
            currentValue: Double?,
            previousValue: Double?,
        ): String {
            if (previousValue == null || previousValue <= 0.0) return "—"
            if (currentValue == null || currentValue == 0.0) return "—"
            val appreciation = ((currentValue / previousValue) - 1.0) * 100.0
            return appreciation.toPercentage()
        }

        internal fun formatSituation(
            previousQuantity: Double?,
            currentQuantity: Double?,
            hasCurrentEntry: Boolean,
        ): String {
            if (!hasCurrentEntry && previousQuantity != null && previousQuantity > 0) {
                return "Não Registrado"
            }
            if (previousQuantity == null || previousQuantity == 0.0) {
                return if (currentQuantity != null && currentQuantity > 0) "Compra" else "—"
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

