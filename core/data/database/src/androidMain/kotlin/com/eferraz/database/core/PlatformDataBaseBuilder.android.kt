package com.eferraz.database.core

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import org.koin.core.component.inject

internal actual object PlatformDataBaseBuilder : DataBaseBuilder {

    val context: Context by inject()

    actual override fun buildPlatform(): RoomDatabase.Builder<AppDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(databaseName())
        return Room.databaseBuilder<AppDatabase>(context = appContext, name = dbFile.absolutePath)
    }
}
