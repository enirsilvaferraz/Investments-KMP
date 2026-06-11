package com.eferraz.entities.brokeragenotes

/**
 * Asset traded on a brokerage note.
 *
 * @property specification Textual line specification; participates in structural equality.
 * @property grossValue Gross trade value from the source (`valor_bruto_total`); validated in Etapa 1.
 */
public data class NoteAsset(
    public val ticker: String,
    public val specification: String,
    public val tradeType: TradeType,
    public val quantity: Double,
    public val unitPrice: Double,
    public val grossValue: Double,
)
