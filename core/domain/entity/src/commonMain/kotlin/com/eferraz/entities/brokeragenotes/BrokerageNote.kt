package com.eferraz.entities.brokeragenotes

public data class BrokerageNote(
    val totalVolumeTraded: Double,
    val apportionableFees: Double,
    /**
     * Soma de `impostos_retidos` (IRRF operações + IRRF day trade).
     *
     * Não entra no rateio proporcional; reduz o crédito líquido ao cliente no fechamento contábil.
     */
    val withheldTaxes: Double,
    /**
     * Valor líquido da nota (`valor_liquido_nota`), pass-through da fonte.
     *
     * Sinal contábil da nota de corretagem: negativo = débito do cliente (investidor paga);
     * positivo = crédito ao cliente (investidor recebe).
     */
    val netValue: Double,
    val assets: List<BrokerageNoteAsset>,
)
