package com.eferraz.database.core

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eferraz.database.core.converters.Converters
import com.eferraz.database.daos.AssetDao
import com.eferraz.database.daos.AssetHoldingDao
import com.eferraz.database.daos.BrokerageDao
import com.eferraz.database.daos.HoldingHistoryDao
import com.eferraz.database.daos.IssuerDao
import com.eferraz.database.daos.OwnerDao
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.AssetHoldingEntity
import com.eferraz.database.entities.BrokerageEntity
import com.eferraz.database.entities.FixedIncomeAssetEntity
import com.eferraz.database.entities.HoldingHistoryEntryEntity
import com.eferraz.database.entities.InvestmentFundAssetEntity
import com.eferraz.database.entities.IssuerEntity
import com.eferraz.database.entities.OwnerEntity
import com.eferraz.database.entities.VariableIncomeAssetEntity

@Database(
    entities = [
        IssuerEntity::class,
        OwnerEntity::class,
        BrokerageEntity::class,
        AssetEntity::class,
        FixedIncomeAssetEntity::class,
        VariableIncomeAssetEntity::class,
        InvestmentFundAssetEntity::class,
        AssetHoldingEntity::class,
        HoldingHistoryEntryEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    version = 2
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun issuerDao(): IssuerDao
    abstract fun ownerDao(): OwnerDao
    abstract fun brokerageDao(): BrokerageDao
    abstract fun assetDao(): AssetDao
    abstract fun assetHoldingDao(): AssetHoldingDao
    abstract fun holdingHistoryDao(): HoldingHistoryDao
}