package com.eferraz.database.datasources

import com.eferraz.entities.Asset
import com.eferraz.entities.FixedIncomeAsset
import com.eferraz.entities.InvestmentFundAsset
import com.eferraz.entities.VariableIncomeAsset

public interface AssetDataSource {
    public suspend fun getAll(): List<Asset>
    public suspend fun getByID(id: Long): Asset?
    public suspend fun save(asset: FixedIncomeAsset): Long
    public suspend fun save(asset: InvestmentFundAsset): Long
    public suspend fun save(asset: VariableIncomeAsset): Long
}