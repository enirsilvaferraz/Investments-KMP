package com.eferraz.entities.brokeragenotes

import kotlinx.datetime.LocalDate

/**
 * SINACOR brokerage note with mixed buy and sell operations.
 *
 * @property date Settlement date.
 * @property netValue Accounting net value: positive = client debit, negative = client credit.
 * @property fees Fees charged on the note.
 * @property assets Traded assets (must not be empty; validated in [NoteFeeAllocation.calculate]).
 */
public data class BrokerageNote(
    public val date: LocalDate,
    public val netValue: Double,
    public val fees: BrokerageNoteFees,
    public val assets: List<NoteAsset>,
)
