package io.nicolaszurbuchen.tallgrass.infra.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Opens the generated dataset, copying it out of the app's assets on first run.
 *
 * The copy is unavoidable rather than a caching choice: SQLite opens a file, and a bundled asset is
 * a compressed entry inside the package rather than a file on disk. It costs roughly twice the
 * database's size in storage for the duration of the copy.
 *
 * Separate from [DatabaseDriverFactory] because the two databases have opposite lifecycles. This one
 * is read-only, carries no migrations, and is replaced wholesale when a build ships a newer dataset;
 * that one is written by the user and migrated in place. Sharing a factory would eventually mean
 * sharing a migration path, which is the mistake the split exists to prevent.
 */
expect class PokedexDriverFactory {
    fun createDriver(): SqlDriver
}

/** The bundled asset, and the name it takes once copied out. */
internal const val POKEDEX_DATABASE_NAME = "pokedex.db"
