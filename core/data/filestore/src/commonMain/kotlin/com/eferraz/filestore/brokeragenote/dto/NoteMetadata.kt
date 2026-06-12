package com.eferraz.filestore.brokeragenote.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NoteMetadata(
    @SerialName("numero_nota") val noteNumber: String,
    @Serializable(with = BrazilianLocalDateSerializer::class)
    @SerialName("data_pregao") val tradingDate: LocalDate,
    @Serializable(with = BrazilianLocalDateSerializer::class)
    @SerialName("data_liquidacao") val settlementDate: LocalDate,
    @SerialName("corretora") val brokerage: String,
    @SerialName("cnpj_corretora") val brokerageDocument: String,
    @SerialName("valor_liquido_nota") val netValue: Double,
)
