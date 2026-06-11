package com.eferraz.entities.brokeragenotes

import kotlin.math.round

/**
 * Resultado do rateio de taxas de uma nota de corretagem.
 *
 * Mapeia cada [NoteAsset] ao valor líquido final (`netValue`) já com a taxa proporcional aplicada.
 * A ordem de iteração segue a lista de ativos da nota de entrada.
 */
public data class NoteFeeAllocation private constructor(
    private val netValuesByAsset: Map<NoteAsset, Double>,
) : Map<NoteAsset, Double> by netValuesByAsset {

    public companion object {

        /**
         * Calcula o rateio proporcional das taxas rateáveis entre os ativos da nota.
         *
         * Pipeline em três etapas: validação pré-cálculo, distribuição em centavos e fechamento contábil.
         *
         * @param note Nota de corretagem com pelo menos um ativo válido.
         * @return Mapa ativo → valor líquido final.
         * @throws IllegalArgumentException se a nota falhar na Etapa 1.
         * @throws IllegalStateException se o fechamento contábil da Etapa 3 falhar.
         */
        public fun calculate(note: BrokerageNote): NoteFeeAllocation {

            // --- Etapa 1: validação dos dados brutos (FR-006 a FR-009, FR-021, FR-022) ---
            // Garante que volume, subtotais e campos por ativo estão consistentes antes de qualquer conta.
            BrokerageNoteValidator.validate(note)

            // --- Etapa 2: rateio proporcional em centavos inteiros (FR-011 a FR-016) ---
            val feeCents = allocateFeeCents(
                grossValueCents = note.assets.map { it.grossValue.toCents() },
                somaFeesCents = note.financialSummary.apportionableFees.total.toCents(),
                totalVolumeCents = note.financialSummary.totalVolumeTraded.toCents(),
            )

            val netValues = computeNetValues(
                assets = note.assets,
                allocatedFees = feeCents,
            )

            // --- Etapa 3: validação pós-cálculo (FR-018, FR-019) ---
            validateFeeDistribution(
                allocatedFees = feeCents,
                somaFeesCents = note.financialSummary.apportionableFees.total.toCents(),
            )

            validateAccountingClosure(
                netValues = netValues,
                noteNetValue = note.metadata.netValue,
            )

            return NoteFeeAllocation(netValuesByAsset = netValues)
        }

        /**
         * Distribui [somaFeesCents] proporcionalmente ao volume bruto de cada ativo.
         *
         * Os N−1 primeiros ativos recebem quota com ROUND_HALF_UP; o último absorve o resíduo
         * para que a soma das taxas alocadas feche exatamente em centavos (FR-013).
         */
        private fun allocateFeeCents(
            grossValueCents: List<Long>,
            somaFeesCents: Long,
            totalVolumeCents: Long,
        ): List<Double> {

            val feeCents = LongArray(grossValueCents.size)
            val lastIndex = grossValueCents.lastIndex

            for (index in 0 until lastIndex) {
                // Quota proporcional: volumeDoAtivo × Soma_Taxas ÷ volumeTotal.
                // A divisão inteira com "+ volumeTotal/2" implementa ROUND_HALF_UP em centavos (FR-012).
                feeCents[index] = (grossValueCents[index] * somaFeesCents + totalVolumeCents / 2) / totalVolumeCents
            }

            // O último ativo recebe o que faltar para Σ taxas == Soma_Taxas (FR-013).
            feeCents[lastIndex] = somaFeesCents - feeCents.copyOfRange(0, lastIndex).sum()

            return feeCents.map { it / 100.0 }
        }

        /**
         * Aplica a taxa alocada ao valor bruto conforme a direção da operação (FR-014, FR-015).
         *
         * COMPRA: taxa aumenta o custo → netValue = grossValue + allocatedFee.
         * VENDA: taxa reduz o recebimento → netValue = grossValue − allocatedFee.
         */
        private fun computeNetValues(
            assets: List<NoteAsset>,
            allocatedFees: List<Double>,
        ): Map<NoteAsset, Double> =
            assets.mapIndexed { index, asset ->
                val allocatedFee = allocatedFees[index]
                asset to when (asset.tradeType) {
                    TradeType.BUY -> asset.grossValue + allocatedFee
                    TradeType.SELL -> asset.grossValue - allocatedFee
                }
            }.toMap()

        /**
         * Regra 3.1 (FR-018): a soma das taxas alocadas deve bater com Soma_Taxas em centavos.
         */
        private fun validateFeeDistribution(
            allocatedFees: List<Double>,
            somaFeesCents: Long,
        ) {
            val allocatedSumCents = allocatedFees.sumOf { it.toCents() }
            if (allocatedSumCents != somaFeesCents) {
                throw IllegalStateException(
                    "fee distribution mismatch: allocated ${allocatedSumCents / 100.0} ≠ somaFees ${somaFeesCents / 100.0}",
                )
            }
        }

        /**
         * Regra 3.2 (FR-019): fechamento contábil da nota em centavos inteiros.
         *
         * Σ(valor líquido COMPRA) − Σ(valor líquido VENDA) deve igualar [noteNetValue]
         * (sinal contábil: positivo = débito do cliente, negativo = crédito).
         */
        private fun validateAccountingClosure(
            netValues: Map<NoteAsset, Double>,
            noteNetValue: Double,
        ) {
            var buysTotalCents = 0L
            var sellsTotalCents = 0L

            netValues.forEach { (asset, netValue) ->
                val netCents = netValue.toCents()
                when (asset.tradeType) {
                    TradeType.BUY -> buysTotalCents += netCents
                    TradeType.SELL -> sellsTotalCents += netCents
                }
            }

            val noteNetValueCents = noteNetValue.toCents()
            if (buysTotalCents - sellsTotalCents != noteNetValueCents) {
                val computedNetValue = (buysTotalCents - sellsTotalCents) / 100.0
                throw IllegalStateException(
                    "accounting closure failed: expected $noteNetValue, got $computedNetValue",
                )
            }
        }

        private fun Double.toCents(): Long = round(this * 100.0).toLong()
    }
}

/**
 * Atalho de chamada a partir da nota; delega para [NoteFeeAllocation.calculate].
 */
public fun BrokerageNote.calculateFeeAllocation(): NoteFeeAllocation =
    NoteFeeAllocation.calculate(this)
