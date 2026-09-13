package io.nicolaszurbuchen.tallgrass.infra.network

import io.ktor.client.engine.HttpClientEngine

expect fun httpClientEngine(): HttpClientEngine
