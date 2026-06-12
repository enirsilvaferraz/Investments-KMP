package com.eferraz.entities.brokeragenotes

import com.eferraz.entities.transactions.AssetTransaction
import com.eferraz.entities.transactions.TransactionType
import kotlin.math.round

/**
 * Resultado do rateio de taxas de uma nota de corretagem.
 *
 * Contém as linhas da nota com [AssetTransaction.allocatedFee] já preenchido via [copy].
 */
public data class NoteFeeAllocation private constructor(
    public val assets: List<BrokerageNoteAsset>,
) {

    public companion object {

        /**
         * Calcula o rateio proporcional das taxas rateáveis entre os ativos da nota.
         *
         * Pipeline em três etapas: validação pré-cálculo, distribuição em centavos e fechamento contábil.
         *
         * @param note Nota de corretagem com pelo menos um ativo válido.
         * @return Linhas da nota com taxa alocada em cada [AssetTransaction].
         * @throws IllegalArgumentException se a nota falhar na Etapa 1.
         * @throws IllegalStateException se o fechamento contábil da Etapa 3 falhar.
         */
        public fun calculate(note: BrokerageNote): NoteFeeAllocation {

            // --- Etapa 2: rateio proporcional em centavos inteiros (FR-011 a FR-016) ---
            val totalVolumeCents = note.totalVolumeTraded.toCents()
            val somaFeesCents = note.apportionableFees.toCents()
            val noteNetValue = note.netValue

            val assetsWithFees = allocateFees(
                assets = note.assets,
                somaFeesCents = somaFeesCents,
                totalVolumeCents = totalVolumeCents,
            )

            // --- Etapa 3: validação pós-cálculo (FR-018, FR-019) ---
            validateFeeDistribution(
                allocatedFees = assetsWithFees.map { it.transaction.allocatedFee },
                somaFeesCents = somaFeesCents,
            )

            validateAccountingClosure(
                assets = assetsWithFees.map { it.transaction },
                noteNetValue = noteNetValue,
                withheldTaxes = note.withheldTaxes,
            )

            return NoteFeeAllocation(assets = assetsWithFees)
        }

        /**
         * Distribui [somaFeesCents] proporcionalmente ao volume bruto de cada ativo.
         *
         * Todos exceto o de maior volume recebem quota com ROUND_HALF_UP; o de maior volume absorve
         * o resíduo para que a soma das taxas alocadas feche exatamente em centavos (FR-013).
         * Em empate de volume, prevalece o primeiro na lista.
         */
        private fun allocateFees(
            assets: List<BrokerageNoteAsset>,
            somaFeesCents: Long,
            totalVolumeCents: Long,
        ): List<BrokerageNoteAsset> {
            val grossValueCents = assets.map { it.transaction.grossValue.toCents() }
            val feeCents = LongArray(grossValueCents.size)
            val residueIndex = grossValueCents.indices.maxBy { grossValueCents[it] }

            for (index in grossValueCents.indices) {
                if (index == residueIndex) continue
                // Quota proporcional: volumeDoAtivo × Soma_Taxas ÷ volumeTotal.
                // A divisão inteira com "+ volumeTotal/2" implementa ROUND_HALF_UP em centavos (FR-012).
                feeCents[index] = (grossValueCents[index] * somaFeesCents + totalVolumeCents / 2) / totalVolumeCents
            }

            // O ativo de maior volume recebe o que faltar para Σ taxas == Soma_Taxas (FR-013).
            feeCents[residueIndex] = somaFeesCents - feeCents.sum()

            return assets.mapIndexed { index, asset ->
                asset.copy(transaction = asset.transaction.copy(allocatedFee = feeCents[index] / 100.0))
            }
        }

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
         * Saldo de operações: Σ(valor líquido VENDA) − Σ(valor líquido COMPRA).
         * Em notas com crédito ao cliente (saldo positivo), [withheldTaxes] reduz o valor
         * líquido final — alinhado a `valor_liquido_nota` da corretora.
         *
         * Sinal da nota: negativo = débito do cliente; positivo = crédito.
         */
        private fun validateAccountingClosure(
            assets: List<AssetTransaction>,
            noteNetValue: Double,
            withheldTaxes: Double,
        ) {
            var buysTotalCents = 0L
            var sellsTotalCents = 0L

            assets.forEach { asset ->
                val netCents = asset.netValue.toCents()
                when (asset.type) {
                    TransactionType.PURCHASE -> buysTotalCents += netCents
                    TransactionType.SALE -> sellsTotalCents += netCents
                }
            }

            val noteNetValueCents = noteNetValue.toCents()
            val tradeBalanceCents = sellsTotalCents - buysTotalCents
            val calculatedTotalCents = tradeBalanceCents - withheldTaxes.toCents()

            if (calculatedTotalCents != noteNetValueCents) {
                val computedNetValue = calculatedTotalCents / 100.0
                throw IllegalStateException(
                    "accounting closure failed: expected $noteNetValue, got $computedNetValue",
                )
            }
        }

        private fun Double.toCents(): Long = round(this * 100.0).toLong()
    }
}
