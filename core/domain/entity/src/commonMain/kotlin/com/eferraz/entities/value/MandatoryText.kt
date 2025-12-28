package com.eferraz.entities.value

import kotlin.jvm.JvmInline

@JvmInline
public value class MandatoryText(private val value: String) {

    init {
        if (value.isEmpty()) throw IllegalArgumentException("MandatoryText cannot be empty")
    }

    public fun get(): String = value
}