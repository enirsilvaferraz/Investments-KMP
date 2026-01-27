package com.eferraz.database.datasources

import com.eferraz.entities.holdings.Brokerage

public interface BrokerageDataSource {
    public suspend fun getAll(): List<Brokerage>
    public suspend fun getByName(name: String): Brokerage?
}

