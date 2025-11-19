package com.eferraz.database.entities.relationship

import androidx.room.Embedded
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.IssuerEntity
import com.eferraz.database.entities.VariableIncomeAssetEntity

/**
 * Data class intermediária que representa um ativo de renda variável completo.
 * Inclui AssetEntity, IssuerEntity e VariableIncomeAssetEntity.
 * Usado para queries com JOIN entre assets, issuers e variable_income_assets.
 */
internal data class VariableIncomeAssetWithDetails(

    @Embedded
    val asset: AssetEntity,

    @Embedded(prefix = "issuer_")
    val issuer: IssuerEntity,

    @Embedded(prefix = "variable_income_")
    val variableIncome: VariableIncomeAssetEntity
)

