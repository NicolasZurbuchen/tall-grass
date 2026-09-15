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
 * The iOS half of the dataset copy.
 *
 * **Written but never run.** The project is Android-first with no iOS release, so this compiles in
 * CI and has not been exercised on a device. It exists because the alternative -- an `actual` that
 * throws -- would make the iOS build a lie that passes.
 *
 * The bundling step is also missing on this side: nothing copies `pokedex.db` into the iOS app
 * bundle yet, so [createDriver] will fail to find it. Whoever picks iOS up adds the Xcode build
 * phase and finds this waiting rather than having to work out the shape from scratch.
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
                NSBundle.mainBundle.pathForResource("pokedex", "db")
                    ?: error("pokedex.db is not in the app bundle. See the note on PokedexDriverFactory.")
            fileManager.copyItemAtPath(bundled, toPath = target, error = null)
        }

        return NativeSqliteDriver(PokedexDatabase.Schema, POKEDEX_DATABASE_NAME)
    }
}
