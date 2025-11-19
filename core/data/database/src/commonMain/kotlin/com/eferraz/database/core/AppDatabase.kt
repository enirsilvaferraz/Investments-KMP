package com.eferraz.database.core

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eferraz.database.converters.Converters
import com.eferraz.database.daos.AssetDao
import com.eferraz.database.daos.IssuerDao
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.FixedIncomeAssetEntity
import com.eferraz.database.entities.InvestmentFundAssetEntity
import com.eferraz.database.entities.IssuerEntity
import com.eferraz.database.entities.VariableIncomeAssetEntity

@Database(
    entities = [
        IssuerEntity::class,
        AssetEntity::class,
        FixedIncomeAssetEntity::class,
        VariableIncomeAssetEntity::class,
        InvestmentFundAssetEntity::class,
    ],
    version = 1
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun issuerDao(): IssuerDao
    abstract fun assetDao(): AssetDao
}