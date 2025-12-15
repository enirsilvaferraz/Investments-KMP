package com.eferraz.database.migrations

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn(tableName = "asset_holdings", columnName = "quantity")
@DeleteColumn(tableName = "asset_holdings", columnName = "averageCost")
@DeleteColumn(tableName = "asset_holdings", columnName = "investedValue")
@DeleteColumn(tableName = "asset_holdings", columnName = "currentValue")
internal class Migration3To4 : AutoMigrationSpec
