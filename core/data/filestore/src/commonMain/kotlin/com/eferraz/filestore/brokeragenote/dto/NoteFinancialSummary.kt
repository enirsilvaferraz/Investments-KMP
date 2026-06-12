package com.eferraz.filestore.brokeragenote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NoteFinancialSummary(
    @SerialName("volume_total_operado") val totalVolumeTraded: Double,
    @SerialName("total_compras_vista") val totalBuys: Double,
    @SerialName("total_vendas_vista") val totalSells: Double,
    @SerialName("taxas_rateaveis") val apportionableFees: ApportionableFees,
    @SerialName("impostos_retidos") val withheldTaxes: WithheldTaxes,
)
