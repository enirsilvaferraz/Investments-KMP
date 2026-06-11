package com.eferraz.entities.brokeragenotes

import kotlin.math.round

/**
 * Rateio consolidado de taxas de uma nota de corretagem inteira.
 *
 * @property allocations Um [AssetFeeAllocation] por [NoteAsset], na mesma ordem da nota de entrada.
 */
public data class NoteFeeAllocation internal constructor(
    public val allocations: List<AssetFeeAllocation>,
) {

    public companion object {

        /**
         * Distribui as taxas da nota proporcionalmente ao volume financeiro bruto, com aritmética inteira em centavos.
         *
         * @param note Nota de corretagem com pelo menos um ativo válido.
         * @return Taxas alocadas e valores líquidos por ativo.
         * @throws IllegalArgumentException se a lista de ativos estiver vazia, quantidade/preço forem inválidos ou o volume total for zero.
         * @throws IllegalStateException se a equação de fechamento contábil falhar.
         */
        public fun calculate(note: BrokerageNote): NoteFeeAllocation {

            // --- Validação de entrada (FR-010, FR-012) ---
            // Fica em calculate() para manter as data classes com construtores simples, sem efeitos colaterais.

            if (note.assets.isEmpty())
                throw IllegalArgumentException("assets must not be empty")

            note.assets.firstOrNull { it.quantity <= 0 || it.unitPrice <= 0 }?.let {
                throw IllegalArgumentException("asset ${it.ticker}: quantity and unitPrice must be > 0")
            }

            // --- Rateio proporcional em centavos inteiros (FR-001–FR-005) ---
            // Aritmética em Double com dinheiro (ex.: 1,51 + 1,51 + 1,52) pode divergir do total da nota;
            // centavos em Long mantêm o rateio determinístico e exato em duas casas decimais.

            val grossValueCents: List<Long> = note.assets.map { it.grossValueCents }
            val totalVolumeCents = grossValueCents.sum()

            if (totalVolumeCents <= 0L)
                throw IllegalArgumentException("total volume must be > 0")

            val totalFeesCents = note.fees.total.toCents()

            // Quota proporcional de cada ativo (em centavos):
            //   quota = volumeDoAtivo × taxasTotais ÷ volumeTotal
            //
            // A conta raramente fecha em centavo inteiro. Exemplo com 3 ativos de R$ 1.000 e taxas de R$ 4,54:
            //   volumeTotal = 300.000 centavos, taxasTotais = 454 centavos
            //   quota de cada um = 100.000 × 454 ÷ 300.000 = 151,33… centavos
            //
            // "Arredondar para baixo" = descartar a fração e ficar só com a parte inteira (151, não 152).
            // A divisão inteira Long (gross * totalFeesCents / totalVolumeCents) faz isso sozinha em valores positivos.

            val feeCents = grossValueCents
                .map { gross -> gross * totalFeesCents / totalVolumeCents }
                .toMutableList()

            // --- Ajuste do centavo residual (FR-006) ---
            // Depois de arredondar cada quota para baixo, a soma das quotas costuma ficar MENOR que o total de taxas.
            // No exemplo acima: 151 + 151 + 151 = 453 centavos, mas a nota cobra 454 — falta 1 centavo.
            //
            // remainder = totalFeesCents − Σ feeCents captura essa diferença (de 0 a N−1 centavos).
            // Esse centavo (ou centavos) é somado a um único ativo para que Σ allocatedFee == note.fees.total.
            // Critério: ativo com maior volume; em empate, o primeiro da lista (ex.: AJFI11 recebe 152 → R$ 1,52).

            val remainder = totalFeesCents - feeCents.sum()
            val maxGross = grossValueCents.max()
            val idxMaxVolume = grossValueCents.indexOfFirst { it == maxGross }
            feeCents[idxMaxVolume] += remainder

            // --- Valor líquido por ativo (FR-007, FR-008) ---
            // Convenção SINACOR: taxas aumentam o débito em BUY (cliente paga mais) e reduzem
            // o crédito em SELL (cliente recebe menos). O sinal é aplicado aqui, não em feeCents.

            val allocations = note.assets.mapIndexed { index, asset ->
                AssetFeeAllocation(
                    ticker = asset.ticker,
                    tradeType = asset.tradeType,
                    grossValue = grossValueCents[index] / 100.0,
                    allocatedFee = feeCents[index] / 100.0,
                )
            }

            // --- Fechamento contábil (FR-009) ---
            // Concilia os valores líquidos alocados com o cabeçalho da nota:
            //   Σ(BUY netValue) − Σ(SELL netValue) == note.netValue
            // Comparação em centavos inteiros evita uma segunda rodada de erro de ponto flutuante.

            var buysTotalCents = 0L
            var sellsTotalCents = 0L
            note.assets.zip(allocations).forEach { (asset, allocation) ->
                val netCents = round(allocation.netValue * 100.0).toLong()
                when (asset.tradeType) {
                    TradeType.BUY -> buysTotalCents += netCents
                    TradeType.SELL -> sellsTotalCents += netCents
                }
            }

            val noteNetValueCents = round(note.netValue * 100.0).toLong()
            val differenceCents = buysTotalCents - sellsTotalCents - noteNetValueCents
            if (differenceCents != 0L) {
                val computedNetValue = (buysTotalCents - sellsTotalCents) / 100.0
                throw IllegalStateException(
                    "accounting closure failed: expected ${note.netValue}, got $computedNetValue",
                )
            }

            return NoteFeeAllocation(allocations = allocations)
        }

        private fun Double.toCents(): Long = round(this * 100.0).toLong()
    }
}
