package com.eferraz.database.datasources.impl

import androidx.room3.withWriteTransaction
import com.eferraz.database.core.AppDatabase
import com.eferraz.database.datasources.InvestmentRegistrationDataSource
import com.eferraz.database.entities.holdings.AssetHoldingEntity
import com.eferraz.database.mappers.toEntity
import com.eferraz.entities.assets.Asset
import org.koin.core.annotation.Factory

@Factory(binds = [InvestmentRegistrationDataSource::class])
internal class InvestmentRegistrationDataSourceImpl(
    private val db: AppDatabase,
) : InvestmentRegistrationDataSource {

    override suspend fun saveNewAssetWithInitialHolding(
        asset: Asset,
        ownerId: Long,
        brokerageId: Long,
    ): Long = db.withWriteTransaction {
        val details = asset.toEntity()
        val assetId = db.assetDao().save(details)
        val holding = AssetHoldingEntity(
            id = 0L,
            assetId = assetId,
            ownerId = ownerId,
            brokerageId = brokerageId,
            goalId = null,
        )
        db.assetHoldingDao().upsert(holding)
        assetId
    }
}
