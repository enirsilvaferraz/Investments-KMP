package com.eferraz.database.datasources

import com.eferraz.entities.assets.Issuer

public interface IssuerDataSource {
    public suspend fun getAll(): List<Issuer>
    public suspend fun getByName(name: String): Issuer?
    public suspend fun create(name: String): Issuer
}

