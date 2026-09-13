package io.nicolaszurbuchen.tallgrass.core.error

class AppException(
    val error: AppError,
) : Exception("App error: $error")
