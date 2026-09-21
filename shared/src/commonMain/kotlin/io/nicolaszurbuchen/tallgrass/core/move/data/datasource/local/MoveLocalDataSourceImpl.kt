package io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMove
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class MoveLocalDataSourceImpl(
    private val queries: Lazy<MoveQueries>,
    private val dispatcher: CoroutineDispatcher,
) : MoveLocalDataSource {
    override suspend fun moves(): List<Move> =
        withContext(dispatcher) {
            queries.value
                .selectMoves()
                .executeAsList()
                .mapNotNull { it.toDomain() }
        }

    // Two queries rather than a join: a move has at most five stat changes and most have none, so a
    // join would repeat twenty-four columns to carry a third one.
    override suspend fun detail(slug: String): MoveDetail? =
        withContext(dispatcher) {
            val move = queries.value.selectMove(slug).executeAsOneOrNull() ?: return@withContext null
            val statChanges = queries.value.selectMoveStatChanges(slug).executeAsList()

            move.toDomain(statChanges)
        }

    // Its own read rather than part of the detail's, because it is its own tab: a reader who never
    // opens it never pays for 1,213 rows, which is what Rest would cost them.
    override suspend fun learners(slug: String): List<MoveLearner> =
        withContext(dispatcher) {
            queries.value
                .selectMoveLearners(slug)
                .executeAsList()
                .mapNotNull { it.toDomain() }
        }

    // Sorted here rather than in SQL. How the list reads -- level-up first and by level within it,
    // then the three methods that have no level -- lives in LearnMethod's declaration order, and a
    // CASE expression in the query would be a second copy of it to keep in step.
    override suspend fun movesFor(variantSlug: String): List<VariantMove> =
        withContext(dispatcher) {
            queries.value
                .selectMovesForVariant(variantSlug)
                .executeAsList()
                .mapNotNull { it.toDomain() }
                .sortedWith(compareBy({ it.method.ordinal }, { it.level ?: 0 }, { it.name }))
        }
}
