package io.nicolaszurbuchen.tallgrass.infra.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import java.io.File

actual class PokedexDriverFactory(
    private val context: Context,
) {
    actual fun createDriver(): SqlDriver {
        // The standard database directory rather than filesDir, because that is where the driver
        // resolves a bare database name -- putting the file anywhere else would mean the copy
        // landing in one place and the open looking in another.
        val database = context.getDatabasePath(POKEDEX_DATABASE_NAME)
        if (!database.exists()) {
            database.parentFile?.mkdirs()
            copyFromAssets(database)
        }

        // The schema is handed over for its version number only. The file is already populated by
        // the time the driver opens it, so nothing is created and no migration runs.
        return AndroidSqliteDriver(
            schema = PokedexDatabase.Schema,
            context = context,
            name = POKEDEX_DATABASE_NAME,
        )
    }

    /**
     * Writes to a temporary file and renames it into place.
     *
     * A process killed part-way through a direct write would leave a truncated database that
     * nonetheless exists, so the next launch would skip the copy and open the wreckage. Renaming is
     * atomic, so the file is either absent or whole.
     */
    private fun copyFromAssets(target: File) {
        val temporary = File(target.parentFile, "$POKEDEX_DATABASE_NAME.tmp")
        context.assets.open(POKEDEX_DATABASE_NAME).use { input ->
            temporary.outputStream().use { output -> input.copyTo(output) }
        }
        check(temporary.renameTo(target)) { "Could not move the bundled dataset into place." }
    }
}
