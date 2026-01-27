package com.eferraz.usecases.repositories

import com.eferraz.entities.holdings.Owner

public interface OwnerRepository {
    public suspend fun getAll(): List<Owner>
    public suspend fun getFirst(): Owner?
}

