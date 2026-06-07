package com.eferraz.usecases.balancing

internal object BalancingFormatters {

    fun formatPercent(value: Double): String {
        val rounded = kotlin.math.round(value * 100.0) / 100.0
        val parts = rounded.toString().split('.')
        val integer = parts[0]
        val fraction = parts.getOrElse(1) { "0" }.padEnd(2, '0').take(2)
        return "$integer,$fraction%"
    }

    fun formatMoney(value: Double): String = formatSignedMoney(value, positiveSign = " ")

    fun formatSignedMoney(value: Double, positiveSign: String = "+"): String {
        val sign = when {
            value < 0 -> "-"
            value > 0 -> positiveSign
            else -> " "
        }
        val absolute = kotlin.math.abs(value)
        val cents = kotlin.math.round(absolute * 100.0).toLong()
        val whole = cents / 100
        val fraction = (cents % 100).toString().padStart(2, '0')
        val wholeFormatted = whole.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
        return "$sign R$ $wholeFormatted,$fraction"
    }
}
