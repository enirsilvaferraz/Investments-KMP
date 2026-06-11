package com.eferraz.entities.brokeragenotes

/**
 * Fee allocation result for a single asset on a brokerage note.
 *
 * @property ticker Asset identifier (copied from [NoteAsset.ticker]).
 * @property grossValue Gross trade value in BRL.
 * @property allocatedFee Proportional share of note fees in BRL.
 * @property netValue Net value after fees: gross plus fee for BUY, gross minus fee for SELL.
 */
public data class AssetFeeAllocation internal constructor(
    public val ticker: String,
    public val tradeType: TradeType,
    public val grossValue: Double,
    public val allocatedFee: Double,
) {

    public val netValue: Double get() = when (tradeType) {
        TradeType.BUY -> grossValue + allocatedFee
        TradeType.SELL -> grossValue - allocatedFee
    }
}
