package com.eferraz.filestore.brokeragenote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ApportionableFees(
    @SerialName("taxa_liquidacao") val settlement: Double,
    @SerialName("emolumentos") val emoluments: Double,
    @SerialName("taxa_transferencia") val transfer: Double,
    @SerialName("corretagem") val brokerage: Double,
    @SerialName("iss") val iss: Double,
    @SerialName("outras") val others: Double,
) {

    val total: Double get() =
        settlement + emoluments + transfer + brokerage + iss + others
}
