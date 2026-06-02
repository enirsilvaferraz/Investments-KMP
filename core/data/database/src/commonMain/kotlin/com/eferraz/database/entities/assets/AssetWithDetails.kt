package com.eferraz.database.entities.assets

import androidx.room3.Embedded
import androidx.room3.Relation
import com.eferraz.database.entities.supports.IssuerEntity

/**
 * Data class intermediária que representa um ativo completo com seus detalhes específicos.
 * Usa @Relation para definir os relacionamentos automaticamente.
 */
internal data class AssetWithDetails(

    @Embedded
    val asset: AssetEntity,

    @Relation(
        parentColumns = ["issuerId"],
        entityColumns = ["id"],
    )
    val issuer: IssuerEntity,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["assetId"],
    )
    val fixedIncome: FixedIncomeAssetEntity? = null,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["assetId"],
    )
    val variableIncome: VariableIncomeAssetEntity? = null,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["assetId"],
    )
    val funds: InvestmentFundAssetEntity? = null,
)
