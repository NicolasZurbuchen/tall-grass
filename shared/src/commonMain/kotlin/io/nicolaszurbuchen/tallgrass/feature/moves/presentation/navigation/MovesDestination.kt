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
 * Carries only the slug, unlike `DetailDestination`. A Pokemon's card hands its artwork forward so
 * the hero can draw it on the first frame; a move has no image to hand over, and the two things that
 * do travel -- the name and the type pill -- are keyed from the slug on both sides. See `SharedMove`.
 */
@Serializable
data class MoveDetailDestination(
    val slug: String,
) : MovesDestination
