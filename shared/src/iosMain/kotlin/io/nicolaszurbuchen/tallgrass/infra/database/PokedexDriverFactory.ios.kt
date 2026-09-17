package io.nicolaszurbuchen.tallgrass.infra.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * Compiles in CI and has never run on a device, so the bundle path below is the documented layout
 * rather than an observed one.
 */
actual class PokedexDriverFactory {
    // copyItemAtPath takes an NSError out-parameter, which is a C pointer even when it is null.
    @OptIn(ExperimentalForeignApi::class)
    actual fun createDriver(): SqlDriver {
        val library =
            NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, true)
                .first() as String
        val target = "$library/$POKEDEX_DATABASE_NAME"

        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(target)) {
            val bundled =
                NSBundle.mainBundle.resourcePath?.let { "$it/$POKEDEX_RESOURCE_PATH" }
                    ?: error("No resource path on the main bundle.")
            fileManager.copyItemAtPath(bundled, toPath = target, error = null)
        }

        return NativeSqliteDriver(PokedexDatabase.Schema, POKEDEX_DATABASE_NAME)
    }
}
