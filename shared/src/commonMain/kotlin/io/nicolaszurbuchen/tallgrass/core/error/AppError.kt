package io.nicolaszurbuchen.tallgrass.core.error

sealed interface AppError {
    sealed interface Network : AppError {
        data object Unavailable : Network

        data object Timeout : Network

        data class Http(
            val code: Int,
            val serverMessage: String? = null,
        ) : Network
    }

    sealed interface Database : AppError {
        data class QueryFailed(
            val cause: Throwable,
        ) : Database

        data class InsertFailed(
            val cause: Throwable,
        ) : Database
    }

    data class Unexpected(
        val cause: Throwable,
    ) : AppError
}
