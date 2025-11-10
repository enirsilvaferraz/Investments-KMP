package com.eferraz.repositories

import com.eferraz.entities.AssetHolding
import org.koin.core.annotation.Factory

public interface InvestmentRepository {
    public fun getAllHoldings(): List<AssetHolding>
}

@Factory(binds = [InvestmentRepository::class])
internal class InvestmentRepositoryImpl : InvestmentRepository {
    override fun getAllHoldings(): List<AssetHolding> {
        return InMemoryDataSource.holdings
    }
}
