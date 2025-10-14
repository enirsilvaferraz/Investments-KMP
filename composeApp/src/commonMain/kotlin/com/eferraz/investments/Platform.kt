package com.eferraz.investments

internal interface Platform {
    val name: String
}

internal expect fun getPlatform(): Platform