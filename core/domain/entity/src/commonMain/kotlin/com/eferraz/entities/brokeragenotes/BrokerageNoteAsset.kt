package com.eferraz.entities.brokeragenotes

import com.eferraz.entities.transactions.AssetTransaction

public data class BrokerageNoteAsset(
    public val ticker: String,
    public val transaction: AssetTransaction,
)
