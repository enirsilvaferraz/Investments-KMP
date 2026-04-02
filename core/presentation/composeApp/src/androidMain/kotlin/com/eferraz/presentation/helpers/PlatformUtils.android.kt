package com.eferraz.presentation.helpers

import java.text.NumberFormat
import java.util.Locale

public actual fun Double.currencyFormat(): String =
    NumberFormat.getCurrencyInstance(Locale.getDefault()).format(this)

public actual fun String.currencyToDouble(): Double? =
    NumberFormat.getCurrencyInstance(Locale.getDefault()).parse(this)?.toDouble()

public actual fun Double.toPercentage(): String =
    NumberFormat.getPercentInstance().apply {
        maximumFractionDigits = 2
    }.format(this / 100)

public actual fun String.fromPercentage(): Double? =
    NumberFormat.getPercentInstance().apply {
        maximumFractionDigits = 2
    }.parse(this)?.toDouble()
