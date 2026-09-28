package io.nicolaszurbuchen.tallgrass.core.location.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.FakeLocationRepository
import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GetRegionDetailUseCaseTest {
    private val details =
        mapOf(
            "kanto" to LocationFixtures.kantoDetail,
            "johto" to LocationFixtures.johtoDetail,
        )

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetRegionDetailUseCase(FakeLocationRepository(regionDetails = details))

            assertEquals(LocationFixtures.kantoDetail, useCase("kanto"))
        }

    @Test
    fun invoke_carriesTheFactsTheAboutTabDraws() =
        runTest {
            val useCase = GetRegionDetailUseCase(FakeLocationRepository(regionDetails = details))
            val johto = useCase("johto")

            assertEquals("ジョウト", johto?.nativeName)
            assertEquals(251, johto?.pokedexSize)
        }

    @Test
    fun invoke_returnsNullForASlugWithNoRow() =
        runTest {
            val useCase = GetRegionDetailUseCase(FakeLocationRepository(regionDetails = details))

            assertNull(useCase("hisui-but-spelled-wrong"))
        }

    @Test
    fun invoke_propagatesFailureRatherThanReturningNull() =
        runTest {
            // Null already means "no such region", so a broken read must not borrow it.
            val useCase = GetRegionDetailUseCase(FakeLocationRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("kanto") }
        }
}
