package com.eferraz.pokedex.utils

import java.text.NumberFormat
import java.util.Locale

public actual fun Double.currencyFormat(): String {
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(this)
}
