package io.nicolaszurbuchen.tallgrass.core.move.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.core.move.domain.repository.MoveRepository

class GetMovesUseCase(
    private val repository: MoveRepository,
) {
    suspend operator fun invoke(): List<Move> = repository.moves()
}
