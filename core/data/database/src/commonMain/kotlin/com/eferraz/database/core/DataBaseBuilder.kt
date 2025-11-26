package com.eferraz.database.core

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.eferraz.database.core.initialization.DatabaseSeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.component.KoinComponent

internal interface DataBaseBuilder : KoinComponent {

    fun build(): AppDatabase {
        return buildPlatform()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addCallback(createInitialDataCallback())
            .build()
    }

    fun buildPlatform(): RoomDatabase.Builder<AppDatabase>

    fun databaseName() = "investiments-kmp.db"

    private fun createInitialDataCallback() = object : RoomDatabase.Callback() {

        override fun onCreate(connection: SQLiteConnection) {
            super.onCreate(connection)
            DatabaseSeedData.inserts.forEach { sql -> connection.execSQL(sql) }
        }
    }
}