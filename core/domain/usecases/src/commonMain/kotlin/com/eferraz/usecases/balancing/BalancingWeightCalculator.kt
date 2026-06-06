package com.eferraz.usecases.balancing

internal object BalancingWeightCalculator {

    internal data class ConfiguredWeight(
        val display: String,
        val percent: Double?,
    )

    internal data class PortfolioTotalContext(
        val totalPortfolioValue: Double,
        val balanceableBase: Double,
        val hasDynamicWeight: Boolean,
    )

    internal data class NestedContext(
        val groupTotal: Double,
        val totalPortfolioValue: Double,
    )

    internal data class ComputedWeights(
        val idealValue: Double,
        val normalizedPercent: Double,
    )

    fun configuredWeight(weight: TargetWeight): ConfiguredWeight = when (weight) {

        is TargetWeight.Fixed -> ConfiguredWeight(
            display = BalancingFormatters.formatPercent(weight.percent),
            percent = weight.percent,
        )

        TargetWeight.Zero -> ConfiguredWeight(
            display = "0,00%",
            percent = 0.0,
        )

        TargetWeight.Dynamic -> ConfiguredWeight(
            display = "dinâmico",
            percent = null,
        )
    }

    fun computePortfolioTotalWeights(
        targetWeight: TargetWeight,
        actualValue: Double,
        configuredPercent: Double?,
        context: PortfolioTotalContext,
    ): ComputedWeights {
        val normalizedPercent = when {
            context.totalPortfolioValue == 0.0 -> 0.0
            targetWeight is TargetWeight.Dynamic ->
                actualValue / context.totalPortfolioValue * 100.0

            targetWeight is TargetWeight.Fixed ->
                context.balanceableBase * configuredPercent!! / context.totalPortfolioValue

            else -> 0.0
        }
        val idealValue = when {
            context.totalPortfolioValue == 0.0 -> 0.0
            targetWeight is TargetWeight.Dynamic -> actualValue
            targetWeight is TargetWeight.Zero -> 0.0
            context.hasDynamicWeight -> context.totalPortfolioValue * normalizedPercent / 100.0
            else -> context.totalPortfolioValue * configuredPercent!! / 100.0
        }
        return ComputedWeights(
            idealValue = idealValue,
            normalizedPercent = normalizedPercent,
        )
    }

    fun computeNestedWeights(
        targetWeight: TargetWeight,
        context: NestedContext,
    ): ComputedWeights {
        val idealValue = when {
            context.totalPortfolioValue == 0.0 -> 0.0
            targetWeight is TargetWeight.Fixed -> context.groupTotal * targetWeight.percent / 100.0
            targetWeight is TargetWeight.Zero -> 0.0
            else -> error("Dynamic weight is not supported in nested groups")
        }
        val normalizedPercent = if (context.totalPortfolioValue > 0.0) {
            idealValue / context.totalPortfolioValue * 100.0
        } else {
            0.0
        }
        return ComputedWeights(
            idealValue = idealValue,
            normalizedPercent = normalizedPercent,
        )
    }

    fun actualWeightPercent(actualValue: Double, groupTotal: Double): Double =
        if (groupTotal > 0.0) actualValue / groupTotal * 100.0 else 0.0
}
