package com.eferraz.usecases.screens

import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createBrokerage
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.TestDataFactory.createHoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createInvestmentFundAsset
import com.eferraz.usecases.TestDataFactory.createVariableIncomeAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FilterHoldingHistoryUseCaseTest {

    private val useCase = FilterHoldingHistoryUseCase(context = Dispatchers.Unconfined)

    private val referenceMonth = YearMonth(2026, Month.JUNE)

    private val rfCdbEntry = entry(
        asset = createFixedIncomeAsset(type = FixedIncomeAssetType.CDB).copy(liquidity = Liquidity.DAILY),
        endOfMonthValue = 100.0,
        endOfMonthQuantity = 1.0,
        brokerageId = 1L,
    )

    private val rfLciEntry = entry(
        asset = createFixedIncomeAsset(
            type = FixedIncomeAssetType.LCI,
            expirationDate = LocalDate(2027, Month.JANUARY, 1),
        ).copy(liquidity = Liquidity.AT_MATURITY),
        endOfMonthValue = 100.0,
        brokerageId = 1L,
    )

    private val rvEntry = entry(
        asset = createVariableIncomeAsset(),
        endOfMonthValue = 100.0,
        brokerageId = 1L,
    )

    private val fundEntry = entry(
        asset = createInvestmentFundAsset(type = InvestmentFundAssetType.STOCK_FUND),
        endOfMonthValue = 100.0,
        brokerageId = 1L,
    )

    private val liquidatedRfEntry = rfCdbEntry.copy(endOfMonthValue = 0.0, endOfMonthQuantity = 1.0)

    /**
     * Empty input list returns empty output.
     */
    @Test
    fun `GIVEN empty entries WHEN filter THEN returns empty list`() = runTest {

        // GIVEN
        val param = FilterHoldingHistoryUseCase.Param(emptyList(), WalletHistoryFilterCriteria())

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertTrue(result.isEmpty())
    }

    /**
     * All filter groups inactive → every entry passes.
     */
    @Test
    fun `GIVEN inactive criteria WHEN filter THEN all entries pass`() = runTest {

        // GIVEN
        val entries = listOf(rfCdbEntry, liquidatedRfEntry)
        val param = FilterHoldingHistoryUseCase.Param(entries, WalletHistoryFilterCriteria())

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(entries, result)
    }

    /**
     * Only non-settled selected → liquidated entries excluded (T2).
     */
    @Test
    fun `GIVEN only non-settled WHEN filter THEN excludes liquidated`() = runTest {

        // GIVEN
        val entries = listOf(rfCdbEntry, liquidatedRfEntry)
        val param =
            FilterHoldingHistoryUseCase.Param(
                entries,
                WalletHistoryFilterCriteria(settled = setOf(false)),
            )

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(listOf(rfCdbEntry), result)
    }

    /**
     * Single fixed income category → only RF entries pass (T3).
     */
    @Test
    fun `GIVEN only fixed income WHEN filter THEN only RF passes`() = runTest {

        // GIVEN
        val entries = listOf(rfCdbEntry, rvEntry)
        val param =
            FilterHoldingHistoryUseCase.Param(
                entries,
                WalletHistoryFilterCriteria(assetClasses = setOf(AssetClass.FIXED_INCOME)),
            )

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(listOf(rfCdbEntry), result)
    }

    /**
     * T1: no active groups → all pass including liquidated.
     */
    @Test
    fun `GIVEN T1 inactive criteria WHEN filter THEN all including liquidated pass`() = runTest {

        // GIVEN
        val entries = listOf(rfCdbEntry, liquidatedRfEntry)
        val param = FilterHoldingHistoryUseCase.Param(entries, WalletHistoryFilterCriteria())

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(2, result.size)
    }

    /**
     * T4: OR fixed income and variable income categories.
     */
    @Test
    fun `GIVEN T4 RF and RV categories WHEN filter THEN RF and RV pass fund excluded`() = runTest {

        // GIVEN
        val entries = listOf(rfCdbEntry, rvEntry, fundEntry)
        val param =
            FilterHoldingHistoryUseCase.Param(
                entries,
                WalletHistoryFilterCriteria(
                    assetClasses = setOf(AssetClass.FIXED_INCOME, AssetClass.VARIABLE_INCOME),
                ),
            )

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(listOf(rfCdbEntry, rvEntry), result)
    }

    /**
     * T5: AND fixed income with B3 informed.
     */
    @Test
    fun `GIVEN T5 RF and B3 informed WHEN filter THEN only informed RF passes`() = runTest {

        // GIVEN
        val informedRf = rfCdbEntry.copy(
            holding = createAssetHolding(
                asset = createFixedIncomeAsset().copy(b3Identifier = "CDB-1"),
            ),
        )
        val entries = listOf(informedRf, rfLciEntry)
        val param =
            FilterHoldingHistoryUseCase.Param(
                entries,
                WalletHistoryFilterCriteria(
                    assetClasses = setOf(AssetClass.FIXED_INCOME),
                    b3Informed = setOf(true),
                ),
            )

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(listOf(informedRf), result)
    }

    /**
     * T6: daily liquidity restricts fixed income only.
     */
    @Test
    fun `GIVEN T6 daily liquidity WHEN filter THEN RF daily passes at-maturity RF fails RV passes`() =
        runTest {

            // GIVEN
            val entries = listOf(rfCdbEntry, rfLciEntry, rvEntry)
            val param =
                FilterHoldingHistoryUseCase.Param(
                    entries,
                    WalletHistoryFilterCriteria(liquidities = setOf(Liquidity.DAILY)),
                )

            // WHEN
            val result = useCase(param).getOrThrow()

            // THEN
            assertEquals(listOf(rfCdbEntry, rvEntry), result)
        }

    /**
     * T7: maturity up to June 2026.
     */
    @Test
    fun `GIVEN T7 maturity up to June 2026 WHEN filter THEN RF in range passes`() = runTest {

        // GIVEN
        val rfInRange = rfCdbEntry.copy(
            holding = createAssetHolding(
                asset = createFixedIncomeAsset(expirationDate = LocalDate(2026, Month.JUNE, 1)),
            ),
        )
        val entries = listOf(rfInRange, rfLciEntry, rvEntry)
        val param =
            FilterHoldingHistoryUseCase.Param(
                entries,
                WalletHistoryFilterCriteria(maturityUpTo = YearMonth(2026, Month.JUNE)),
            )

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(listOf(rfInRange, rvEntry), result)
    }

    /**
     * T8: B3 both yes and no → group inactive.
     */
    @Test
    fun `GIVEN T8 B3 saturated WHEN filter THEN all RF pass`() = runTest {

        // GIVEN
        val entries = listOf(rfCdbEntry, rfLciEntry)
        val param =
            FilterHoldingHistoryUseCase.Param(
                entries,
                WalletHistoryFilterCriteria(b3Informed = setOf(true, false)),
            )

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(entries, result)
    }

    /**
     * T9: defaultForHistory excludes liquidated (T2).
     */
    @Test
    fun `GIVEN T9 defaultForHistory WHEN filter THEN excludes liquidated`() = runTest {

        // GIVEN
        val entries = listOf(rfCdbEntry, liquidatedRfEntry)
        val param =
            FilterHoldingHistoryUseCase.Param(
                entries,
                WalletHistoryFilterCriteria.defaultForHistory(),
            )

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(listOf(rfCdbEntry), result)
    }

    /**
     * Brokerage filter: single id.
     */
    @Test
    fun `GIVEN single brokerage id WHEN filter THEN only matching entries`() = runTest {

        // GIVEN
        val broker1 = entry(brokerageId = 1L)
        val broker2 = entry(brokerageId = 2L)
        val entries = listOf(broker1, broker2)
        val param =
            FilterHoldingHistoryUseCase.Param(
                entries,
                WalletHistoryFilterCriteria(brokerageIds = setOf(1L)),
            )

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(listOf(broker1), result)
    }

    /**
     * Brokerage OR two ids.
     */
    @Test
    fun `GIVEN two brokerage ids WHEN filter THEN OR passes`() = runTest {

        // GIVEN
        val broker1 = entry(brokerageId = 1L)
        val broker2 = entry(brokerageId = 2L)
        val broker3 = entry(brokerageId = 3L)
        val entries = listOf(broker1, broker2, broker3)
        val param =
            FilterHoldingHistoryUseCase.Param(
                entries,
                WalletHistoryFilterCriteria(brokerageIds = setOf(1L, 2L)),
            )

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(listOf(broker1, broker2), result)
    }

    /**
     * Negative value and quantity with zero product → settled and excluded by defaultForHistory.
     */
    @Test
    fun `GIVEN negative value and zero quantity WHEN filter THEN settled true excluded`() = runTest {

        // GIVEN
        val settledEntry = rfCdbEntry.copy(endOfMonthValue = -100.0, endOfMonthQuantity = 0.0)
        val entries = listOf(rfCdbEntry, settledEntry)
        val param =
            FilterHoldingHistoryUseCase.Param(
                entries,
                WalletHistoryFilterCriteria.defaultForHistory(),
            )

        // WHEN
        val result = useCase(param).getOrThrow()

        // THEN
        assertEquals(listOf(rfCdbEntry), result)
        assertTrue(settledEntry.toWalletHistoryFilterCandidate().settled)
    }

    private fun entry(
        asset: com.eferraz.entities.assets.Asset = createFixedIncomeAsset(),
        endOfMonthValue: Double = 100.0,
        endOfMonthQuantity: Double = 1.0,
        brokerageId: Long = 1L,
    ): HoldingHistoryEntry =
        createHoldingHistoryEntry(
            holding = createAssetHolding(asset = asset, brokerage = createBrokerage(id = brokerageId)),
            referenceDate = referenceMonth,
            endOfMonthValue = endOfMonthValue,
            endOfMonthQuantity = endOfMonthQuantity,
        )
}
