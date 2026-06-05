package com.eferraz.database.migrations

import androidx.room3.DeleteColumn
import androidx.room3.migration.AutoMigrationSpec

/**
 * Migração 8→9: remove colunas `liquidityDays` e `expirationDate` de
 * `investment_fund_assets` — campos movidos para a camada de domínio/UI.
 */
@DeleteColumn(tableName = "investment_fund_assets", columnName = "liquidityDays")
@DeleteColumn(tableName = "investment_fund_assets", columnName = "expirationDate")
internal class Migration8To9 : AutoMigrationSpec
