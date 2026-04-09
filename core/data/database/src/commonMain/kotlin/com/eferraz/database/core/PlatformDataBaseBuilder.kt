package com.eferraz.database.core

import androidx.room3.RoomDatabase

internal expect object PlatformDataBaseBuilder : DataBaseBuilder {

    override fun buildPlatform(): RoomDatabase.Builder<AppDatabase>
}
