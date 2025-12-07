package com.eferraz.usecases.repositories

import com.eferraz.entities.Issuer

public interface IssuerRepository {
    public suspend fun getAll(): List<Issuer>
    public suspend fun getByName(name: String): Issuer?
    public suspend fun create(name: String): Issuer
}

