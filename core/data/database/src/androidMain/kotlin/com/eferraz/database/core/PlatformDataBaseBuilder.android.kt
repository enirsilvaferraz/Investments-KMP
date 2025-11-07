package com.eferraz.database.core

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.component.inject

internal actual object PlatformDataBaseBuilder : DataBaseBuilder {

    val context: Context by inject()

    actual override fun buildPlatform(): RoomDatabase.Builder<AppDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(databaseName())
        return Room.databaseBuilder<AppDatabase>(context = appContext, name = dbFile.absolutePath)
    }
}