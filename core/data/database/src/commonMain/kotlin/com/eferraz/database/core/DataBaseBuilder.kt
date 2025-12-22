package com.eferraz.database.core

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.component.KoinComponent

internal interface DataBaseBuilder : KoinComponent {

    fun build(): AppDatabase {
        return buildPlatform()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    fun buildPlatform(): RoomDatabase.Builder<AppDatabase>

    fun databaseName() = "investiments-kmp-temp.db"
}