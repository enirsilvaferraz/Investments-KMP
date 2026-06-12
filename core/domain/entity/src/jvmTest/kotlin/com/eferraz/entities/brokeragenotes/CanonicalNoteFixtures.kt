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
        assets: List<AssetTransaction> = simplifiedDefaultAssets(),
        totalVolumeTraded: Double = assets.sumOf { it.totalValue },
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
    ): AssetTransaction = AssetTransaction(
        id = id,
        date = date,
        type = type,
        quantity = quantity,
        unitPrice = unitPrice,
    )

    private fun simplifiedDefaultAssets(): List<AssetTransaction> = listOf(
        asset(0, TransactionType.PURCHASE, 100.0, 10.00),
        asset(1, TransactionType.SALE, 10.0, 100.00),
        asset(2, TransactionType.PURCHASE, 1000.0, 1.00),
    )

    private fun fullCanonicalAssets(): List<AssetTransaction> = listOf(
        asset(0, TransactionType.PURCHASE, 53.0, 51.06, fullCanonicalDate),
        asset(1, TransactionType.PURCHASE, 42.0, 15.13, fullCanonicalDate),
        asset(2, TransactionType.PURCHASE, 1.0, 15.14, fullCanonicalDate),
        asset(3, TransactionType.PURCHASE, 3.0, 15.15, fullCanonicalDate),
        asset(4, TransactionType.PURCHASE, 50.0, 12.84, fullCanonicalDate),
        asset(5, TransactionType.PURCHASE, 56.0, 25.15, fullCanonicalDate),
        asset(6, TransactionType.PURCHASE, 97.0, 10.59, fullCanonicalDate),
        asset(7, TransactionType.PURCHASE, 1.0, 10.6, fullCanonicalDate),
        asset(8, TransactionType.PURCHASE, 1.0, 40.82, fullCanonicalDate),
        asset(9, TransactionType.PURCHASE, 50.0, 40.85, fullCanonicalDate),
        asset(10, TransactionType.PURCHASE, 40.0, 26.97, fullCanonicalDate),
        asset(11, TransactionType.PURCHASE, 21.0, 41.12, fullCanonicalDate),
        asset(12, TransactionType.PURCHASE, 8.0, 18.15, fullCanonicalDate),
        asset(13, TransactionType.PURCHASE, 18.0, 18.16, fullCanonicalDate),
        asset(14, TransactionType.SALE, 2.0, 50.97, fullCanonicalDate),
        asset(15, TransactionType.SALE, 59.0, 24.22, fullCanonicalDate),
        asset(16, TransactionType.SALE, 9.0, 38.73, fullCanonicalDate),
        asset(17, TransactionType.SALE, 1.0, 38.74, fullCanonicalDate),
        asset(18, TransactionType.SALE, 8.0, 48.48, fullCanonicalDate),
        asset(19, TransactionType.PURCHASE, 100.0, 51.04, fullCanonicalDate),
        asset(20, TransactionType.PURCHASE, 200.0, 15.12, fullCanonicalDate),
        asset(21, TransactionType.PURCHASE, 2000.0, 3.39, fullCanonicalDate),
        asset(22, TransactionType.PURCHASE, 29.0, 113.53, fullCanonicalDate),
        asset(23, TransactionType.PURCHASE, 24.0, 102.45, fullCanonicalDate),
        asset(24, TransactionType.PURCHASE, 16.0, 88.53, fullCanonicalDate),
        asset(25, TransactionType.PURCHASE, 11.0, 88.54, fullCanonicalDate),
        asset(26, TransactionType.PURCHASE, 400.0, 18.16, fullCanonicalDate),
        asset(27, TransactionType.SALE, 217.0, 8.96, fullCanonicalDate),
        asset(28, TransactionType.SALE, 75.0, 19.74, fullCanonicalDate),
        asset(29, TransactionType.SALE, 207.0, 9.08, fullCanonicalDate),
    )
}
