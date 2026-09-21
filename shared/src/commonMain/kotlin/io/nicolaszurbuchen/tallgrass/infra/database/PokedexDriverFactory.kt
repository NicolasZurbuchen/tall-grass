package io.nicolaszurbuchen.tallgrass.infra.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Opens the generated dataset, copying it out of the app's bundled resources when the copy on the
 * device is missing or older than the one in the package.
 *
 * The copy is unavoidable: SQLite opens a file, and a bundled resource is an entry inside the
 * package. It costs roughly twice the database's size in storage while it runs.
 *
 * **The copy is replaced rather than migrated**, which is the whole design of this database -- see
 * `DECISIONS.md`. What makes that work on a device is [POKEDEX_STAMP_NAME]: a copy made on first run
 * would otherwise survive every future dataset, because the file it was copied from is an asset the
 * app never looks at again.
 */
expect class PokedexDriverFactory {
    fun createDriver(): SqlDriver
}

internal const val POKEDEX_DATABASE_NAME = "pokedex.db"

/**
 * Which dataset the copy on the device came from: the schema version and the pinned upstream SHA.
 *
 * **Not derivable from the database itself.** SQLite's `user_version` holds the schema version and
 * nothing else, so a SHA bump that rewrites every row leaves it untouched — which is exactly the
 * change a reader would notice and the file could not report.
 */
internal const val POKEDEX_STAMP_NAME = "pokedex.stamp"

/**
 * Where Compose Multiplatform puts `commonMain/composeResources/files` once packaged.
 *
 * Addressed by path rather than through `Res.readBytes`, which is `suspend` and would make opening a
 * database a coroutine. The package segment is derived from the shared module's namespace at build
 * time, so renaming that module moves these files.
 */
internal const val POKEDEX_RESOURCE_DIR = "composeResources/tallgrass.shared.generated.resources/files"

internal const val POKEDEX_RESOURCE_PATH = "$POKEDEX_RESOURCE_DIR/$POKEDEX_DATABASE_NAME"

internal const val POKEDEX_STAMP_RESOURCE_PATH = "$POKEDEX_RESOURCE_DIR/$POKEDEX_STAMP_NAME"
