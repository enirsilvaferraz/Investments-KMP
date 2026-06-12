package com.eferraz.filestore.brokeragenote

import com.eferraz.entities.brokeragenotes.BrokerageNote
import com.eferraz.entities.brokeragenotes.BrokerageNoteAsset
import com.eferraz.entities.brokeragenotes.TradeType
import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.TransactionType
import com.eferraz.filestore.brokeragenote.dto.BrokerageNoteDocument

internal object BrokerageNoteV2Parser {

    internal fun parse(note: BrokerageNoteDocument): BrokerageNote = BrokerageNote(
        totalVolumeTraded = note.financialSummary.totalVolumeTraded,
        apportionableFees = note.financialSummary.apportionableFees.total,
        withheldTaxes = note.financialSummary.withheldTaxes.run {
            irrfOperations + irrfDayTrade
        },
        netValue = note.metadata.netValue,
        assets = note.assets.map { asset ->
            BrokerageNoteAsset(
                ticker = asset.ticker,
                transaction = AssetTransaction(
                    id = 0,
                    date = note.metadata.tradingDate,
                    type = when (TradeType.fromMovement(asset.movement)) {
                        TradeType.BUY -> TransactionType.PURCHASE
                        TradeType.SELL -> TransactionType.SALE
                    },
                    quantity = asset.quantity,
                    unitPrice = asset.unitPrice,
                ),
            )
        },
    )
}