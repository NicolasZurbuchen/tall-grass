package io.nicolaszurbuchen.tallgrass.infra.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
