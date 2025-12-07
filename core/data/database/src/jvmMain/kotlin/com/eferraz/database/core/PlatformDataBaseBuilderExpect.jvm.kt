package com.eferraz.database.core

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

internal actual object PlatformDataBaseBuilder : DataBaseBuilder {

    actual override fun buildPlatform(): RoomDatabase.Builder<AppDatabase> {
        val dbFile = File(System.getProperty("user.home")+"/Database", databaseName())
        println("Enir: java.io.tmpdir: [$dbFile]")
        return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
    }
}