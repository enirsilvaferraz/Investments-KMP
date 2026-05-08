package com.eferraz.filestore

import com.eferraz.entities.assets.FixedIncomeAsset
import com.eferraz.entities.assets.FixedIncomeAssetType
import com.eferraz.entities.assets.Liquidity
import com.eferraz.entities.holdings.HoldingHistoryEntry
import kotlin.math.roundToLong
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import org.koin.core.annotation.Factory

@Factory(binds = [AssetFileStore::class])
internal class AssetFileStoreImpl : AssetFileStore {

    override fun exportToCSV(data: List<HoldingHistoryEntry>) {
        val csv = buildFixedIncomeCsv(data)
        writeCsv(csv)
    }
}

private fun buildFixedIncomeCsv(entries: List<HoldingHistoryEntry>): String {

    val header = listOf(
        "Corretora",
        "Display Name",
        "Observação",
        "Vencimento",
        "Tipo de liquidez",
        "Valor atual",
    ).joinToString(separator = ",") { it.toCsvField() }

    val lines = entries
        .asSequence()
        .mapNotNull { entry -> buildLine(entry) }
        .toList()

    return buildString {
        appendLine(header)
        lines.forEach { appendLine(it) }
    }
}

private fun buildLine(entry: HoldingHistoryEntry): String? {

    val asset = entry.holding.asset as? FixedIncomeAsset ?: return null
    val currentTotal = entry.endOfMonthValue * entry.endOfMonthQuantity

    return listOf(
        entry.holding.brokerage.name,
        asset.displayNameForCsv(),
        asset.observations.orEmpty(),
        asset.expirationDate.formattedForCsv(),
        asset.liquidity.formattedForCsv(),
        currentTotal.toCsvDecimal(),
    ).joinToString(separator = ",") { value ->
        value.toCsvField()
    }
}

private fun FixedIncomeAsset.displayNameForCsv(): String =
    when (type) {
        FixedIncomeAssetType.POST_FIXED -> "${subType.name} de $contractedYield% do CDI"
        FixedIncomeAssetType.PRE_FIXED -> "${subType.name} de $contractedYield% a.a."
        FixedIncomeAssetType.INFLATION_LINKED -> "${subType.name} + $contractedYield%"
    }

private fun LocalDate.formattedForCsv(): String =
    format(
        LocalDate.Format {
            year()
            char('.')
            monthNumber()
            char('.')
            day()
        },
    )

private fun Liquidity.formattedForCsv(): String =
    when (this) {
        Liquidity.DAILY -> "Diária"
        Liquidity.AT_MATURITY -> "No vencimento"
        Liquidity.D_PLUS_DAYS -> "D+0"
    }

private fun Double.toCsvDecimal(): String {
    val scaledValue = (this * 100).roundToLong() / 100.0
    return scaledValue.toString().replace('.', ',')
}

private fun String.toCsvField(): String =
    "\"${replace("\"", "\"\"")}\""
