package com.eferraz.database.core

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.eferraz.entities.runtime.RuntimeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.component.KoinComponent

internal interface DataBaseBuilder : KoinComponent {

    fun build(): AppDatabase =
        buildPlatform()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    fun buildPlatform(): RoomDatabase.Builder<AppDatabase>

    fun databaseName(): String =
        if (RuntimeConfig.ENV_HML) "investiments-kmp-temp.db"
        else "investiments-kmp.db"
}
