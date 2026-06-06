package com.eferraz.database.migrations

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Migração 9→10 manual: achata transações — qty/unitPrice na tabela base.
 *
 * AutoMigration com [@DeleteTable][androidx.room3.DeleteTable] remove satélites
 * **antes** de [onPostMigrate][androidx.room3.migration.AutoMigrationSpec.onPostMigrate],
 * impossibilitando copiar dados legados (R3). Migração manual garante ordem correcta.
 */
internal object Migration9To10 : Migration(9, 10) {

    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE asset_transactions ADD COLUMN quantity REAL NOT NULL DEFAULT 1"
        )
        connection.execSQL(
            "ALTER TABLE asset_transactions ADD COLUMN unitPrice REAL NOT NULL DEFAULT 0"
        )

        copySatelliteData(connection)
        dropSatelliteTables(connection)
        rebuildAssetTransactionsWithoutLegacyColumns(connection)
    }

    private fun copySatelliteData(connection: SQLiteConnection) {
        connection.execSQL(
            """
            UPDATE asset_transactions
            SET quantity = (
                SELECT vit.quantity FROM variable_income_transactions vit
                WHERE vit.transactionId = asset_transactions.id
            ),
            unitPrice = (
                SELECT vit.unitPrice FROM variable_income_transactions vit
                WHERE vit.transactionId = asset_transactions.id
            )
            WHERE EXISTS (
                SELECT 1 FROM variable_income_transactions vit
                WHERE vit.transactionId = asset_transactions.id
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            UPDATE asset_transactions
            SET quantity = 1,
            unitPrice = (
                SELECT fit.totalValue FROM fixed_income_transactions fit
                WHERE fit.transactionId = asset_transactions.id
            )
            WHERE EXISTS (
                SELECT 1 FROM fixed_income_transactions fit
                WHERE fit.transactionId = asset_transactions.id
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            UPDATE asset_transactions
            SET quantity = 1,
            unitPrice = (
                SELECT ft.totalValue FROM funds_transactions ft
                WHERE ft.transactionId = asset_transactions.id
            )
            WHERE EXISTS (
                SELECT 1 FROM funds_transactions ft
                WHERE ft.transactionId = asset_transactions.id
            )
            """.trimIndent()
        )
    }

    private fun dropSatelliteTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS variable_income_transactions")
        connection.execSQL("DROP TABLE IF EXISTS fixed_income_transactions")
        connection.execSQL("DROP TABLE IF EXISTS funds_transactions")
    }

    private fun rebuildAssetTransactionsWithoutLegacyColumns(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys=OFF")
        connection.execSQL("DROP INDEX IF EXISTS index_asset_transactions_asset_class")
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `asset_transactions_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `holdingId` INTEGER NOT NULL,
                `transactionDate` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `quantity` REAL NOT NULL DEFAULT 1,
                `unitPrice` REAL NOT NULL DEFAULT 0,
                FOREIGN KEY(`holdingId`) REFERENCES `asset_holdings`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO `asset_transactions_new`
                (`id`, `holdingId`, `transactionDate`, `type`, `quantity`, `unitPrice`)
            SELECT `id`, `holdingId`, `transactionDate`, `type`, `quantity`, `unitPrice`
            FROM `asset_transactions`
            """.trimIndent()
        )
        connection.execSQL("DROP TABLE `asset_transactions`")
        connection.execSQL("ALTER TABLE `asset_transactions_new` RENAME TO `asset_transactions`")
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_asset_transactions_holdingId` " +
                "ON `asset_transactions` (`holdingId`)"
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_asset_transactions_transactionDate` " +
                "ON `asset_transactions` (`transactionDate`)"
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_asset_transactions_type` " +
                "ON `asset_transactions` (`type`)"
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_asset_transactions_holdingId_transactionDate` " +
                "ON `asset_transactions` (`holdingId`, `transactionDate`)"
        )
        connection.execSQL("PRAGMA foreign_keys=ON")
    }
}
