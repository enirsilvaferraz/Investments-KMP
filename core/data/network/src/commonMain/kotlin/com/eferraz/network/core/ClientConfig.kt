package com.eferraz.network.core

import io.ktor.client.HttpClient

internal interface ClientConfig {
    val client: HttpClient
}