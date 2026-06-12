package com.eferraz.repositories

import com.eferraz.entities.brokeragenotes.BrokerageNote
import com.eferraz.filestore.brokeragenote.BrokerageNoteJsonDataSource
import com.eferraz.usecases.repositories.BrokerageNoteRepository
import org.koin.core.annotation.Factory

@Factory
internal class BrokerageNoteRepositoryImpl(
    private val dataSource: BrokerageNoteJsonDataSource
) : BrokerageNoteRepository {

    override suspend fun loadNote(): Result<BrokerageNote> {
        return dataSource.loadNote()
    }
}