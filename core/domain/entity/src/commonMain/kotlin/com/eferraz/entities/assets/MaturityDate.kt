package com.eferraz.entities.assets

import kotlinx.datetime.LocalDate
import kotlin.jvm.JvmInline

@JvmInline
public value class MaturityDate(private val value: String) {

    init {
        value.toDate()
    }

    public fun get(): LocalDate =
        value.toDate()

    private fun String.toDate() =
        LocalDate.Companion.Format { year(); monthNumber(); day() }.parse(this)
}