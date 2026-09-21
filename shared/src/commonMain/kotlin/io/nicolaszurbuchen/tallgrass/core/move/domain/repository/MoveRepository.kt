package io.nicolaszurbuchen.tallgrass.core.move.domain.repository

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner

interface MoveRepository {
    /** Every main-series move, by name. */
    suspend fun moves(): List<Move>

    /** One move, or null when no row carries that slug. */
    suspend fun moveDetail(slug: String): MoveDetail?

    /** Which Pokemon learn one move, in National Dex order. Empty for the 106 nobody is taught. */
    suspend fun moveLearners(slug: String): List<MoveLearner>
}
