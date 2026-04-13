package com.eferraz.database.datasources.impl

import androidx.room3.withWriteTransaction
import com.eferraz.database.core.AppDatabase
import com.eferraz.database.datasources.InvestmentRegistrationDataSource
import com.eferraz.database.entities.holdings.AssetHoldingEntity
import com.eferraz.database.mappers.toEntity
import com.eferraz.entities.assets.Asset
import com.eferraz.entities.assets.Issuer
import com.eferraz.entities.holdings.Brokerage
import org.koin.core.annotation.Factory

@Factory(binds = [InvestmentRegistrationDataSource::class])
internal class InvestmentRegistrationDataSourceImpl(
    private val db: AppDatabase,
) : InvestmentRegistrationDataSource {

    override suspend fun saveNewAssetWithInitialHolding(
        asset: Asset,
        ownerId: Long,
        brokerage: Brokerage,
        issuer: Issuer,
    ): Long = db.withWriteTransaction {
        check(asset.issuer == issuer) {
            "Emissor do ativo não coincide com o emissor passado à persistência"
        }
        val details = asset.toEntity()
        val assetId = db.assetDao().save(details)
        val holding = AssetHoldingEntity(
            id = 0L,
            assetId = assetId,
            ownerId = ownerId,
            brokerageId = brokerage.id,
            goalId = null,
        )
        db.assetHoldingDao().upsert(holding)
        assetId
    }
}
