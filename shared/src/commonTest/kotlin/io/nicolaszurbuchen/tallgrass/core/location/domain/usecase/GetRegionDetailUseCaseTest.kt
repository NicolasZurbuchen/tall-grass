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
            "orre" to LocationFixtures.orreDetail,
        )

    @Test
    fun invoke_returnsWhatTheRepositoryHolds() =
        runTest {
            val useCase = GetRegionDetailUseCase(FakeLocationRepository(regionDetails = details))

            assertEquals(LocationFixtures.kantoDetail, useCase("kanto"))
        }

    @Test
    fun invoke_carriesTheAbsencesOrreHas() =
        runTest {
            // No Japanese name and no regional dex. Both are real answers rather than missing data,
            // and the About tab has to be able to tell them apart from a failed read.
            val useCase = GetRegionDetailUseCase(FakeLocationRepository(regionDetails = details))
            val orre = useCase("orre")

            assertNull(orre?.nativeName)
            assertEquals(0, orre?.pokedexSize)
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
