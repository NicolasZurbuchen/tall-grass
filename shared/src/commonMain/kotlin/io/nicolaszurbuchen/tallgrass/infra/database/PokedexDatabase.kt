package io.nicolaszurbuchen.tallgrass.infra.database

import io.nicolaszurbuchen.tallgrass.pokedex.PokedexDatabase

fun createPokedexDatabase(driverFactory: PokedexDriverFactory): PokedexDatabase {
    val driver = driverFactory.createDriver()
    return PokedexDatabase(
        driver = driver,
    )
}
