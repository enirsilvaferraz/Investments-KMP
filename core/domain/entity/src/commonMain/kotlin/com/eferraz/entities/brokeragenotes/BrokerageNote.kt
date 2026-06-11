package com.eferraz.entities.brokeragenotes

/**
 * SINACOR brokerage note with mixed buy and sell operations.
 */
public data class BrokerageNote(
    public val metadata: BrokerageNoteMetadata,
    public val financialSummary: FinancialSummary,
    public val assets: List<NoteAsset>,
)
