package com.eferraz.repositories

import com.eferraz.database.datasources.InvestmentRegistrationDataSource
import com.eferraz.entities.assets.Asset
import com.eferraz.usecases.repositories.RegisterInvestmentAssetPersistence
import org.koin.core.annotation.Factory

@Factory(binds = [RegisterInvestmentAssetPersistence::class])
internal class RegisterInvestmentAssetPersistenceImpl(
    private val investmentRegistrationDataSource: InvestmentRegistrationDataSource,
) : RegisterInvestmentAssetPersistence {

    override suspend fun persistNewAssetAndInitialHolding(
        asset: Asset,
        ownerId: Long,
        brokerageId: Long,
    ): Long = investmentRegistrationDataSource.saveNewAssetWithInitialHolding(
        asset = asset,
        ownerId = ownerId,
        brokerageId = brokerageId,
    )
}
