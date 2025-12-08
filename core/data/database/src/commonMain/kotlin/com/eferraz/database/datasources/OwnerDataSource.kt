package com.eferraz.database.datasources

import com.eferraz.entities.Owner

public interface OwnerDataSource {
    public suspend fun getAll(): List<Owner>
    public suspend fun getFirst(): Owner?
}

