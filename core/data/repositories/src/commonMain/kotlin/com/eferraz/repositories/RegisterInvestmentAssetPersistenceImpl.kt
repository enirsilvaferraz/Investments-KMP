package com.eferraz.repositories

import com.eferraz.database.datasources.InvestmentRegistrationDataSource
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.holdings.Brokerage
import com.eferraz.usecases.repositories.RegisterInvestmentAssetPersistence
import org.koin.core.annotation.Factory

@Factory(binds = [RegisterInvestmentAssetPersistence::class])
internal class RegisterInvestmentAssetPersistenceImpl(
    private val investmentRegistrationDataSource: InvestmentRegistrationDataSource,
) : RegisterInvestmentAssetPersistence {

    override suspend fun persistNewAssetAndInitialHolding(
        asset: Asset,
        ownerId: Long,
        brokerage: Brokerage,
        issuer: Issuer,
    ): Long = investmentRegistrationDataSource.saveNewAssetWithInitialHolding(
        asset = asset,
        ownerId = ownerId,
        brokerage = brokerage,
        issuer = issuer,
    )
}
