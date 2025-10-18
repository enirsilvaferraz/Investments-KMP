package com.eferraz.pokedex.utils

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

public actual fun Double.currencyFormat(): String {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterCurrencyStyle
    return formatter.stringFromNumber(NSNumber(this)) ?: ""
}
