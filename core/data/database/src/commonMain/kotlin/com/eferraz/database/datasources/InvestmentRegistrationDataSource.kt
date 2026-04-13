package com.eferraz.database.datasources

import com.eferraz.entities.assets.Asset

/**
 * Persistência atómica: novo ativo (polimórfico) + linha inicial em `asset_holdings`.
 */
public fun interface InvestmentRegistrationDataSource {

    public suspend fun saveNewAssetWithInitialHolding(
        asset: Asset,
        ownerId: Long,
        brokerageId: Long,
    ): Long
}
