package io.nicolaszurbuchen.tallgrass.infra.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.nicolaszurbuchen.tallgrass.cache.AppDatabase

actual class DatabaseDriverFactory(
    private val context: Context,
) {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(AppDatabase.Schema, context, "app.db")
}
