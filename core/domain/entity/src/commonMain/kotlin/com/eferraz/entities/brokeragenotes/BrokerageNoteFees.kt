package com.eferraz.entities.brokeragenotes

/**
 * Fees charged on a brokerage note.
 *
 * @property emoluments Exchange emoluments in BRL.
 * @property settlement Settlement fee in BRL.
 * @property incomeTax Income tax withheld in BRL.
 */
public data class BrokerageNoteFees(
    public val emoluments: Double,
    public val settlement: Double,
    public val incomeTax: Double,
) {
    public val total: Double get() = emoluments + settlement + incomeTax
}
