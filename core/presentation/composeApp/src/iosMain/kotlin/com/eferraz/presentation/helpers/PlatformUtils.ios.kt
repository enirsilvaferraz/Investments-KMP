package com.eferraz.presentation.helpers

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterPercentStyle
import platform.Foundation.currentLocale

public actual fun Double.currencyFormat(): String =
    NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
    }.stringFromNumber(NSNumber(this)) ?: ""

public actual fun String.currencyToDouble(): Double? =
    NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
    }.numberFromString(this)?.doubleValue()

public actual fun Double.toPercentage(): String =
    NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterPercentStyle
        locale = NSLocale.currentLocale
        maximumFractionDigits = 2u
    }.stringFromNumber(NSNumber(double = this))!!

public actual fun String.fromPercentage(): Double? =
    NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterPercentStyle
        locale = NSLocale.currentLocale
        maximumFractionDigits = 2u
    }.numberFromString(this)?.doubleValue()
