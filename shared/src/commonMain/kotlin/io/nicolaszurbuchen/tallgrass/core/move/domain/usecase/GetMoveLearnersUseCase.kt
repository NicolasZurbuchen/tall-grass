package io.nicolaszurbuchen.tallgrass.core.move.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner
import io.nicolaszurbuchen.tallgrass.core.move.domain.repository.MoveRepository

class GetMoveLearnersUseCase(
    private val repository: MoveRepository,
) {
    suspend operator fun invoke(slug: String): List<MoveLearner> = repository.moveLearners(slug)
}
