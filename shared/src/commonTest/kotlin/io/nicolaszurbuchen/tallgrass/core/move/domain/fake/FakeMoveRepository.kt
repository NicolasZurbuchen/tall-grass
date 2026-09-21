package io.nicolaszurbuchen.tallgrass.core.move.domain.fake

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner
import io.nicolaszurbuchen.tallgrass.core.move.domain.repository.MoveRepository

/**
 * Counted per query rather than in total, on the same grounds as `FakePokedexRepository`: the move
 * detail reads twice -- the move and who learns it -- so one counter could not tell a retry from the
 * second half of one load.
 */
class FakeMoveRepository(
    private var moves: List<Move> = emptyList(),
    private var details: Map<String, MoveDetail> = emptyMap(),
    private var learners: Map<String, List<MoveLearner>> = emptyMap(),
    private var failure: Throwable? = null,
) : MoveRepository {
    var movesCallCount: Int = 0
        private set

    var detailCallCount: Int = 0
        private set

    var learnersCallCount: Int = 0
        private set

    override suspend fun moves(): List<Move> {
        movesCallCount++
        failure?.let { throw it }
        return moves
    }

    override suspend fun moveDetail(slug: String): MoveDetail? {
        detailCallCount++
        failure?.let { throw it }
        return details[slug]
    }

    override suspend fun moveLearners(slug: String): List<MoveLearner> {
        learnersCallCount++
        failure?.let { throw it }
        return learners[slug].orEmpty()
    }
}
