package com.eferraz.database.core

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal actual object PlatformDataBaseBuilder : DataBaseBuilder {

    actual override fun buildPlatform(): RoomDatabase.Builder<AppDatabase> {
        val dbFilePath = documentDirectory() + "/" + databaseName()
        return Room.databaseBuilder<AppDatabase>(name = dbFilePath)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )

        return requireNotNull(documentDirectory?.path)
    }
}