package com.eferraz.database.entities.assets

import androidx.room.Embedded
import androidx.room.Relation
import com.eferraz.database.entities.IssuerEntity

/**
 * Data class intermediária que representa um ativo completo com seus detalhes específicos.
 * Usa @Relation para definir os relacionamentos automaticamente.
 */
internal data class AssetWithDetails(

    @Embedded
    val asset: AssetEntity,

    @Relation(
        parentColumn = "issuerId",
        entityColumn = "id"
    )
    val issuer: IssuerEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "assetId"
    )
    val fixedIncome: FixedIncomeAssetEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "assetId"
    )
    val variableIncome: VariableIncomeAssetEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "assetId"
    )
    val funds: InvestmentFundAssetEntity? = null,
)