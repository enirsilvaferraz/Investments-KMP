package com.eferraz.usecases

import com.eferraz.entities.assets.AssetClass
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.YieldIndexer
import com.eferraz.entities.assets.InvestmentFundAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.assets.VariableIncomeAssetType
import com.eferraz.entities.holdings.HoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createAssetHolding
import com.eferraz.usecases.TestDataFactory.createFixedIncomeAsset
import com.eferraz.usecases.TestDataFactory.createHoldingHistoryEntry
import com.eferraz.usecases.TestDataFactory.createInvestmentFundAsset
import com.eferraz.usecases.TestDataFactory.createVariableIncomeAsset
import com.eferraz.usecases.entities.B3IdentifierStatus
import com.eferraz.usecases.entities.FixedIncomeHistoryTableData
import com.eferraz.usecases.entities.HoldingHistoryView
import com.eferraz.usecases.entities.InvestmentFundHistoryTableData
import com.eferraz.usecases.entities.VariableIncomeHistoryTableData
import com.eferraz.usecases.screens.GetHistoryTableDataUseCase
import com.eferraz.usecases.screens.WalletHistoryFilterCriteria
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class GetHistoryTableDataUseCaseTest {

    private val referenceEntry: HoldingHistoryEntry =
      createHoldingHistoryEntry(
          holding = createAssetHolding(asset = createFixedIncomeAsset()),
          referenceDate = YearMonth(2026, Month.MAY),
      )

    private fun fixedIncomeRow(b3Identifier: String?): FixedIncomeHistoryTableData =
        FixedIncomeHistoryTableData(
          currentEntry = referenceEntry,
          brokerageName = "Broker",
          indexer = YieldIndexer.PRE_FIXED,
          type = FixedIncomeAssetType.CDB,
          expirationDate = LocalDate(2030, Month.JUNE, 1),
          contractedYield = 10.0,
          cdiRelativeYield = null,
          b3Identifier = b3Identifier,
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
      )

  /**
   * Fixed income history row with non-blank b3Identifier maps to Informed status.
   */
  @Test
  fun `GIVEN fixed income row with b3Identifier WHEN map to HoldingHistoryView THEN status is Informed`() {

    // GIVEN
    val row = fixedIncomeRow(b3Identifier = "CDB-001")

    // WHEN
    val view = HoldingHistoryView(row)

    // THEN
    val status = assertIs<B3IdentifierStatus.Informed>(view.b3IdentifierStatus)
    assertEquals("CDB-001", status.value)
  }

  /**
   * Fixed income without identifier maps to NotInformed.
   */
  @Test
  fun `GIVEN fixed income row without b3Identifier WHEN map to HoldingHistoryView THEN status is NotInformed`() {

    // GIVEN
    val row = fixedIncomeRow(b3Identifier = null)

    // WHEN
    val view = HoldingHistoryView(row)

    // THEN
    assertEquals(B3IdentifierStatus.NotInformed, view.b3IdentifierStatus)
  }

  /**
   * Variable income history row maps ticker to Informed B3 status.
   */
  @Test
  fun `GIVEN variable income row WHEN map to HoldingHistoryView THEN status is Informed with ticker`() {

    // GIVEN
    val row =
        VariableIncomeHistoryTableData(
            currentEntry = referenceEntry.copy(holding = createAssetHolding(asset = createVariableIncomeAsset())),
            brokerageName = "Broker",
            type = VariableIncomeAssetType.NATIONAL_STOCK,
            ticker = "PETR4",
            cnpj = "",
            name = "PETR",
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
        )

    // WHEN
    val view = HoldingHistoryView(row)

    // THEN
    val status = assertIs<B3IdentifierStatus.Informed>(view.b3IdentifierStatus)
    assertEquals("PETR4", status.value)
  }

  /**
   * Investment fund history row maps to NotInformed (no B3 identifier on fund assets).
   */
  @Test
  fun `GIVEN investment fund row WHEN map to HoldingHistoryView THEN status is NotInformed`() {

    // GIVEN
    val row =
        InvestmentFundHistoryTableData(
            currentEntry = referenceEntry.copy(holding = createAssetHolding(asset = createInvestmentFundAsset())),
            brokerageName = "Broker",
            type = InvestmentFundAssetType.MULTIMARKET_FUND,
            name = "Fund",
            liquidity = Liquidity.D_PLUS_DAYS,
            liquidityDays = 30,
            expirationDate = null,
            issuerName = "Issuer",
            observations = "",
            previousValue = 0.0,
            currentValue = 100.0,
            appreciation = 0.0,
            editable = true,
            totalContributions = 0.0,
            totalWithdrawals = 0.0,
            totalBalance = 0.0,
            displayName = "Fund",
        )

    // WHEN
    val view = HoldingHistoryView(row)

    // THEN
    assertEquals(B3IdentifierStatus.NotInformed, view.b3IdentifierStatus)
  }

  /**
   * History screen passes walletFilter via Param; default excludes settled positions (contract T9).
   */
  @Test
  fun `GIVEN defaultForHistory Param WHEN walletFilter settled THEN only non-settled active`() {

    // GIVEN
    val param =
        GetHistoryTableDataUseCase.Param(
            referenceDate = YearMonth(2026, Month.JUNE),
            walletFilter = WalletHistoryFilterCriteria.defaultForHistory(),
        )

    // THEN
    assertEquals(setOf(false), param.walletFilter.settled)
  }
}
