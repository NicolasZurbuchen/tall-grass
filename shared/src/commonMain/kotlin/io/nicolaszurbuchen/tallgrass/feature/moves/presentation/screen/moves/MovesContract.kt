package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move

sealed interface MovesIntent {
    data class MoveClicked(
        val slug: String,
    ) : MovesIntent

    data object RetryClicked : MovesIntent
}

sealed interface MovesLabel {
    data class NavigateToDetail(
        val slug: String,
    ) : MovesLabel
}

sealed interface MovesAction {
    data object LoadMoves : MovesAction
}

sealed interface MovesMessage {
    data object LoadStarted : MovesMessage

    data class MovesLoaded(
        val moves: List<Move>,
    ) : MovesMessage

    data class LoadFailed(
        val error: AppError,
    ) : MovesMessage
}

data class MovesState(
    val isLoading: Boolean = true,
    val moves: List<Move> = emptyList(),
    val error: AppError? = null,
)
