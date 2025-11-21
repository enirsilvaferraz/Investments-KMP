package com.eferraz.presentation.helpers

public expect fun Double.currencyFormat(): String

public expect fun String.currencyToDouble(): Double?

public expect fun Double.toPercentage(): String

public expect fun String.fromPercentage(): Double?