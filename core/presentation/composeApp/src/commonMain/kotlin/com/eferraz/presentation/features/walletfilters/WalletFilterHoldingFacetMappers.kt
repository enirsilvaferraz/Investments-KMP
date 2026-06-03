package com.eferraz.presentation.features.walletfilters

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.YesOrNo
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.usecases.entities.B3IdentifierStatus
import com.eferraz.usecases.entities.HoldingHistoryView
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

internal fun Asset.toWalletFilterHoldingFacet(settled: YesOrNo): WalletFilterHoldingFacet =
    when (this) {
        is FixedIncomeAsset -> toWalletFilterHoldingFacet(settled)
        is VariableIncomeAsset -> toWalletFilterHoldingFacet(settled)
        is InvestmentFundAsset -> toWalletFilterHoldingFacet(settled)
    }

internal fun AssetHolding.toWalletFilterHoldingFacet(settled: YesOrNo): WalletFilterHoldingFacet =
    asset.toWalletFilterHoldingFacet(settled)

internal fun FixedIncomeAsset.toWalletFilterHoldingFacet(settled: YesOrNo): WalletFilterHoldingFacet =
    WalletFilterHoldingFacet(
        category = category,
        subtype = WalletFilterSubtype.FixedIncome(subType),
        liquidity = liquidity,
        b3Informed = b3Identifier.toYesOrNoFromPresence(),
        settled = settled,
        maturity = expirationDate.toYearMonth(),
    )

internal fun VariableIncomeAsset.toWalletFilterHoldingFacet(settled: YesOrNo): WalletFilterHoldingFacet =
    WalletFilterHoldingFacet(
        category = category,
        subtype = WalletFilterSubtype.VariableIncome(type),
        liquidity = liquidity,
        b3Informed = YesOrNo.YES,
        settled = settled,
        maturity = null,
    )

internal fun InvestmentFundAsset.toWalletFilterHoldingFacet(settled: YesOrNo): WalletFilterHoldingFacet =
    WalletFilterHoldingFacet(
        category = category,
        subtype = WalletFilterSubtype.InvestmentFund(type),
        liquidity = liquidity,
        b3Informed = YesOrNo.NO,
        settled = settled,
        maturity = expirationDate?.toYearMonth(),
    )

/** Linha de histórico → faceta; alinha B3 e liquidado com [HoldingHistoryView]. */
internal fun HoldingHistoryView.toWalletFilterHoldingFacet(): WalletFilterHoldingFacet {
    val fromAsset = entry.holding.asset.toWalletFilterHoldingFacet(settled = isLiquidated.toYesOrNo())
    return fromAsset.copy(
        b3Informed = b3IdentifierStatus.toYesOrNo(),
        liquidity = liquidity ?: fromAsset.liquidity,
    )
}

internal fun HoldingHistoryEntry.toWalletFilterHoldingFacet(): WalletFilterHoldingFacet =
    holding.asset.toWalletFilterHoldingFacet(
        settled = (endOfMonthValue == 0.0).toYesOrNo(),
    )

internal fun B3IdentifierStatus.toYesOrNo(): YesOrNo =
    when (this) {
        is B3IdentifierStatus.Informed -> YesOrNo.YES
        B3IdentifierStatus.NotInformed,
        B3IdentifierStatus.NotApplicable,
        -> YesOrNo.NO
    }

internal fun Boolean.toYesOrNo(): YesOrNo = if (this) YesOrNo.YES else YesOrNo.NO

private fun String?.toYesOrNoFromPresence(): YesOrNo =
    orEmpty().trim().isNotEmpty().toYesOrNo()

private fun LocalDate.toYearMonth(): YearMonth = YearMonth(year, month)
