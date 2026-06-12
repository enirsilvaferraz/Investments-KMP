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
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class Migration10To11Test {

    private lateinit var dbPath: java.nio.file.Path
    private lateinit var helper: MigrationTestHelper

    @BeforeTest
    fun setUp() {
        dbPath = createTempFile(prefix = "migration-10-11-", suffix = ".db")
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
     * Existing v10 transaction rows keep data and receive allocatedFee default 0.
     */
    @Test
    fun `GIVEN v10 transaction rows WHEN migrate 10 to 11 THEN data preserved and allocatedFee is zero`() =
        runTest {
            // GIVEN
            val connection = helper.createDatabase(START_VERSION)
            seedTransactions(connection)
            connection.close()

            // WHEN
            val migrated = helper.runMigrationsAndValidate(
                version = END_VERSION,
                migrations = emptyList(),
            )

            // THEN
            assertTransaction(migrated, id = 1L, quantity = 100.0, unitPrice = 25.5, allocatedFee = 0.0)
            assertTransaction(migrated, id = 2L, quantity = 50.0, unitPrice = 10.0, allocatedFee = 0.0)
            migrated.close()
        }

    /**
     * Empty v10 database migrates to v11 without error.
     */
    @Test
    fun `GIVEN empty v10 database WHEN migrate 10 to 11 THEN migration completes`() = runTest {

        // GIVEN
        val connection = helper.createDatabase(START_VERSION)
        connection.close()

        // WHEN
        val migrated = helper.runMigrationsAndValidate(
            version = END_VERSION,
            migrations = emptyList(),
        )

        // THEN
        assertTrue(migrated.tableExists("asset_transactions"))
        migrated.close()
    }

    private fun seedTransactions(connection: SQLiteConnection) {
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
                (id, holdingId, transactionDate, type, quantity, unitPrice)
            VALUES (1, 1, '2025-01-15', 'PURCHASE', 100.0, 25.5)
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO asset_transactions
                (id, holdingId, transactionDate, type, quantity, unitPrice)
            VALUES (2, 1, '2025-01-16', 'SALE', 50.0, 10.0)
            """.trimIndent()
        )
        connection.execSQL("PRAGMA foreign_keys=ON")
    }

    private fun assertTransaction(
        connection: SQLiteConnection,
        id: Long,
        quantity: Double,
        unitPrice: Double,
        allocatedFee: Double,
    ) {
        connection.prepare(
            """
            SELECT quantity, unitPrice, allocatedFee
            FROM asset_transactions
            WHERE id = ?
            """.trimIndent()
        ).use { statement ->
            statement.bindLong(1, id)
            assertTrue(statement.step(), "transaction $id should exist")
            assertEquals(quantity, statement.getDouble(0), QUANTITY_DELTA)
            assertEquals(unitPrice, statement.getDouble(1), MONEY_DELTA)
            assertEquals(allocatedFee, statement.getDouble(2), MONEY_DELTA)
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

    private companion object {
        const val START_VERSION = 10
        const val END_VERSION = 11
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
                .setDriver(BundledSQLiteDriver())
                .build()
    }
}
