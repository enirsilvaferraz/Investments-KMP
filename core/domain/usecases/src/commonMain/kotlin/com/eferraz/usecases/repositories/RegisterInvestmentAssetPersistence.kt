package com.eferraz.usecases.repositories

import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.holdings.Brokerage

/**
 * Persiste um novo [Asset] e a [com.eferraz.entities.holdings.AssetHolding] inicial (sem meta)
 * numa única transação, para cumprir atomicidade no cadastro via diálogo.
 */
public fun interface RegisterInvestmentAssetPersistence {

    /**
     * [issuer] deve coincidir com [Asset.issuer] do [asset] (verificação na implementação em dados).
     *
     * @return id do ativo persistido
     */
    public suspend fun persistNewAssetAndInitialHolding(
        asset: Asset,
        ownerId: Long,
        brokerage: Brokerage,
        issuer: Issuer,
    ): Long
}
