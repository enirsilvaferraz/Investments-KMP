package com.eferraz.usecases.repositories

import com.eferraz.entities.assets.Asset

/**
 * Persiste um novo [Asset] e a [com.eferraz.entities.holdings.AssetHolding] inicial (sem meta)
 * numa única transação, para cumprir atomicidade no cadastro via diálogo.
 */
public fun interface RegisterInvestmentAssetPersistence {

    /**
     * @return id do ativo persistido
     */
    public suspend fun persistNewAssetAndInitialHolding(
        asset: Asset,
        ownerId: Long,
        brokerageId: Long,
    ): Long
}
