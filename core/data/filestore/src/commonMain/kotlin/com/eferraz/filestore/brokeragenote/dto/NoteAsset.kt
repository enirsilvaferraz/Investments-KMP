package com.eferraz.filestore.brokeragenote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NoteAsset(
    @SerialName("ticker") val ticker: String,
    @SerialName("especificacao") val specification: String,
    @SerialName("movimentacao") val movement: String,
    @SerialName("quantidade") val quantity: Double,
    @SerialName("valor_unitario") val unitPrice: Double,
    @SerialName("valor_bruto_total") val grossValue: Double,
)
