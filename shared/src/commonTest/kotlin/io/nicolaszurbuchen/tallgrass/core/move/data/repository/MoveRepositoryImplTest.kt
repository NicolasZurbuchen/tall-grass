package io.nicolaszurbuchen.tallgrass.core.move.data.repository

import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.MoveLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.MoveFixtures
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMove
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MoveRepositoryImplTest {
    private fun repository() =
        MoveRepositoryImpl(
            StubMoveLocalDataSource(
                moves = listOf(MoveFixtures.flamethrower),
                details = mapOf("flamethrower" to MoveFixtures.flamethrowerDetail),
            ),
        )

    @Test
    fun moves_readFromTheLocalSourceAndAreNotReshaped() =
        runTest {
            assertEquals(listOf(MoveFixtures.flamethrower), repository().moves())
        }

    @Test
    fun moveDetail_readsFromTheLocalSourceAndIsNotReshaped() =
        runTest {
            assertEquals(MoveFixtures.flamethrowerDetail, repository().moveDetail("flamethrower"))
        }

    @Test
    fun moveDetail_passesAMissingSlugStraightThroughAsNull() =
        runTest {
            assertNull(repository().moveDetail("missingno"))
        }

    private class StubMoveLocalDataSource(
        private val moves: List<Move>,
        private val details: Map<String, MoveDetail>,
    ) : MoveLocalDataSource {
        override suspend fun moves(): List<Move> = moves

        override suspend fun detail(slug: String): MoveDetail? = details[slug]

        override suspend fun learners(slug: String): List<MoveLearner> = emptyList()

        override suspend fun movesFor(variantSlug: String): List<VariantMove> = emptyList()
    }
}
