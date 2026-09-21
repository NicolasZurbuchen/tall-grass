package io.nicolaszurbuchen.tallgrass.core.move.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMove
import io.nicolaszurbuchen.tallgrass.core.move.domain.repository.MoveRepository

class GetMovesForVariantUseCase(
    private val repository: MoveRepository,
) {
    suspend operator fun invoke(variantSlug: String): List<VariantMove> = repository.movesFor(variantSlug)
}
