package com.eferraz.entities.brokeragenotes

import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.TransactionType
import kotlinx.datetime.LocalDate

internal object CanonicalNoteFixtures {

    private val simplifiedDate = LocalDate(2026, 1, 1)
    private val fullCanonicalDate = LocalDate(2026, 6, 9)

    internal fun simplifiedThreeAssetNote(
        netValue: Double = -1004.54,
        apportionableFees: Double = 4.54,
        withheldTaxes: Double = 0.0,
        assets: List<BrokerageNoteAsset> = simplifiedDefaultAssets(),
        totalVolumeTraded: Double = assets.sumOf { it.transaction.grossValue },
    ): BrokerageNote = BrokerageNote(
        totalVolumeTraded = totalVolumeTraded,
        apportionableFees = apportionableFees,
        withheldTaxes = withheldTaxes,
        netValue = netValue,
        assets = assets,
    )

    internal fun fullCanonicalNote(): BrokerageNote = BrokerageNote(
        totalVolumeTraded = 48912.22,
        apportionableFees = 14.66,
        withheldTaxes = 0.0,
        netValue = -33705.98,
        assets = fullCanonicalAssets(),
    )

    internal fun asset(
        id: Long,
        type: TransactionType,
        quantity: Double,
        unitPrice: Double,
        date: LocalDate = simplifiedDate,
        ticker: String = "TICK$id",
    ): BrokerageNoteAsset = BrokerageNoteAsset(
        ticker = ticker,
        transaction = AssetTransaction(
            id = id,
            date = date,
            type = type,
            quantity = quantity,
            unitPrice = unitPrice,
        ),
    )

    private fun simplifiedDefaultAssets(): List<BrokerageNoteAsset> = listOf(
        asset(0, TransactionType.PURCHASE, 100.0, 10.00, ticker = "AJFI11"),
        asset(1, TransactionType.SALE, 10.0, 100.00, ticker = "BRCO11"),
        asset(2, TransactionType.PURCHASE, 1000.0, 1.00, ticker = "VILG11"),
    )

    private fun fullCanonicalAssets(): List<BrokerageNoteAsset> = listOf(
        asset(0, TransactionType.PURCHASE, 53.0, 51.06, fullCanonicalDate, "TICK0"),
        asset(1, TransactionType.PURCHASE, 42.0, 15.13, fullCanonicalDate, "TICK1"),
        asset(2, TransactionType.PURCHASE, 1.0, 15.14, fullCanonicalDate, "TICK2"),
        asset(3, TransactionType.PURCHASE, 3.0, 15.15, fullCanonicalDate, "TICK3"),
        asset(4, TransactionType.PURCHASE, 50.0, 12.84, fullCanonicalDate, "TICK4"),
        asset(5, TransactionType.PURCHASE, 56.0, 25.15, fullCanonicalDate, "TICK5"),
        asset(6, TransactionType.PURCHASE, 97.0, 10.59, fullCanonicalDate, "TICK6"),
        asset(7, TransactionType.PURCHASE, 1.0, 10.6, fullCanonicalDate, "TICK7"),
        asset(8, TransactionType.PURCHASE, 1.0, 40.82, fullCanonicalDate, "TICK8"),
        asset(9, TransactionType.PURCHASE, 50.0, 40.85, fullCanonicalDate, "TICK9"),
        asset(10, TransactionType.PURCHASE, 40.0, 26.97, fullCanonicalDate, "TICK10"),
        asset(11, TransactionType.PURCHASE, 21.0, 41.12, fullCanonicalDate, "TICK11"),
        asset(12, TransactionType.PURCHASE, 8.0, 18.15, fullCanonicalDate, "TICK12"),
        asset(13, TransactionType.PURCHASE, 18.0, 18.16, fullCanonicalDate, "TICK13"),
        asset(14, TransactionType.SALE, 2.0, 50.97, fullCanonicalDate, "TICK14"),
        asset(15, TransactionType.SALE, 59.0, 24.22, fullCanonicalDate, "TICK15"),
        asset(16, TransactionType.SALE, 9.0, 38.73, fullCanonicalDate, "TICK16"),
        asset(17, TransactionType.SALE, 1.0, 38.74, fullCanonicalDate, "TICK17"),
        asset(18, TransactionType.SALE, 8.0, 48.48, fullCanonicalDate, "TICK18"),
        asset(19, TransactionType.PURCHASE, 100.0, 51.04, fullCanonicalDate, "TICK19"),
        asset(20, TransactionType.PURCHASE, 200.0, 15.12, fullCanonicalDate, "TICK20"),
        asset(21, TransactionType.PURCHASE, 2000.0, 3.39, fullCanonicalDate, "TICK21"),
        asset(22, TransactionType.PURCHASE, 29.0, 113.53, fullCanonicalDate, "TICK22"),
        asset(23, TransactionType.PURCHASE, 24.0, 102.45, fullCanonicalDate, "TICK23"),
        asset(24, TransactionType.PURCHASE, 16.0, 88.53, fullCanonicalDate, "TICK24"),
        asset(25, TransactionType.PURCHASE, 11.0, 88.54, fullCanonicalDate, "TICK25"),
        asset(26, TransactionType.PURCHASE, 400.0, 18.16, fullCanonicalDate, "TICK26"),
        asset(27, TransactionType.SALE, 217.0, 8.96, fullCanonicalDate, "TICK27"),
        asset(28, TransactionType.SALE, 75.0, 19.74, fullCanonicalDate, "TICK28"),
        asset(29, TransactionType.SALE, 207.0, 9.08, fullCanonicalDate, "TICK29"),
    )
}
