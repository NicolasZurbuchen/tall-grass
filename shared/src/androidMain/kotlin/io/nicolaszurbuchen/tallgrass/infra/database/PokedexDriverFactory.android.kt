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
        // The driver resolves a bare database name against this directory, so the copy has to land here.
        val database = context.getDatabasePath(POKEDEX_DATABASE_NAME)
        if (!database.exists()) {
            database.parentFile?.mkdirs()
            copyFromResources(database)
        }

        return AndroidSqliteDriver(
            schema = PokedexDatabase.Schema,
            context = context,
            name = POKEDEX_DATABASE_NAME,
        )
    }

    // Renamed into place rather than written directly: a process killed mid-write would leave a
    // truncated database that exists, so the next launch would skip the copy and open the wreckage.
    private fun copyFromResources(target: File) {
        val temporary = File(target.parentFile, "$POKEDEX_DATABASE_NAME.tmp")
        context.assets.open(POKEDEX_RESOURCE_PATH).use { input ->
            temporary.outputStream().use { output -> input.copyTo(output) }
        }
        check(temporary.renameTo(target)) { "Could not move the bundled dataset into place." }
    }
}
