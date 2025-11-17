package com.eferraz.pokedex.utils

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

public actual fun Double.currencyFormat(): String {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterCurrencyStyle
    return formatter.stringFromNumber(NSNumber(this)) ?: ""
}

public actual fun String.currencyToDouble(): Double? {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterCurrencyStyle
    return formatter.numberFromString(this)?.doubleValue()
}

