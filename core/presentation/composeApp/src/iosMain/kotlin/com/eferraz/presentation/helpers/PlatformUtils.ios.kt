package com.eferraz.presentation.helpers

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterPercentStyle
import platform.Foundation.currentLocale

public actual fun Double.currencyFormat(): String {
    return NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
    }.stringFromNumber(NSNumber(this)) ?: ""
}

public actual fun String.currencyToDouble(): Double? {
    return NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
    }.numberFromString(this)?.doubleValue()
}

public actual fun Double.toPercentage(): String {
    return NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterPercentStyle
        locale = NSLocale.currentLocale
        maximumFractionDigits = 2u
    }.stringFromNumber(NSNumber(double = this))!!
}

public actual fun String.fromPercentage(): Double? {
    return NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterPercentStyle
        locale = NSLocale.currentLocale
        maximumFractionDigits = 2u
    }.numberFromString(this)?.doubleValue()
}