package com.eferraz.entities.brokeragenotes

import kotlin.math.round

internal object BrokerageNoteValidator {

    internal fun validate(note: BrokerageNote) {
        val summary = note.financialSummary
        val fees = summary.apportionableFees

        if (note.assets.isEmpty()) {
            throw IllegalArgumentException("assets must not be empty")
        }

        fees.settlement.takeIf { it < 0 }?.let { throwNegative("settlement") }
        fees.emoluments.takeIf { it < 0 }?.let { throwNegative("emoluments") }
        fees.transfer.takeIf { it < 0 }?.let { throwNegative("transfer") }
        fees.brokerage.takeIf { it < 0 }?.let { throwNegative("brokerage") }
        fees.iss.takeIf { it < 0 }?.let { throwNegative("iss") }
        fees.others.takeIf { it < 0 }?.let { throwNegative("others") }
        if (summary.totalVolumeTraded < 0) throwNegative("totalVolumeTraded")
        if (summary.totalBuys < 0) throwNegative("totalBuys")
        if (summary.totalSells < 0) throwNegative("totalSells")

        note.assets.forEach { asset ->
            if (asset.quantity <= 0 || asset.unitPrice <= 0) {
                throw IllegalArgumentException(
                    "asset ${asset.ticker}: quantity and unitPrice must be > 0",
                )
            }
            if (asset.grossValue < 0) throwNegative("grossValue")
        }

        if (summary.totalVolumeTraded <= 0) {
            throw IllegalArgumentException("total volume must be > 0")
        }

        val assetsSumCents = note.assets.sumOf { it.grossValue.toCents() }
        val totalVolumeCents = summary.totalVolumeTraded.toCents()
        if (assetsSumCents != totalVolumeCents) {
            throw IllegalArgumentException(
                "volume mismatch: assets sum ${assetsSumCents / 100.0} ≠ declared ${summary.totalVolumeTraded}",
            )
        }

        note.assets.forEach { asset ->
            val computedCents = (asset.quantity * asset.unitPrice).toCents()
            val declaredCents = asset.grossValue.toCents()
            if (computedCents != declaredCents) {
                throw IllegalArgumentException(
                    "asset ${asset.ticker}: quantity×unitPrice ${computedCents / 100.0} ≠ grossValue ${asset.grossValue}",
                )
            }
        }

        val buysCents = note.assets
            .filter { it.tradeType == TradeType.BUY }
            .sumOf { it.grossValue.toCents() }
        val sellsCents = note.assets
            .filter { it.tradeType == TradeType.SELL }
            .sumOf { it.grossValue.toCents() }
        val expectedBuysCents = summary.totalBuys.toCents()
        val expectedSellsCents = summary.totalSells.toCents()
        if (buysCents != expectedBuysCents || sellsCents != expectedSellsCents) {
            val expected = if (buysCents != expectedBuysCents) summary.totalBuys else summary.totalSells
            val computed = if (buysCents != expectedBuysCents) buysCents / 100.0 else sellsCents / 100.0
            throw IllegalArgumentException(
                "buys/sells totals mismatch: expected $expected, got $computed",
            )
        }

        if (note.assets.size != note.assets.toSet().size) {
            val seen = mutableSetOf<NoteAsset>()
            note.assets.forEachIndexed { index, asset ->
                if (!seen.add(asset)) {
                    val firstIndex = note.assets.indexOf(asset)
                    throw IllegalArgumentException(
                        "duplicate asset detected: ${asset.ticker} at index $index is identical to asset at index $firstIndex",
                    )
                }
            }
        }
    }

    private fun throwNegative(field: String): Nothing =
        throw IllegalArgumentException("fee/volume fields must not be negative: $field")

    private fun Double.toCents(): Long = round(this * 100.0).toLong()
}
