package com.eferraz.usecases.entities

import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.TestDataFactory.createHoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createInvestmentFundAsset
import com.eferraz.usecases.TestDataFactory.createVariableIncomeAsset
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HoldingHistoryViewTest {

    private val referenceEntry =
        createHoldingHistoryEntry(
            holding = createAssetHolding(asset = createFixedIncomeAsset()),
            referenceDate = YearMonth(2026, Month.MAY),
        )

    private fun viewWithCurrentValue(currentValue: Double): HoldingHistoryView =
        HoldingHistoryView(
            FixedIncomeHistoryTableData(
                currentEntry = referenceEntry,
                brokerageName = "Broker",
                indexer = YieldIndexer.PRE_FIXED,
                type = FixedIncomeAssetType.CDB,
                expirationDate = LocalDate(2030, Month.JUNE, 1),
                contractedYield = 10.0,
                cdiRelativeYield = null,
                b3Identifier = null,
                issuerName = "Issuer",
                liquidity = Liquidity.DAILY,
                observations = "",
                previousValue = 0.0,
                currentValue = currentValue,
                appreciation = 0.0,
                editable = true,
                totalContributions = 0.0,
                totalWithdrawals = 0.0,
                totalBalance = 0.0,
                displayName = "CDB",
            ),
        )

    /**
     * Zero current value indicates a liquidated position.
     */
    @Test
    fun `GIVEN currentValue is zero WHEN isLiquidated THEN returns true`() {

        // GIVEN
        val view = viewWithCurrentValue(currentValue = 0.0)

        // WHEN
        val liquidated = view.isLiquidated

        // THEN
        assertEquals(true, liquidated)
    }

    /**
     * Positive current value indicates an active position.
     */
    @Test
    fun `GIVEN currentValue is positive WHEN isLiquidated THEN returns false`() {

        // GIVEN
        val view = viewWithCurrentValue(currentValue = 100.0)

        // WHEN
        val liquidated = view.isLiquidated

        // THEN
        assertEquals(false, liquidated)
    }

    /**
     * Negative current value is not treated as liquidated.
     */
    @Test
    fun `GIVEN currentValue is negative WHEN isLiquidated THEN returns false`() {

        // GIVEN
        val view = viewWithCurrentValue(currentValue = -1.0)

        // WHEN
        val liquidated = view.isLiquidated

        // THEN
        assertEquals(false, liquidated)
    }

    /**
     * Negative zero compares equal to zero and is liquidated.
     */
    @Test
    fun `GIVEN currentValue is negative zero WHEN isLiquidated THEN returns true`() {

        // GIVEN
        val view = viewWithCurrentValue(currentValue = -0.0)

        // WHEN
        val liquidated = view.isLiquidated

        // THEN
        assertEquals(true, liquidated)
    }

    /**
     * Fixed income history row with zero current value is liquidated (US2).
     */
    @Test
    fun `GIVEN fixed income row with zero currentValue WHEN isLiquidated THEN returns true`() {

        // GIVEN
        val view =
            HoldingHistoryView(
                FixedIncomeHistoryTableData(
                    currentEntry = referenceEntry,
                    brokerageName = "Broker",
                    indexer = YieldIndexer.PRE_FIXED,
                    type = FixedIncomeAssetType.CDB,
                    expirationDate = LocalDate(2030, Month.JUNE, 1),
                    contractedYield = 10.0,
                    cdiRelativeYield = null,
                    b3Identifier = null,
                    issuerName = "Issuer",
                    liquidity = Liquidity.DAILY,
                    observations = "",
                    previousValue = 0.0,
                    currentValue = 0.0,
                    appreciation = 0.0,
                    editable = true,
                    totalContributions = 0.0,
                    totalWithdrawals = 0.0,
                    totalBalance = 0.0,
                    displayName = "CDB",
                ),
            )

        // WHEN
        val liquidated = view.isLiquidated

        // THEN
        assertEquals(true, liquidated)
    }

    /**
     * Variable income history row with zero current value is liquidated (US2).
     */
    @Test
    fun `GIVEN variable income row with zero currentValue WHEN isLiquidated THEN returns true`() {

        // GIVEN
        val view =
            HoldingHistoryView(
                VariableIncomeHistoryTableData(
                    currentEntry =
                        referenceEntry.copy(
                            holding = createAssetHolding(asset = createVariableIncomeAsset()),
                        ),
                    brokerageName = "Broker",
                    type = VariableIncomeAssetType.NATIONAL_STOCK,
                    ticker = "PETR4",
                    cnpj = "",
                    name = "PETR",
                    issuerName = "Issuer",
                    observations = "",
                    previousValue = 0.0,
                    currentValue = 0.0,
                    appreciation = 0.0,
                    editable = false,
                    totalContributions = 0.0,
                    totalWithdrawals = 0.0,
                    totalBalance = 0.0,
                    displayName = "PETR4",
                ),
            )

        // WHEN
        val liquidated = view.isLiquidated

        // THEN
        assertEquals(true, liquidated)
    }

    /**
     * Investment fund history row with zero current value is liquidated (US2).
     */
    @Test
    fun `GIVEN investment fund row with zero currentValue WHEN isLiquidated THEN returns true`() {

        // GIVEN
        val view =
            HoldingHistoryView(
                InvestmentFundHistoryTableData(
                    currentEntry =
                        referenceEntry.copy(
                            holding = createAssetHolding(asset = createInvestmentFundAsset()),
                        ),
                    brokerageName = "Broker",
                    type = InvestmentFundAssetType.MULTIMARKET_FUND,
                    name = "Fund",
                    liquidity = Liquidity.D_PLUS_DAYS,
                    liquidityDays = 30,
                    expirationDate = null,
                    issuerName = "Issuer",
                    observations = "",
                    previousValue = 0.0,
                    currentValue = 0.0,
                    appreciation = 0.0,
                    editable = true,
                    totalContributions = 0.0,
                    totalWithdrawals = 0.0,
                    totalBalance = 0.0,
                    displayName = "Fund",
                ),
            )

        // WHEN
        val liquidated = view.isLiquidated

        // THEN
        assertEquals(true, liquidated)
    }
}
