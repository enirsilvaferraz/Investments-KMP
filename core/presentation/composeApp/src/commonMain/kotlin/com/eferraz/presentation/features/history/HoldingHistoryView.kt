package com.eferraz.presentation.features.history

import androidx.compose.runtime.Composable
import com.eferraz.entities.AssetHolding
import com.eferraz.entities.HoldingHistoryEntry
import com.eferraz.presentation.features.assets.AssetView
import com.eferraz.presentation.helpers.Formatters.formated
import com.eferraz.presentation.helpers.currencyFormat
import com.eferraz.presentation.helpers.toPercentage
import kotlinx.datetime.LocalDate

internal class HoldingHistoryView(
    val formatted: Formatted,
    val sort: Sort,
    val currentEntryId: Long?,
    val holdingId: Long,
) {

    data class Formatted(
        val brokerage: String,
        val category: String,
        val subCategory: String,
        val description: String,
        val observations: String,
        val maturity: String,
        val issuer: String,
        val previousValue: String,
        val currentValue: String,
        val appreciation: String,
        val situation: String,
    )

    data class Sort(
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
        val situation: String,
    )

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
                formatted = Formatted(
                    brokerage = holding.brokerage.name,
                    category = assetView.category,
                    subCategory = assetView.subCategory,
                    description = assetView.name,
                    observations = assetView.notes,
                    maturity = assetView.maturity.formated(),
                    issuer = assetView.issuer,
                    previousValue = (previousValue ?: 0.0).currencyFormat(),
                    currentValue = (currentValue ?: 0.0).currencyFormat(),
                    appreciation = formatAppreciation(currentValue, previousValue),
                    situation = formatSituation(
                        previousQuantity = previousQuantity,
                        currentQuantity = currentQuantity,
                        hasCurrentEntry = currentEntry != null
                    )
                ),
                sort = Sort(
                    brokerage = holding.brokerage.name,
                    category = assetView.category,
                    subCategory = assetView.subCategory,
                    description = assetView.name,
                    observations = assetView.notes,
                    maturity = assetView.maturity,
                    issuer = assetView.issuer,
                    previousValue = previousValue ?: 0.0,
                    currentValue = currentValue ?: 0.0,
                    appreciation = formatAppreciation(currentValue, previousValue),
                    situation = formatSituation(
                        previousQuantity = previousQuantity,
                        currentQuantity = currentQuantity,
                        hasCurrentEntry = currentEntry != null
                    )
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

