package com.eferraz.entities.brokeragenotes

/**
 * Trade direction on a SINACOR brokerage note.
 */
public enum class TradeType {
    BUY,
    SELL,
    ;

    public companion object {

        /**
         * Parses SINACOR `movimentacao` literals from brokerage note sources.
         */
        public fun fromMovement(raw: String): TradeType =
            when (raw) {
                "COMPRA" -> BUY
                "VENDA" -> SELL
                else -> throw IllegalArgumentException("Unknown movement type: $raw")
            }
    }
}
