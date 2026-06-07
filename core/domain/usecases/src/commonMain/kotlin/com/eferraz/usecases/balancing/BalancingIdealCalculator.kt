package com.eferraz.usecases.balancing

internal object BalancingIdealCalculator {

    fun computeIdeal(weight: TargetWeight, actual: Double, referenceBase: Double): Double = when (weight) {
        is TargetWeight.Dynamic -> actual
        is TargetWeight.Fixed -> referenceBase * weight.percent / 100.0
        TargetWeight.Zero -> 0.0
    }

    fun configuredWeightDisplay(weight: TargetWeight): String = when (weight) {
        is TargetWeight.Fixed -> BalancingFormatters.formatPercent(weight.percent)
        TargetWeight.Zero -> "0,00%"
        TargetWeight.Dynamic -> "dinâmico"
    }

    fun configuredWeightPercent(weight: TargetWeight): Double? = when (weight) {
        is TargetWeight.Fixed -> weight.percent
        TargetWeight.Zero -> 0.0
        TargetWeight.Dynamic -> null
    }
}
