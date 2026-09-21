package io.nicolaszurbuchen.tallgrass.core.move.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.domain.repository.MoveRepository

class GetMoveDetailUseCase(
    private val repository: MoveRepository,
) {
    suspend operator fun invoke(slug: String): MoveDetail? = repository.moveDetail(slug)
}
