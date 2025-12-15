package com.eferraz.database.entities.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.FixedIncomeAssetEntity
import com.eferraz.database.entities.InvestmentFundAssetEntity
import com.eferraz.database.entities.IssuerEntity
import com.eferraz.database.entities.VariableIncomeAssetEntity

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
    val fixedIncome: FixedIncomeAssetEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "assetId"
    )
    val variableIncome: VariableIncomeAssetEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "assetId"
    )
    val funds: InvestmentFundAssetEntity?
)
