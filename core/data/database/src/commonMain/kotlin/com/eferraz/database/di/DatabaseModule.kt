package com.eferraz.database.di

import com.eferraz.database.core.AppDatabase
import com.eferraz.database.core.PlatformDataBaseBuilder
import com.eferraz.database.daos.AssetDao
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
}