package com.eferraz.database.datasources

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.holdings.Brokerage

/**
 * Persistência atómica: novo ativo (polimórfico) + linha inicial em `asset_holdings`.
 */
public fun interface InvestmentRegistrationDataSource {

    public suspend fun saveNewAssetWithInitialHolding(
        asset: Asset,
        ownerId: Long,
        brokerage: Brokerage,
        issuer: Issuer,
    ): Long
}
