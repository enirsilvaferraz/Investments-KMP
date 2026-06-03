package com.eferraz.usecases.screens

import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.Liquidity
import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.TestDataFactory.createHoldingHistoryEntry
import com.eferraz.usecases.entities.FixedIncomeHistoryTableData
import com.eferraz.usecases.entities.VariableIncomeHistoryTableData
import com.eferraz.usecases.repositories.DateProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetHistoryMaturityMonthsUseCaseTest {

    /**
     * Distinct sorted YearMonths from fixed income rows only.
     */
    @Test
    fun `GIVEN fixed income rows with distinct expiration months WHEN maturityMonthsFromHistory THEN returns sorted distinct months`() {

        // GIVEN
        val rows =
            listOf(
                fixedIncomeRow(expirationDate = LocalDate(2026, Month.MAY, 1)),
                fixedIncomeRow(expirationDate = LocalDate(2026, Month.NOVEMBER, 1)),
                fixedIncomeRow(expirationDate = LocalDate(2026, Month.MAY, 15)),
            )

        // WHEN
        val months = rows.maturityMonthsFromHistory()

        // THEN
        assertEquals(
            listOf(YearMonth(2026, Month.MAY), YearMonth(2026, Month.NOVEMBER)),
            months,
        )
    }

    /**
     * Non fixed income rows are ignored for maturity month extraction.
     */
    @Test
    fun `GIVEN mixed history rows WHEN maturityMonthsFromHistory THEN ignores non fixed income`() {

        // GIVEN
        val entry = createHoldingHistoryEntry(holding = createAssetHolding())
        val rows =
            listOf(
                fixedIncomeRow(expirationDate = LocalDate(2027, Month.JANUARY, 1)),
                VariableIncomeHistoryTableData(
                    currentEntry = entry,
                    brokerageName = "Broker",
                    type = com.eferraz.entities.assets.VariableIncomeAssetType.NATIONAL_STOCK,
                    ticker = "PETR4",
                    cnpj = "",
                    name = "Petro",
                    issuerName = "Issuer",
                    observations = "",
                    previousValue = 0.0,
                    currentValue = 100.0,
                    appreciation = 0.0,
                    editable = false,
                    totalContributions = 0.0,
                    totalWithdrawals = 0.0,
                    totalBalance = 0.0,
                    displayName = "PETR4",
                ),
            )

        // WHEN
        val months = rows.maturityMonthsFromHistory()

        // THEN
        assertEquals(listOf(YearMonth(2027, Month.JANUARY)), months)
    }

    /**
     * Maturity filter options span from the given month through December 2030.
     */
    @Test
    fun `GIVEN start month WHEN maturityFilterMonthRange THEN returns consecutive months through December 2030`() {

        // GIVEN
        val from = YearMonth(2026, Month.JUNE)

        // WHEN
        val months = maturityFilterMonthRange(from)

        // THEN
        assertEquals(from, months.first())
        assertEquals(MaturityFilterRangeEnd, months.last())
        assertEquals(55, months.size)
        assertEquals(
            listOf(
                YearMonth(2026, Month.JUNE),
                YearMonth(2026, Month.JULY),
                YearMonth(2026, Month.AUGUST),
            ),
            months.take(3),
        )
    }

    /**
     * Start after the configured end yields no selectable months.
     */
    @Test
    fun `GIVEN start month after December 2030 WHEN maturityFilterMonthRange THEN returns empty list`() {

        // GIVEN
        val from = YearMonth(2031, Month.JANUARY)

        // WHEN
        val months = maturityFilterMonthRange(from)

        // THEN
        assertTrue(months.isEmpty())
    }

    /**
     * Use case returns the fixed month range from the current system month.
     */
    @Test
    fun `GIVEN current month from date provider WHEN execute THEN returns maturity filter month range`() = runTest {

        // GIVEN
        val currentMonth = YearMonth(2026, Month.JUNE)
        val dateProvider =
            mockk<DateProvider> {
                every { getCurrentYearMonth() } returns currentMonth
            }
        val useCase = GetHistoryMaturityMonthsUseCase(dateProvider)

        // WHEN
        val months = useCase(Unit).getOrThrow()

        // THEN
        assertEquals(maturityFilterMonthRange(currentMonth), months)
    }

    private fun fixedIncomeRow(expirationDate: LocalDate): FixedIncomeHistoryTableData {
        val entry =
            createHoldingHistoryEntry(
                holding =
                    createAssetHolding(
                        asset =
                            createFixedIncomeAsset(
                                expirationDate = expirationDate,
                            ),
                    ),
                referenceDate = YearMonth(2026, Month.JUNE),
            )
        return FixedIncomeHistoryTableData(
            currentEntry = entry,
            brokerageName = "Broker",
            subType = FixedIncomeSubType.CDB,
            type = FixedIncomeAssetType.PRE_FIXED,
            expirationDate = expirationDate,
            contractedYield = 10.0,
            cdiRelativeYield = null,
            b3Identifier = null,
            issuerName = "Issuer",
            liquidity = Liquidity.DAILY,
            observations = "",
            previousValue = 0.0,
            currentValue = 100.0,
            appreciation = 0.0,
            editable = true,
            totalContributions = 0.0,
            totalWithdrawals = 0.0,
            totalBalance = 0.0,
            displayName = "CDB",
            category = InvestmentCategory.FIXED_INCOME,
        )
    }
}
