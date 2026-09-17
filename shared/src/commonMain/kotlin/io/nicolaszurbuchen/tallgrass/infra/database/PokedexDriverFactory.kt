package io.nicolaszurbuchen.tallgrass.infra.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Opens the generated dataset, copying it out of the app's bundled resources on first run.
 *
 * The copy is unavoidable: SQLite opens a file, and a bundled resource is an entry inside the
 * package. It costs roughly twice the database's size in storage while it runs.
 */
expect class PokedexDriverFactory {
    fun createDriver(): SqlDriver
}

internal const val POKEDEX_DATABASE_NAME = "pokedex.db"

/**
 * Where Compose Multiplatform puts `commonMain/composeResources/files` once packaged.
 *
 * Addressed by path rather than through `Res.readBytes`, which is `suspend` and would make opening a
 * database a coroutine. The package segment is derived from the shared module's namespace at build
 * time, so renaming that module moves this file.
 */
internal const val POKEDEX_RESOURCE_PATH =
    "composeResources/tallgrass.shared.generated.resources/files/$POKEDEX_DATABASE_NAME"
