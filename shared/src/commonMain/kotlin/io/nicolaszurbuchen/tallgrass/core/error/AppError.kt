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
        /**
         * A row the app asked for by key is not in the bundled dataset. Reachable when a saved back
         * stack outlives the dataset it was built against -- an app update ships a new pokedex.db,
         * and a slug that was on a card yesterday can be gone.
         */
        data object NotFound : Database

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
