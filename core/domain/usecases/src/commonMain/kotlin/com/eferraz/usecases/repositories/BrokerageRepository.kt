package com.eferraz.usecases.repositories

import com.eferraz.entities.Brokerage

public interface BrokerageRepository {
    public suspend fun getAll(): List<Brokerage>
    public suspend fun getByName(name: String): Brokerage?
}

