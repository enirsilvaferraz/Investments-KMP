package com.eferraz.database.di

import com.eferraz.database.core.AppDatabase
import com.eferraz.database.core.PlatformDataBaseBuilder
import com.eferraz.database.daos.AssetDao
import com.eferraz.database.daos.AssetHoldingDao
import com.eferraz.database.daos.AssetTransactionDao
import com.eferraz.database.daos.BrokerageDao
import com.eferraz.database.daos.HoldingHistoryDao
import com.eferraz.database.daos.IssuerDao
import com.eferraz.database.daos.OwnerDao
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.eferraz.database")
public class DatabaseModule {

    @Single
    internal fun provideDatabase(): AppDatabase = PlatformDataBaseBuilder.build()

    @Single
    internal fun provideAssetDao(database: AppDatabase): AssetDao = database.assetDao()

    @Single
    internal fun provideAssetHoldingDao(database: AppDatabase): AssetHoldingDao = database.assetHoldingDao()

    @Single
    internal fun provideHoldingHistoryDao(database: AppDatabase): HoldingHistoryDao = database.holdingHistoryDao()

    @Single
    internal fun provideIssuerDao(database: AppDatabase): IssuerDao = database.issuerDao()

    @Single
    internal fun provideOwnerDao(database: AppDatabase): OwnerDao = database.ownerDao()

    @Single
    internal fun provideBrokerageDao(database: AppDatabase): BrokerageDao = database.brokerageDao()

    @Single
    internal fun provideAssetTransactionDao(database: AppDatabase): AssetTransactionDao = database.assetTransactionDao()
}