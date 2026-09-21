package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface MovesDestination : NavKey

@Serializable
data object MovesListDestination : MovesDestination

/**
 * One move, keyed by slug.
 *
 * Carries nothing else, unlike `DetailDestination`. A Pokemon's card hands its artwork forward so the
 * hero can render synchronously into a shared element; a move has no image, so the transition is an
 * ordinary push and the screen reads what it needs. See #11.
 */
@Serializable
data class MoveDetailDestination(
    val slug: String,
) : MovesDestination
