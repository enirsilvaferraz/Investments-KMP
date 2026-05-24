package com.eferraz.usecases.repositories

public interface B3ImportDataSource {
    public suspend fun importAndLog(): Result<Unit>
}
