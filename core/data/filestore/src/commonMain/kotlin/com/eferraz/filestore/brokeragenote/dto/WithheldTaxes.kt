package com.eferraz.filestore.brokeragenote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class WithheldTaxes(
    @SerialName("irrf_operacoes") val irrfOperations: Double,
    @SerialName("irrf_day_trade") val irrfDayTrade: Double,
)
