package com.eferraz.database.migrations

import androidx.room3.RenameColumn
import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Migração atómica 6→7: renomeia colunas ao vocabulário de taxonomia 016
 * (`asset_class`, `indexer`, `type`) sem alterar valores enum persistidos,
 * excepto normalização `FUNDS` → `INVESTMENT_FUND` em transações (R5).
 */
@RenameColumn(tableName = "assets", fromColumnName = "category", toColumnName = "asset_class")
@RenameColumn(tableName = "asset_transactions", fromColumnName = "category", toColumnName = "asset_class")
@RenameColumn(tableName = "fixed_income_assets", fromColumnName = "type", toColumnName = "indexer")
@RenameColumn(tableName = "fixed_income_assets", fromColumnName = "subType", toColumnName = "type")
internal class Migration6To7 : AutoMigrationSpec {

    override suspend fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL(
            "UPDATE asset_transactions SET asset_class = 'INVESTMENT_FUND' WHERE asset_class = 'FUNDS'"
        )
    }
}
