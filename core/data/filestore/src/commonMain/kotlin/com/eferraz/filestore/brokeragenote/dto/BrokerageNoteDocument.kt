package com.eferraz.filestore.brokeragenote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BrokerageNoteDocument(
    @SerialName("metadados") val metadata: NoteMetadata,
    @SerialName("resumo_financeiro") val financialSummary: NoteFinancialSummary,
    @SerialName("ativos") val assets: List<NoteAsset>,
)
