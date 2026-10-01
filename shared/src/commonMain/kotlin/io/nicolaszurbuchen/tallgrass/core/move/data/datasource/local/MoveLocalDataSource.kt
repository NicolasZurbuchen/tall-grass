package io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MaxMove
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMove

interface MoveLocalDataSource {
    suspend fun moves(): List<Move>

    suspend fun detail(slug: String): MoveDetail?

    suspend fun learners(slug: String): List<MoveLearner>

    suspend fun movesFor(variantSlug: String): List<VariantMove>

    /** The 19 Max Moves and the 33 G-Max Moves, which a Dynamaxed Pokemon's own moves turn into. */
    suspend fun maxMoves(): List<MaxMove>
}
