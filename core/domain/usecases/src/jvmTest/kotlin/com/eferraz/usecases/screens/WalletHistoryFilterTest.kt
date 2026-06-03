package com.eferraz.usecases.screens

import com.eferraz.entities.assets.FixedIncomeSubType
import com.eferraz.entities.assets.InvestmentCategory
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WalletHistoryFilterTest {

    private val rfCdb = candidate(
        category = InvestmentCategory.FIXED_INCOME,
        subtype = WalletHistorySubtype.FixedIncome(FixedIncomeSubType.CDB),
        liquidity = Liquidity.DAILY,
        b3Informed = true,
        settled = false,
        expirationDate = LocalDate(2026, Month.JUNE, 1),
    )

    private val rfLci = candidate(
        category = InvestmentCategory.FIXED_INCOME,
        subtype = WalletHistorySubtype.FixedIncome(FixedIncomeSubType.LCI),
        liquidity = Liquidity.AT_MATURITY,
        b3Informed = false,
        settled = false,
        expirationDate = LocalDate(2027, Month.JANUARY, 1),
    )

    private val rvStock = candidate(
        category = InvestmentCategory.VARIABLE_INCOME,
        subtype = WalletHistorySubtype.VariableIncome(VariableIncomeAssetType.NATIONAL_STOCK),
        liquidity = Liquidity.DAILY,
        b3Informed = true,
        settled = false,
        expirationDate = null,
    )

    private val fund = candidate(
        category = InvestmentCategory.INVESTMENT_FUND,
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
        val active = matchesWalletHistoryFilter(rfCdb, criteria)
        val liquidated = matchesWalletHistoryFilter(liquidatedRf, criteria)

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
        val active = matchesWalletHistoryFilter(rfCdb, criteria)
        val liquidated = matchesWalletHistoryFilter(liquidatedRf, criteria)

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
        val criteria = WalletHistoryFilterCriteria(categories = setOf(InvestmentCategory.FIXED_INCOME))

        // WHEN
        val rf = matchesWalletHistoryFilter(rfCdb, criteria)
        val rv = matchesWalletHistoryFilter(rvStock, criteria)

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
            categories = setOf(
                InvestmentCategory.FIXED_INCOME,
                InvestmentCategory.VARIABLE_INCOME,
            ),
        )

        // WHEN
        val rf = matchesWalletHistoryFilter(rfCdb, criteria)
        val rv = matchesWalletHistoryFilter(rvStock, criteria)
        val fundMatch = matchesWalletHistoryFilter(fund, criteria)

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
            categories = setOf(InvestmentCategory.FIXED_INCOME),
            b3Informed = setOf(true),
        )

        // WHEN
        val informed = matchesWalletHistoryFilter(rfCdb, criteria)
        val notInformed = matchesWalletHistoryFilter(rfLci, criteria)

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
        val rfDaily = matchesWalletHistoryFilter(rfCdb, criteria)
        val rfAtMaturity = matchesWalletHistoryFilter(rfLci, criteria)
        val rvMatch = matchesWalletHistoryFilter(rvStock, criteria)

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
        val rfInRange = matchesWalletHistoryFilter(rfCdb, criteria)
        val rfAfter = matchesWalletHistoryFilter(rfLci, criteria)
        val rvMatch = matchesWalletHistoryFilter(rvStock, criteria)

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
            category = InvestmentCategory.FIXED_INCOME,
            subtype = WalletHistorySubtype.FixedIncome(FixedIncomeSubType.CDB),
            liquidity = Liquidity.DAILY,
            b3Informed = true,
            settled = false,
            expirationDate = LocalDate(2026, Month.MAY, 1),
        )
        val assetNovember = candidate(
            category = InvestmentCategory.FIXED_INCOME,
            subtype = WalletHistorySubtype.FixedIncome(FixedIncomeSubType.LCI),
            liquidity = Liquidity.DAILY,
            b3Informed = true,
            settled = false,
            expirationDate = LocalDate(2026, Month.NOVEMBER, 1),
        )
        val assetDecember = candidate(
            category = InvestmentCategory.FIXED_INCOME,
            subtype = WalletHistorySubtype.FixedIncome(FixedIncomeSubType.CDB),
            liquidity = Liquidity.AT_MATURITY,
            b3Informed = true,
            settled = false,
            expirationDate = LocalDate(2026, Month.DECEMBER, 1),
        )
        val criteria = WalletHistoryFilterCriteria(maturityUpTo = YearMonth(2026, Month.NOVEMBER))

        // WHEN
        val mayMatch = matchesWalletHistoryFilter(assetMay, criteria)
        val novemberMatch = matchesWalletHistoryFilter(assetNovember, criteria)
        val decemberMatch = matchesWalletHistoryFilter(assetDecember, criteria)

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
        val withoutExpirationMatch = matchesWalletHistoryFilter(dailyWithoutExpiration, criteria)
        val afterMonthMatch = matchesWalletHistoryFilter(dailyAfterMonth, criteria)
        val atMaturityAfterMonth = matchesWalletHistoryFilter(rfLci, criteria)

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
        val informed = matchesWalletHistoryFilter(rfCdb, criteria)
        val notInformed = matchesWalletHistoryFilter(rfLci, criteria)

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
        val active = matchesWalletHistoryFilter(rfCdb, criteria)
        val liquidated = matchesWalletHistoryFilter(liquidatedRf, criteria)
        val emptyCriteria = WalletHistoryFilterCriteria()

        // THEN
        assertTrue(active)
        assertFalse(liquidated)
        assertTrue(matchesWalletHistoryFilter(liquidatedRf, emptyCriteria))
    }

    /**
     * OR subtype: CDB ∪ LCI within fixed income; RV is not restricted by RF-only subtypes.
     */
    @Test
    fun `GIVEN CDB and LCI subtypes WHEN matchesWalletHistoryFilter THEN CDB or LCI RF passes RV unaffected`() {

        // GIVEN
        val criteria = WalletHistoryFilterCriteria(
            subtypes = setOf(
                WalletHistorySubtype.FixedIncome(FixedIncomeSubType.CDB),
                WalletHistorySubtype.FixedIncome(FixedIncomeSubType.LCI),
            ),
        )

        // WHEN
        val cdb = matchesWalletHistoryFilter(rfCdb, criteria)
        val lci = matchesWalletHistoryFilter(rfLci, criteria)
        val rvMatch = matchesWalletHistoryFilter(rvStock, criteria)

        // THEN
        assertTrue(cdb)
        assertTrue(lci)
        assertTrue(rvMatch)
    }

    private fun candidate(
        category: InvestmentCategory,
        subtype: WalletHistorySubtype,
        liquidity: Liquidity?,
        b3Informed: Boolean,
        settled: Boolean,
        expirationDate: LocalDate?,
    ): WalletHistoryFilterCandidate =
        WalletHistoryFilterCandidate(
            category = category,
            subtype = subtype,
            liquidity = liquidity,
            b3Informed = b3Informed,
            settled = settled,
            expirationDate = expirationDate,
        )
}
