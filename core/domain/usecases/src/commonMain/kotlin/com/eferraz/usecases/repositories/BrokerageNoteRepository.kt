package com.eferraz.usecases.repositories

import com.eferraz.entities.brokeragenotes.BrokerageNote

public interface BrokerageNoteRepository {
    public suspend fun loadNote(): Result<BrokerageNote>
}