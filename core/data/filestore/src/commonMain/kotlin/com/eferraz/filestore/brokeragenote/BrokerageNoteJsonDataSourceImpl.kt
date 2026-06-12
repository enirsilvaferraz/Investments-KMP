package com.eferraz.filestore.brokeragenote

import com.eferraz.entities.brokeragenotes.BrokerageNote
import com.eferraz.filestore.brokeragenote.dto.BrokerageNoteDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Factory

@Factory(binds = [BrokerageNoteJsonDataSource::class])
internal class BrokerageNoteJsonDataSourceImpl : BrokerageNoteJsonDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun loadNote(): Result<BrokerageNote> = withContext(Dispatchers.Default) {

        runCatching {

            val document = json.decodeFromString<BrokerageNoteDocument>(Nota2JsonFixture.raw)

            BrokerageNoteValidator.validate(document)

            BrokerageNoteV2Parser.parse(document)
        }
    }
}
