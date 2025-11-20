package com.eferraz.database.entities.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.InvestmentFundAssetEntity
import com.eferraz.database.entities.IssuerEntity

/**
 * Data class intermediária que representa um fundo de investimento completo.
 * Usa @Relation para definir os relacionamentos automaticamente.
 */
internal data class InvestmentFundAssetWithDetails(

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
    val investmentFund: InvestmentFundAssetEntity
)

