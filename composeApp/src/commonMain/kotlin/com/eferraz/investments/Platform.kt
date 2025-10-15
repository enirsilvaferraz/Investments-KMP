package com.eferraz.investments

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform