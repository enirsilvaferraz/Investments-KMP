package com.eferraz.filestore.brokeragenote

import com.eferraz.entities.brokeragenotes.BrokerageNote

public interface BrokerageNoteJsonDataSource {

    public suspend fun loadNote(): Result<BrokerageNote>
}
