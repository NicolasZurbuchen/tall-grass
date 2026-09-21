package io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner

interface MoveLocalDataSource {
    suspend fun moves(): List<Move>

    suspend fun detail(slug: String): MoveDetail?

    suspend fun learners(slug: String): List<MoveLearner>
}
