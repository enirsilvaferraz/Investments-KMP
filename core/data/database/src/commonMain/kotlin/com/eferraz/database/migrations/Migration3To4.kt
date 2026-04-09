package com.eferraz.database.migrations

import androidx.room3.DeleteColumn
import androidx.room3.migration.AutoMigrationSpec

@DeleteColumn(tableName = "asset_holdings", columnName = "quantity")
@DeleteColumn(tableName = "asset_holdings", columnName = "averageCost")
@DeleteColumn(tableName = "asset_holdings", columnName = "investedValue")
@DeleteColumn(tableName = "asset_holdings", columnName = "currentValue")
internal class Migration3To4 : AutoMigrationSpec
