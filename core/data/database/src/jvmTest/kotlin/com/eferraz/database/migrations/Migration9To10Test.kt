package com.eferraz.database.migrations

import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.eferraz.database.core.AppDatabase
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class Migration9To10Test {

    private lateinit var dbPath: java.nio.file.Path
    private lateinit var helper: MigrationTestHelper

    @BeforeTest
    fun setUp() {
        dbPath = createTempFile(prefix = "migration-9-10-", suffix = ".db")
        helper = MigrationTestHelper(
            schemaDirectoryPath = resolveSchemaDirectory(),
            databasePath = dbPath,
            driver = BundledSQLiteDriver(),
            databaseClass = AppDatabase::class,
            databaseFactory = { buildTestDatabase(dbPath) },
            autoMigrationSpecs = listOf(
                Migration3To4(),
                Migration6To7(),
                Migration8To9(),
            ),
        )
    }

    @AfterTest
    fun tearDown() {
        dbPath.deleteIfExists()
    }

    /**
     * Legacy RV/RF/Fund satellite rows migrate to flat quantity and unitPrice without data loss.
     */
    @Test
    fun `GIVEN v9 satellite transaction rows WHEN migrate 9 to 10 THEN quantity and unitPrice are preserved`() =
        runTest {
            // GIVEN
            val connection = helper.createDatabase(START_VERSION)
            seedLegacyTransactions(connection)
            connection.close()

            // WHEN
            val migrated = helper.runMigrationsAndValidate(
                version = END_VERSION,
                migrations = listOf(Migration9To10),
            )

            // THEN
            assertTransaction(migrated, id = 1L, quantity = 100.0, unitPrice = 25.5)
            assertTransaction(migrated, id = 2L, quantity = 1.0, unitPrice = 5000.0)
            assertTransaction(migrated, id = 3L, quantity = 1.0, unitPrice = 1200.75)
            assertFalse(migrated.tableExists("variable_income_transactions"))
            assertFalse(migrated.tableExists("fixed_income_transactions"))
            assertFalse(migrated.tableExists("funds_transactions"))
            assertNull(migrated.tableColumn("asset_transactions", "observations"))
            assertNull(migrated.tableColumn("asset_transactions", "asset_class"))
            migrated.close()
        }

    private fun seedLegacyTransactions(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys=OFF")
        connection.execSQL("INSERT INTO issuers (id, name) VALUES (1, 'Issuer')")
        connection.execSQL("INSERT INTO owners (id, name) VALUES (1, 'Owner')")
        connection.execSQL("INSERT INTO brokerages (id, name) VALUES (1, 'Broker')")
        connection.execSQL(
            """
            INSERT INTO assets (id, name, issuerId, asset_class, liquidity)
            VALUES (1, 'Asset', 1, 'VARIABLE_INCOME', 'DAILY')
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO asset_holdings (id, assetId, ownerId, brokerageId)
            VALUES (1, 1, 1, 1)
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO asset_transactions
                (id, holdingId, transactionDate, type, asset_class, observations)
            VALUES (1, 1, '2025-01-15', 'PURCHASE', 'VARIABLE_INCOME', 'legacy note')
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO variable_income_transactions (transactionId, quantity, unitPrice)
            VALUES (1, 100.0, 25.5)
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO asset_transactions (id, holdingId, transactionDate, type, asset_class)
            VALUES (2, 1, '2025-01-16', 'PURCHASE', 'FIXED_INCOME')
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO fixed_income_transactions (transactionId, totalValue)
            VALUES (2, 5000.0)
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO asset_transactions (id, holdingId, transactionDate, type, asset_class)
            VALUES (3, 1, '2025-01-17', 'PURCHASE', 'INVESTMENT_FUND')
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO funds_transactions (transactionId, totalValue)
            VALUES (3, 1200.75)
            """.trimIndent()
        )
        connection.execSQL("PRAGMA foreign_keys=ON")
    }

    private fun assertTransaction(
        connection: SQLiteConnection,
        id: Long,
        quantity: Double,
        unitPrice: Double,
    ) {
        connection.prepare(
            """
            SELECT quantity, unitPrice
            FROM asset_transactions
            WHERE id = ?
            """.trimIndent()
        ).use { statement ->
            statement.bindLong(1, id)
            assertTrue(statement.step(), "transaction $id should exist")
            assertEquals(quantity, statement.getDouble(0), QUANTITY_DELTA)
            assertEquals(unitPrice, statement.getDouble(1), MONEY_DELTA)
        }
    }

    private fun SQLiteConnection.tableExists(tableName: String): Boolean =
        prepare(
            """
            SELECT 1 FROM sqlite_master
            WHERE type = 'table' AND name = ?
            """.trimIndent()
        ).use { statement ->
            statement.bindText(1, tableName)
            statement.step()
        }

    private fun SQLiteConnection.tableColumn(tableName: String, columnName: String): String? =
        prepare("PRAGMA table_info(`$tableName`)").use { statement ->
            while (statement.step()) {
                if (statement.getText(1) == columnName) {
                    return statement.getText(1)
                }
            }
            null
        }

    private companion object {
        const val START_VERSION = 9
        const val END_VERSION = 10
        const val QUANTITY_DELTA = 0.001
        const val MONEY_DELTA = 0.01

        fun resolveSchemaDirectory(): java.nio.file.Path {
            val candidates = listOf(
                Path("schemas"),
                Path("core/data/database/schemas"),
            )
            return candidates.firstOrNull { it.exists() }
                ?: error("Schema directory not found. Tried: ${candidates.joinToString()}")
        }

        fun buildTestDatabase(dbPath: java.nio.file.Path): AppDatabase =
            Room.databaseBuilder<AppDatabase>(name = dbPath.toString())
                .addMigrations(Migration9To10)
                .setDriver(BundledSQLiteDriver())
                .build()
    }
}
