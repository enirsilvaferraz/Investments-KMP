package com.eferraz.database.core

import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.io.File

internal actual object PlatformDataBaseBuilder : DataBaseBuilder {

    actual override fun buildPlatform(): RoomDatabase.Builder<AppDatabase> {
        val dbFile = File(System.getProperty("user.home") + "/Database", databaseName())
        println("Database path: [$dbFile]")
        return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
    }
}
