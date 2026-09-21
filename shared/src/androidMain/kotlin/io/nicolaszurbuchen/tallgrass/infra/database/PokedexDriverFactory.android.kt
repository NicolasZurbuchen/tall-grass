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
        val stamp = File(database.parentFile, POKEDEX_STAMP_NAME)
        val bundled = context.assets.open(POKEDEX_STAMP_RESOURCE_PATH).use { it.readBytes().decodeToString() }

        // **Compared rather than merely checked for existence.** An update ships a new dataset as an
        // asset, and an asset is not a file the device has: without this, the copy made on the very
        // first run is the dex the user keeps for as long as the app is installed.
        if (!database.exists() || stampOf(stamp) != bundled) {
            database.parentFile?.mkdirs()
            copyFromResources(database)
            stamp.writeText(bundled)
        }

        return AndroidSqliteDriver(
            schema = PokedexDatabase.Schema,
            context = context,
            name = POKEDEX_DATABASE_NAME,
        )
    }

    // An unreadable stamp is treated as a different one, which re-copies. That is the safe direction:
    // the cost is one copy of a file the app is about to read anyway.
    private fun stampOf(file: File): String? = runCatching { file.readText() }.getOrNull()

    // Renamed into place rather than written directly: a process killed mid-write would leave a
    // truncated database that exists, so the next launch would skip the copy and open the wreckage.
    private fun copyFromResources(target: File) {
        val temporary = File(target.parentFile, "$POKEDEX_DATABASE_NAME.tmp")
        context.assets.open(POKEDEX_RESOURCE_PATH).use { input ->
            temporary.outputStream().use { output -> input.copyTo(output) }
        }

        // The old copy has to go first: rename does not replace on every filesystem, and a failed
        // replace here leaves the stamp unwritten, so the next launch tries again.
        target.delete()
        check(temporary.renameTo(target)) { "Could not move the bundled dataset into place." }
    }
}
