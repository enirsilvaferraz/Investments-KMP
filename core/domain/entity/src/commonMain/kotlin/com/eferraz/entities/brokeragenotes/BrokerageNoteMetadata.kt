package com.eferraz.entities.brokeragenotes

import kotlinx.datetime.LocalDate

/**
 * Header metadata for a SINACOR brokerage note.
 *
 * @property netValue Accounting net value: positive = client debit, negative = client credit.
 */
public data class BrokerageNoteMetadata(
    public val noteNumber: String,
    public val tradingDate: LocalDate,
    public val settlementDate: LocalDate,
    public val brokerage: String,
    public val brokerageDocument: String,
    public val netValue: Double,
)
