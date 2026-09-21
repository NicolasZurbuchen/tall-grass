package io.nicolaszurbuchen.tallgrass.infra.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

/**
 * Compiles in CI and has never run on a device, so the bundle paths below are the documented layout
 * rather than an observed one.
 */
actual class PokedexDriverFactory {
    // Three of these Foundation calls take an NSError out-parameter, which is a C pointer even when
    // it is null.
    @OptIn(ExperimentalForeignApi::class)
    actual fun createDriver(): SqlDriver {
        val library =
            NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, true)
                .first() as String
        val target = "$library/$POKEDEX_DATABASE_NAME"
        val stamp = "$library/$POKEDEX_STAMP_NAME"

        val fileManager = NSFileManager.defaultManager
        val resources =
            NSBundle.mainBundle.resourcePath
                ?: error("No resource path on the main bundle.")

        val bundledStamp =
            NSString.stringWithContentsOfFile("$resources/$POKEDEX_STAMP_RESOURCE_PATH", NSUTF8StringEncoding, null)
                ?: error("The bundled dataset has no stamp.")

        // **Compared rather than merely checked for existence.** An update ships a new dataset inside
        // the bundle, and the bundle is not the file the app opens: without this, the copy made on the
        // very first run is the dex the user keeps for as long as the app is installed.
        val copied = NSString.stringWithContentsOfFile(stamp, NSUTF8StringEncoding, null)
        if (!fileManager.fileExistsAtPath(target) || copied != bundledStamp) {
            // The old copy has to go first: copyItemAtPath fails rather than replacing.
            fileManager.removeItemAtPath(target, null)
            fileManager.copyItemAtPath("$resources/$POKEDEX_RESOURCE_PATH", toPath = target, error = null)

            // Written after the copy, so a failure here means the next launch copies again rather
            // than trusting a stamp for a database that never landed.
            (bundledStamp as NSString).writeToFile(stamp, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        }

        return NativeSqliteDriver(PokedexDatabase.Schema, POKEDEX_DATABASE_NAME)
    }
}
