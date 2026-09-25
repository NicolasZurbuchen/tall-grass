package io.nicolaszurbuchen.tallgrass.core.move.data.repository

import io.nicolaszurbuchen.tallgrass.core.move.data.datasource.local.MoveLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMove
import io.nicolaszurbuchen.tallgrass.core.move.domain.repository.MoveRepository

class MoveRepositoryImpl(
    private val localDataSource: MoveLocalDataSource,
) : MoveRepository {
    override suspend fun moves(): List<Move> = localDataSource.moves()

    override suspend fun moveDetail(slug: String): MoveDetail? = localDataSource.detail(slug)

    override suspend fun moveLearners(slug: String): List<MoveLearner> = localDataSource.learners(slug)

    override suspend fun movesFor(variantSlug: String): List<VariantMove> = localDataSource.movesFor(variantSlug)
}
