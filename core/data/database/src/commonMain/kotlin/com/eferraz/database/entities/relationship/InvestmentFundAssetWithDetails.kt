package com.eferraz.database.entities.relationship

import androidx.room.Embedded
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.InvestmentFundAssetEntity
import com.eferraz.database.entities.IssuerEntity

/**
 * Data class intermediária que representa um fundo de investimento completo.
 * Inclui AssetEntity, IssuerEntity e InvestmentFundAssetEntity.
 * Usado para queries com JOIN entre assets, issuers e investment_fund_assets.
 */
internal data class InvestmentFundAssetWithDetails(

    @Embedded
    val asset: AssetEntity,

    @Embedded(prefix = "issuer_")
    val issuer: IssuerEntity,

    @Embedded(prefix = "investment_fund_")
    val investmentFund: InvestmentFundAssetEntity
)

