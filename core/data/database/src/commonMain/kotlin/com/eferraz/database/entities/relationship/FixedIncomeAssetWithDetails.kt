package com.eferraz.database.entities.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.FixedIncomeAssetEntity
import com.eferraz.database.entities.IssuerEntity

/**
 * Data class intermediária que representa um ativo de renda fixa completo.
 * Usa @Relation para definir os relacionamentos automaticamente.
 */
internal data class FixedIncomeAssetWithDetails(

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
    val fixedIncome: FixedIncomeAssetEntity
)

