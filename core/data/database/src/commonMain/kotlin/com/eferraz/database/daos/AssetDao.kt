package com.eferraz.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.eferraz.database.entities.AssetEntity
import com.eferraz.database.entities.FixedIncomeAssetEntity
import com.eferraz.database.entities.InvestmentFundAssetEntity
import com.eferraz.database.entities.VariableIncomeAssetEntity
import com.eferraz.database.entities.relationship.AssetWithDetails

/**
 * DAO para operações CRUD na tabela assets e suas subclasses.
 * Suporta a estrutura polimórfica "Table per Subclass".
 */
@Dao
internal interface AssetDao {

    @Upsert
    suspend fun save(asset: AssetEntity): Long

    @Upsert
    suspend fun save(fixedIncome: FixedIncomeAssetEntity): Long

    @Upsert
    suspend fun save(variableIncome: VariableIncomeAssetEntity): Long

    @Upsert
    suspend fun save(investmentFund: InvestmentFundAssetEntity): Long

    @Transaction
    suspend fun save(asset: AssetWithDetails): Long {
        val id = save(asset.asset)
        asset.fixedIncome?.let { save(it.copy(assetId = id)) }
        asset.variableIncome?.let { save(it.copy(assetId = id)) }
        asset.funds?.let { save(it.copy(assetId = id)) }
        return id
    }

    @Transaction
    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun find(id: Long): AssetWithDetails?

    @Transaction
    @Query("SELECT * FROM assets")
    suspend fun getAll(): List<AssetWithDetails>
}
