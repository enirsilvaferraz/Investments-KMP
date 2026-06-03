package com.eferraz.usecases.screens

import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createBrokerage
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.TestDataFactory.createHoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createInvestmentFundAsset
import com.eferraz.usecases.TestDataFactory.createVariableIncomeAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WalletHistoryFilterTest {

    private val filter = FilterHoldingHistoryUseCase(context = Dispatchers.Unconfined)

    private fun matches(
        candidate: WalletHistoryFilterCandidate,
        criteria: WalletHistoryFilterCriteria,
    ): Boolean = runBlocking {
        val entry = candidate.toTestEntry()
        filter(FilterHoldingHistoryUseCase.Param(listOf(entry), criteria))
            .getOrThrow()
            .contains(entry)
    }

    private val rfCdb = candidate(
        assetClass = AssetClass.FIXED_INCOME,
        subtype = WalletHistorySubtype.FixedIncome(FixedIncomeAssetType.CDB),
        liquidity = Liquidity.DAILY,
        b3Informed = true,
        settled = false,
        expirationDate = LocalDate(2026, Month.JUNE, 1),
    )

    private val rfLci = candidate(
        assetClass = AssetClass.FIXED_INCOME,
        subtype = WalletHistorySubtype.FixedIncome(FixedIncomeAssetType.LCI),
        liquidity = Liquidity.AT_MATURITY,
        b3Informed = false,
        settled = false,
        expirationDate = LocalDate(2027, Month.JANUARY, 1),
    )

    private val rvStock = candidate(
        assetClass = AssetClass.VARIABLE_INCOME,
        subtype = WalletHistorySubtype.VariableIncome(VariableIncomeAssetType.NATIONAL_STOCK),
        liquidity = Liquidity.DAILY,
        b3Informed = true,
        settled = false,
        expirationDate = null,
    )

    private val fund = candidate(
        assetClass = AssetClass.INVESTMENT_FUND,
        subtype = WalletHistorySubtype.InvestmentFund(InvestmentFundAssetType.STOCK_FUND),
        liquidity = Liquidity.DAILY,
        b3Informed = false,
        settled = false,
        expirationDate = null,
    )

    private val liquidatedRf = rfCdb.copy(settled = true)

    /**
     * T1: no active filter groups → all candidates pass, including liquidated.
     */
    @Test
    fun `GIVEN no active filter groups WHEN matchesWalletHistoryFilter THEN all candidates pass`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria()

        // WHEN
        val active = matches(rfCdb, criteria)
        val liquidated = matches(liquidatedRf, criteria)

        // THEN
        assertTrue(active)
        assertTrue(liquidated)
    }

    /**
     * T2: only non-settled selected → liquidated positions excluded.
     */
    @Test
    fun `GIVEN only non-settled selected WHEN matchesWalletHistoryFilter THEN excludes liquidated`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(settled = setOf(false))

        // WHEN
        val active = matches(rfCdb, criteria)
        val liquidated = matches(liquidatedRf, criteria)

        // THEN
        assertTrue(active)
        assertFalse(liquidated)
    }

    /**
     * T3: single category fixed income → only RF passes.
     */
    @Test
    fun `GIVEN only fixed income category WHEN matchesWalletHistoryFilter THEN only RF passes`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(assetClasses = setOf(AssetClass.FIXED_INCOME))

        // WHEN
        val rf = matches(rfCdb, criteria)
        val rv = matches(rvStock, criteria)

        // THEN
        assertTrue(rf)
        assertFalse(rv)
    }

    /**
     * T4: OR two categories (RF ∪ RV).
     */
    @Test
    fun `GIVEN fixed income and variable income categories WHEN matchesWalletHistoryFilter THEN RF or RV passes`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(
            assetClasses = setOf(
                AssetClass.FIXED_INCOME,
                AssetClass.VARIABLE_INCOME,
            ),
        )

        // WHEN
        val rf = matches(rfCdb, criteria)
        val rv = matches(rvStock, criteria)
        val fundMatch = matches(fund, criteria)

        // THEN
        assertTrue(rf)
        assertTrue(rv)
        assertFalse(fundMatch)
    }

    /**
     * T5: AND fixed income category with B3 informed.
     */
    @Test
    fun `GIVEN fixed income and B3 informed WHEN matchesWalletHistoryFilter THEN only matching RF passes`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(
            assetClasses = setOf(AssetClass.FIXED_INCOME),
            b3Informed = setOf(true),
        )

        // WHEN
        val informed = matches(rfCdb, criteria)
        val notInformed = matches(rfLci, criteria)

        // THEN
        assertTrue(informed)
        assertFalse(notInformed)
    }

    /**
     * T6: liquidity filter only restricts fixed income.
     */
    @Test
    fun `GIVEN daily liquidity filter WHEN matchesWalletHistoryFilter THEN only RF with daily liquidity restricted`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(liquidities = setOf(Liquidity.DAILY))

        // WHEN
        val rfDaily = matches(rfCdb, criteria)
        val rfAtMaturity = matches(rfLci, criteria)
        val rvMatch = matches(rvStock, criteria)

        // THEN
        assertTrue(rfDaily)
        assertFalse(rfAtMaturity)
        assertTrue(rvMatch)
    }

    /**
     * T7: maturity up to only restricts fixed income by expiration date (inclusive through end of month).
     */
    @Test
    fun `GIVEN maturity up to June 2026 WHEN matchesWalletHistoryFilter THEN RF after month fails RV passes`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(maturityUpTo = YearMonth(2026, Month.JUNE))

        // WHEN
        val rfInRange = matches(rfCdb, criteria)
        val rfAfter = matches(rfLci, criteria)
        val rvMatch = matches(rvStock, criteria)

        // THEN
        assertTrue(rfInRange)
        assertFalse(rfAfter)
        assertTrue(rvMatch)
    }

    /**
     * Vence até: all RF assets expiring on or before the selected month (inclusive).
     */
    @Test
    fun `GIVEN maturity up to November 2026 WHEN matchesWalletHistoryFilter THEN May and November RF pass December fails`() {

        // GIVEN
        val assetMay = candidate(
            assetClass = AssetClass.FIXED_INCOME,
            subtype = WalletHistorySubtype.FixedIncome(FixedIncomeAssetType.CDB),
            liquidity = Liquidity.DAILY,
            b3Informed = true,
            settled = false,
            expirationDate = LocalDate(2026, Month.MAY, 1),
        )
        val assetNovember = candidate(
            assetClass = AssetClass.FIXED_INCOME,
            subtype = WalletHistorySubtype.FixedIncome(FixedIncomeAssetType.LCI),
            liquidity = Liquidity.DAILY,
            b3Informed = true,
            settled = false,
            expirationDate = LocalDate(2026, Month.NOVEMBER, 1),
        )
        val assetDecember = candidate(
            assetClass = AssetClass.FIXED_INCOME,
            subtype = WalletHistorySubtype.FixedIncome(FixedIncomeAssetType.CDB),
            liquidity = Liquidity.AT_MATURITY,
            b3Informed = true,
            settled = false,
            expirationDate = LocalDate(2026, Month.DECEMBER, 1),
        )
        val criteria = WalletHistoryFilterCriteria(maturityUpTo = YearMonth(2026, Month.NOVEMBER))

        // WHEN
        val mayMatch = matches(assetMay, criteria)
        val novemberMatch = matches(assetNovember, criteria)
        val decemberMatch = matches(assetDecember, criteria)

        // THEN
        assertTrue(mayMatch)
        assertTrue(novemberMatch)
        assertFalse(decemberMatch)
    }

    /**
     * Vence até: daily liquidity fixed income always passes, even without expiration or after the selected month.
     */
    @Test
    fun `GIVEN maturity up to June 2026 WHEN matchesWalletHistoryFilter THEN daily liquidity RF always passes`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(maturityUpTo = YearMonth(2026, Month.JUNE))
        val dailyWithoutExpiration = rfCdb.copy(expirationDate = null)
        val dailyAfterMonth = rfCdb.copy(expirationDate = LocalDate(2027, Month.JANUARY, 1))

        // WHEN
        val withoutExpirationMatch = matches(dailyWithoutExpiration, criteria)
        val afterMonthMatch = matches(dailyAfterMonth, criteria)
        val atMaturityAfterMonth = matches(rfLci, criteria)

        // THEN
        assertTrue(withoutExpirationMatch)
        assertTrue(afterMonthMatch)
        assertFalse(atMaturityAfterMonth)
    }

    /**
     * T8: B3 both yes and no selected → group inactive, all pass.
     */
    @Test
    fun `GIVEN B3 both yes and no selected WHEN matchesWalletHistoryFilter THEN group is inactive`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(b3Informed = setOf(true, false))

        // WHEN
        val informed = matches(rfCdb, criteria)
        val notInformed = matches(rfLci, criteria)

        // THEN
        assertTrue(informed)
        assertTrue(notInformed)
    }

    /**
     * T9: defaultForHistory criteria → same as non-settled only (T2).
     */
    @Test
    fun `GIVEN defaultForHistory criteria WHEN matchesWalletHistoryFilter THEN excludes liquidated like T2`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria.defaultForHistory()

        // WHEN
        val active = matches(rfCdb, criteria)
        val liquidated = matches(liquidatedRf, criteria)
        val emptyCriteria = WalletHistoryFilterCriteria()

        // THEN
        assertTrue(active)
        assertFalse(liquidated)
        assertTrue(matches(liquidatedRf, emptyCriteria))
    }

    /**
     * OR subtype: CDB ∪ LCI within fixed income; RV is not restricted by RF-only subtypes.
     */
    @Test
    fun `GIVEN CDB and LCI subtypes WHEN matchesWalletHistoryFilter THEN CDB or LCI RF passes RV unaffected`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(
            subtypes = setOf(
                WalletHistorySubtype.FixedIncome(FixedIncomeAssetType.CDB),
                WalletHistorySubtype.FixedIncome(FixedIncomeAssetType.LCI),
            ),
        )

        // WHEN
        val cdb = matches(rfCdb, criteria)
        val lci = matches(rfLci, criteria)
        val rvMatch = matches(rvStock, criteria)

        // THEN
        assertTrue(cdb)
        assertTrue(lci)
        assertTrue(rvMatch)
    }

    /**
     * Brokerage: single id → only matching candidate passes.
     */
    @Test
    fun `GIVEN single brokerage id WHEN matchesWalletHistoryFilter THEN only that brokerage passes`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(brokerageIds = setOf(1L))
        val atBroker1 = rfCdb.copy(brokerageId = 1L)
        val atBroker2 = rfCdb.copy(brokerageId = 2L)

        // WHEN
        val match1 = matches(atBroker1, criteria)
        val match2 = matches(atBroker2, criteria)

        // THEN
        assertTrue(match1)
        assertFalse(match2)
    }

    /**
     * Brokerage: OR two ids → either brokerage passes.
     */
    @Test
    fun `GIVEN two brokerage ids WHEN matchesWalletHistoryFilter THEN OR passes`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(brokerageIds = setOf(1L, 2L))

        // WHEN
        val match1 = matches(rfCdb.copy(brokerageId = 1L), criteria)
        val match2 = matches(rfCdb.copy(brokerageId = 2L), criteria)
        val match3 = matches(rfCdb.copy(brokerageId = 3L), criteria)

        // THEN
        assertTrue(match1)
        assertTrue(match2)
        assertFalse(match3)
    }

    /**
     * Brokerage AND asset class: fixed income at broker 1 only.
     */
    @Test
    fun `GIVEN brokerage and fixed income WHEN matchesWalletHistoryFilter THEN AND applies`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(
            brokerageIds = setOf(1L),
            assetClasses = setOf(AssetClass.FIXED_INCOME),
        )

        // WHEN
        val rfBroker1 = matches(rfCdb.copy(brokerageId = 1L), criteria)
        val rvBroker1 = matches(rvStock.copy(brokerageId = 1L), criteria)
        val rfBroker2 = matches(rfCdb.copy(brokerageId = 2L), criteria)

        // THEN
        assertTrue(rfBroker1)
        assertFalse(rvBroker1)
        assertFalse(rfBroker2)
    }

    /**
     * Empty brokerageIds → group inactive, all pass.
     */
    @Test
    fun `GIVEN empty brokerage ids WHEN matchesWalletHistoryFilter THEN group inactive`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria()

        // WHEN
        val match = matches(rfCdb.copy(brokerageId = 99L), criteria)

        // THEN
        assertTrue(match)
    }

    /**
     * Nonexistent brokerage id in criteria → no candidate passes.
     */
    @Test
    fun `GIVEN nonexistent brokerage id WHEN matchesWalletHistoryFilter THEN no match`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(brokerageIds = setOf(99L))

        // WHEN
        val match = matches(rfCdb.copy(brokerageId = 1L), criteria)

        // THEN
        assertFalse(match)
    }

    private fun candidate(
        assetClass: AssetClass,
        subtype: WalletHistorySubtype,
        liquidity: Liquidity?,
        b3Informed: Boolean,
        settled: Boolean,
        expirationDate: LocalDate?,
        brokerageId: Long = 1L,
    ): WalletHistoryFilterCandidate =
        WalletHistoryFilterCandidate(
            assetClass = assetClass,
            subtype = subtype,
            liquidity = liquidity,
            b3Informed = b3Informed,
            settled = settled,
            expirationDate = expirationDate,
            brokerageId = brokerageId,
        )

    private fun WalletHistoryFilterCandidate.toTestEntry(): HoldingHistoryEntry {
        val endOfMonthValue = if (settled) 0.0 else 100.0
        val asset =
            when (val subtype = subtype) {
                is WalletHistorySubtype.FixedIncome ->
                    createFixedIncomeAsset(
                        type = subtype.value,
                        expirationDate = expirationDate ?: LocalDate(2026, Month.JUNE, 1),
                    ).copy(
                        liquidity = liquidity ?: Liquidity.DAILY,
                        b3Identifier = if (b3Informed) "b3" else null,
                    )

                is WalletHistorySubtype.VariableIncome ->
                    createVariableIncomeAsset(type = subtype.value)

                is WalletHistorySubtype.InvestmentFund ->
                    createInvestmentFundAsset(type = subtype.value)
                        .copy(
                            liquidity = liquidity ?: Liquidity.DAILY,
                            expirationDate = expirationDate,
                        )
            }
        return createHoldingHistoryEntry(
            holding = createAssetHolding(
                asset = asset,
                brokerage = createBrokerage(id = brokerageId),
            ),
            endOfMonthValue = endOfMonthValue,
            endOfMonthQuantity = 1.0,
        )
    }
}
