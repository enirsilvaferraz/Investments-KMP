package com.eferraz.entities.brokeragenotes

import kotlin.math.round

/**
 * Asset traded on a brokerage note.
 *
 * @property ticker Opaque asset identifier.
 * @property tradeType Buy or sell direction.
 * @property quantity Traded quantity (must be > 0; validated in [NoteFeeAllocation.calculate]).
 * @property unitPrice Unit price in BRL (must be > 0; validated in [NoteFeeAllocation.calculate]).
 */
public data class NoteAsset(
    public val ticker: String,
    public val tradeType: TradeType,
    public val quantity: Double,
    public val unitPrice: Double,
) {

    public val grossValue: Double =
        quantity * unitPrice

    public val grossValueCents: Long =
        round(grossValue * 100).toLong()
}
