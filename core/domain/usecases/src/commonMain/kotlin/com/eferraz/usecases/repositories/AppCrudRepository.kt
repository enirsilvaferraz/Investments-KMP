package com.eferraz.usecases.repositories

public interface AppCrudRepository<T> {

    public suspend fun upsert(model: T): Long

    public suspend fun getById(id: Long): T?

    public suspend fun getAll(): List<T>

    public suspend fun delete(id: Long)
}