package io.nicolaszurbuchen.tallgrass.infra.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.nicolaszurbuchen.tallgrass.cache.AppDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(AppDatabase.Schema, "app.db")
}
