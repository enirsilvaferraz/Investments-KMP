package com.eferraz.database.entities.relationship

import androidx.room.Embedded
import androidx.room.Relation
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.IssuerEntity
import com.eferraz.database.entities.VariableIncomeAssetEntity

/**
 * Data class intermediária que representa um ativo de renda variável completo.
 * Usa @Relation para definir os relacionamentos automaticamente.
 */
internal data class VariableIncomeAssetWithDetails(

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
    val variableIncome: VariableIncomeAssetEntity
)

