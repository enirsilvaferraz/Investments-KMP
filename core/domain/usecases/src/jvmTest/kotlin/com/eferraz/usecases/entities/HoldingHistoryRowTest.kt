package com.eferraz.usecases.entities

import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAsset
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAsset
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.holdings.AssetHolding
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.entities.holdings.Owner
import com.eferraz.entities.transactions.FundsTransaction
import com.eferraz.entities.transactions.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class HoldingHistoryRowTest {

    private val issuer = Issuer(id = 1, name = "Issuer")
    private val brokerage = Brokerage(id = 1, name = "XP Investimentos")
    private val owner = Owner(id = 1, name = "Owner")

    /**
     * Fixed income row with previous snapshot and no period transactions.
     */
    @Test
    fun `GIVEN fixed income entries without transactions WHEN build THEN returns row with appreciation`() {

        // GIVEN
        val period = YearMonth(2024, Month.MARCH)
        val asset = FixedIncomeAsset(
            id = 1,
            issuer = issuer,
            indexer = YieldIndexer.PRE_FIXED,
            type = FixedIncomeAssetType.CDB,
            expirationDate = LocalDate(2030, Month.JUNE, 1),
            contractedYield = 12.0,
            liquidity = Liquidity.DAILY,
            observations = "Obs test",
            b3Identifier = "B3-123",
        )
        val holding = AssetHolding(id = 1, asset = asset, owner = owner, brokerage = brokerage)
        val previous = HoldingHistoryEntry(
            holding = holding,
            referenceDate = YearMonth(2024, Month.FEBRUARY),
            endOfMonthValue = 10.0,
            endOfMonthQuantity = 10.0,
        )
        val current = HoldingHistoryEntry(
            holding = holding,
            referenceDate = period,
            endOfMonthValue = 11.0,
            endOfMonthQuantity = 10.0,
        )

        // WHEN
        val rows = HoldingHistoryRow.build(
            period = period,
            previousEntries = listOf(previous),
            currentEntries = listOf(current),
        )

        // THEN
        assertEquals(1, rows.size)
        val row = rows.single()
        assertEquals(current, row.entry)
        assertEquals(AssetClass.FIXED_INCOME, row.assetClass)
        assertEquals(Liquidity.DAILY, row.liquidity)
        assertEquals("XP Investimentos", row.brokerageName)
        assertEquals("CDB de 12.0% a.a.", row.displayName)
        assertEquals(LocalDate(2030, Month.JUNE, 1), row.expirationDate)
        assertEquals("Obs test", row.observation)
        assertEquals(100.0, row.previousValue, 0.01)
        assertEquals(110.0, row.currentValue, 0.01)
        assertEquals(0.0, row.periodTransactionValue, 0.01)
        assertEquals(10.0, row.appreciationValue, 0.01)
        assertEquals(10.0, row.appreciationPercentage, 0.01)
        val status = assertIs<B3IdentifierStatus.Informed>(row.b3IdentifierStatus)
        assertEquals("B3-123", status.value)
        assertEquals(false, row.isLiquidated)
        assertEquals(true, row.isCurrentValueEnabled())
    }

    /**
     * Missing previous entry defaults to zero market value for appreciation base.
     */
    @Test
    fun `GIVEN current entry without previous WHEN build THEN uses default previous entry`() {

        // GIVEN
        val period = YearMonth(2024, Month.APRIL)
        val asset = VariableIncomeAsset(
            id = 2,
            name = "Petrobras",
            issuer = issuer,
            type = VariableIncomeAssetType.NATIONAL_STOCK,
            ticker = "PETR4",
        )
        val holding = AssetHolding(id = 2, asset = asset, owner = owner, brokerage = brokerage)
        val current = HoldingHistoryEntry(
            holding = holding,
            referenceDate = period,
            endOfMonthValue = 30.0,
            endOfMonthQuantity = 100.0,
        )

        // WHEN
        val row = HoldingHistoryRow.build(
            period = period,
            previousEntries = emptyList(),
            currentEntries = listOf(current),
        ).single()

        // THEN
        assertEquals(0.0, row.previousValue, 0.01)
        assertEquals(3000.0, row.currentValue, 0.01)
        val status = assertIs<B3IdentifierStatus.Informed>(row.b3IdentifierStatus)
        assertEquals("PETR4", status.value)
        assertEquals(Liquidity.D_PLUS_DAYS, row.liquidity)
        assertEquals("Ação Nacional - PETR4", row.displayName)
        assertEquals(null, row.expirationDate)
        assertEquals(false, row.isCurrentValueEnabled())
    }

    /**
     * Fixed income without B3 identifier maps to NotInformed status.
     */
    @Test
    fun `GIVEN fixed income without b3Identifier WHEN build THEN status is NotInformed`() {

        // GIVEN
        val period = YearMonth(2024, Month.MARCH)
        val asset = FixedIncomeAsset(
            id = 1,
            issuer = issuer,
            indexer = YieldIndexer.PRE_FIXED,
            type = FixedIncomeAssetType.CDB,
            expirationDate = LocalDate(2030, Month.JUNE, 1),
            contractedYield = 12.0,
            liquidity = Liquidity.DAILY,
            b3Identifier = null,
        )
        val holding = AssetHolding(id = 1, asset = asset, owner = owner, brokerage = brokerage)
        val current = HoldingHistoryEntry(holding = holding, referenceDate = period)

        // WHEN
        val row = HoldingHistoryRow.build(period, emptyList(), listOf(current)).single()

        // THEN
        assertEquals(B3IdentifierStatus.NotInformed, row.b3IdentifierStatus)
    }

    /**
     * Investment fund maps to NotInformed B3 status.
     */
    @Test
    fun `GIVEN investment fund WHEN build THEN status is NotInformed`() {

        // GIVEN
        val period = YearMonth(2024, Month.MARCH)
        val asset = InvestmentFundAsset(
            id = 3,
            name = "Fund",
            issuer = issuer,
            type = InvestmentFundAssetType.MULTIMARKET_FUND,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val holding = AssetHolding(id = 3, asset = asset, owner = owner, brokerage = brokerage)
        val current = HoldingHistoryEntry(holding = holding, referenceDate = period)

        // WHEN
        val row = HoldingHistoryRow.build(period, emptyList(), listOf(current)).single()

        // THEN
        assertEquals(B3IdentifierStatus.NotInformed, row.b3IdentifierStatus)
    }

    /**
     * Period transactions are reflected in appreciation and period transaction value.
     */
    @Test
    fun `GIVEN contribution in period WHEN build THEN isolates market appreciation`() {

        // GIVEN
        val period = YearMonth(2024, Month.MAY)
        val transactionDate = LocalDate(2024, Month.MAY, 10)
        val asset = InvestmentFundAsset(
            id = 3,
            name = "Multimercado Alpha",
            issuer = issuer,
            type = InvestmentFundAssetType.MULTIMARKET_FUND,
            liquidity = Liquidity.D_PLUS_DAYS,
        )
        val purchase = FundsTransaction(
            id = 1,
            date = transactionDate,
            type = TransactionType.PURCHASE,
            totalValue = 500.0,
        )
        val holding = AssetHolding(
            id = 3,
            asset = asset,
            owner = owner,
            brokerage = brokerage,
            transactions = listOf(purchase),
        )
        val previous = HoldingHistoryEntry(
            holding = holding,
            referenceDate = YearMonth(2024, Month.APRIL),
            endOfMonthValue = 10.0,
            endOfMonthQuantity = 100.0,
        )
        val current = HoldingHistoryEntry(
            holding = holding,
            referenceDate = period,
            endOfMonthValue = 16.0,
            endOfMonthQuantity = 100.0,
        )

        // WHEN
        val row = HoldingHistoryRow.build(
            period = period,
            previousEntries = listOf(previous),
            currentEntries = listOf(current),
        ).single()

        // THEN
        assertEquals(500.0, row.periodTransactionValue, 0.01)
        assertEquals(listOf(purchase), row.transactions)
        assertEquals(100.0, row.appreciationValue, 0.01)
        assertEquals(10.0, row.appreciationPercentage, 0.01)
        assertEquals("Fundo Multimercado - Multimercado Alpha", row.displayName)
        assertEquals(null, row.expirationDate)
    }

    /**
     * Investment fund with expiration date exposes it on the row.
     */
    @Test
    fun `GIVEN investment fund WHEN build THEN expirationDate is null`() {

        // GIVEN
        val period = YearMonth(2024, Month.MARCH)
        val asset = InvestmentFundAsset(
            id = 4,
            name = "Fund With Maturity",
            issuer = issuer,
            type = InvestmentFundAssetType.PENSION,
            liquidity = Liquidity.AT_MATURITY,
        )
        val holding = AssetHolding(id = 4, asset = asset, owner = owner, brokerage = brokerage)
        val current = HoldingHistoryEntry(holding = holding, referenceDate = period)

        // WHEN
        val row = HoldingHistoryRow.build(period, emptyList(), listOf(current)).single()

        // THEN
        assertNull(row.expirationDate)
        assertEquals("Previdência - Fund With Maturity", row.displayName)
    }

    /**
     * Zero current value indicates a liquidated position.
     */
    @Test
    fun `GIVEN currentValue is zero WHEN isLiquidated THEN returns true`() {

        // GIVEN
        val row = rowWithCurrentValue(currentValue = 0.0)

        // WHEN
        val liquidated = row.isLiquidated

        // THEN
        assertEquals(true, liquidated)
    }

    /**
     * Positive current value indicates an active position.
     */
    @Test
    fun `GIVEN currentValue is positive WHEN isLiquidated THEN returns false`() {

        // GIVEN
        val row = rowWithCurrentValue(currentValue = 100.0)

        // WHEN
        val liquidated = row.isLiquidated

        // THEN
        assertEquals(false, liquidated)
    }

    private fun rowWithCurrentValue(currentValue: Double): HoldingHistoryRow {
        val period = YearMonth(2024, Month.MARCH)
        val asset = FixedIncomeAsset(
            id = 1,
            issuer = issuer,
            indexer = YieldIndexer.PRE_FIXED,
            type = FixedIncomeAssetType.CDB,
            expirationDate = LocalDate(2030, Month.JUNE, 1),
            contractedYield = 10.0,
            liquidity = Liquidity.DAILY,
        )
        val holding = AssetHolding(id = 1, asset = asset, owner = owner, brokerage = brokerage)
        val quantity = 10.0
        val endOfMonthValue = currentValue / quantity
        val current = HoldingHistoryEntry(
            holding = holding,
            referenceDate = period,
            endOfMonthValue = endOfMonthValue,
            endOfMonthQuantity = quantity,
        )
        return HoldingHistoryRow.build(period, emptyList(), listOf(current)).single()
    }
}
