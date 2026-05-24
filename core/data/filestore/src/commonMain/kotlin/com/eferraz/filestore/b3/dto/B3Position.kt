package com.eferraz.filestore.b3.dto

internal sealed interface B3Position {
    fun isBlankRow(): Boolean
}
