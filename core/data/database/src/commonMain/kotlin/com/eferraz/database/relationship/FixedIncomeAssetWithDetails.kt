package com.eferraz.database.relationship

import androidx.room.Embedded
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.FixedIncomeAssetEntity
import com.eferraz.database.entities.IssuerEntity

/**
 * Data class intermediária que representa um ativo de renda fixa completo.
 * Inclui AssetEntity, IssuerEntity e FixedIncomeAssetEntity.
 * Usado para queries com JOIN entre assets, issuers e fixed_income_assets.
 */
internal data class FixedIncomeAssetWithDetails(

    @Embedded
    val asset: AssetEntity,

    @Embedded(prefix = "issuer_")
    val issuer: IssuerEntity,

    @Embedded(prefix = "fixed_income_")
    val fixedIncome: FixedIncomeAssetEntity
)

