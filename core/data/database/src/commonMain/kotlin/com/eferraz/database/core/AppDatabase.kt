package com.eferraz.database.core

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eferraz.database.core.converters.Converters
import com.eferraz.database.daos.AssetDao
import com.eferraz.database.daos.AssetHoldingDao
import com.eferraz.database.daos.AssetTransactionDao
import com.eferraz.database.daos.BrokerageDao
import com.eferraz.database.daos.HoldingHistoryDao
import com.eferraz.database.daos.IssuerDao
import com.eferraz.database.daos.OwnerDao
import com.eferraz.database.entities.assets.AssetEntity
import com.eferraz.database.entities.holdings.AssetHoldingEntity
import com.eferraz.database.entities.transaction.AssetTransactionEntity
import com.eferraz.database.entities.supports.BrokerageEntity
import com.eferraz.database.entities.assets.FixedIncomeAssetEntity
import com.eferraz.database.entities.transaction.FixedIncomeTransactionEntity
import com.eferraz.database.entities.transaction.FundsTransactionEntity
import com.eferraz.database.entities.histories.HoldingHistoryEntryEntity
import com.eferraz.database.entities.assets.InvestmentFundAssetEntity
import com.eferraz.database.entities.supports.IssuerEntity
import com.eferraz.database.entities.supports.OwnerEntity
import com.eferraz.database.entities.assets.VariableIncomeAssetEntity
import com.eferraz.database.entities.transaction.VariableIncomeTransactionEntity
import com.eferraz.database.migrations.Migration3To4

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
        AssetTransactionEntity::class,
        FixedIncomeTransactionEntity::class,
        VariableIncomeTransactionEntity::class,
        FundsTransactionEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = Migration3To4::class),
    ],
    version = 4
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
    abstract fun assetTransactionDao(): AssetTransactionDao
}