package io.nicolaszurbuchen.tallgrass.core.type.data.repository

import io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local.TypeLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeEfficacy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeRepositoryImplTest {
    private val cell = TypeEfficacy(damageType = PokemonType.WATER, factorPercent = 200)

    @Test
    fun efficaciesAgainst_readsFromTheLocalSourceAndDoesNotReshape() =
        runTest {
            val repository = TypeRepositoryImpl(StubLocalDataSource(listOf(cell)))

            assertEquals(listOf(cell), repository.efficaciesAgainst(PokemonType.FIRE))
        }

    private class StubLocalDataSource(
        private val cells: List<TypeEfficacy>,
    ) : TypeLocalDataSource {
        override suspend fun efficaciesAgainst(type: PokemonType): List<TypeEfficacy> = cells
    }
}
