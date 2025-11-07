package com.eferraz.network

import io.ktor.client.engine.HttpClientEngineFactory

internal expect fun httpEngine(): HttpClientEngineFactory<*>