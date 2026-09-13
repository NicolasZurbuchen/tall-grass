package io.nicolaszurbuchen.tallgrass.infra.database

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
