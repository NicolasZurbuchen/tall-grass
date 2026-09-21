package io.nicolaszurbuchen.tallgrass.core.move.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.FakeMoveRepository
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GetMoveDetailUseCaseTest {
    private val details = mapOf("flamethrower" to MoveFixtures.flamethrowerDetail)

    @Test
    fun invoke_returnsTheMoveThatWasAskedFor() =
        runTest {
            val useCase = GetMoveDetailUseCase(FakeMoveRepository(details = details))

            assertEquals(MoveFixtures.flamethrowerDetail, useCase("flamethrower"))
        }

    @Test
    fun invoke_returnsNullForASlugTheDatasetDoesNotHold() =
        runTest {
            // Null rather than an exception: the screen turns it into the not-found error, which is
            // what a disagreement between the build and the bundled dataset should read as.
            val useCase = GetMoveDetailUseCase(FakeMoveRepository(details = details))

            assertNull(useCase("missingno"))
        }

    @Test
    fun invoke_propagatesFailure() =
        runTest {
            val useCase = GetMoveDetailUseCase(FakeMoveRepository(failure = IllegalStateException("no database")))

            assertFailsWith<IllegalStateException> { useCase("flamethrower") }
        }
}
