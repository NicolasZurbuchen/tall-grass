package io.nicolaszurbuchen.tallgrass.infra.database

import io.nicolaszurbuchen.tallgrass.cache.AppDatabase

fun createDatabase(driverFactory: DatabaseDriverFactory): AppDatabase {
    val driver = driverFactory.createDriver()
    return AppDatabase(
        driver = driver,
    )
}
