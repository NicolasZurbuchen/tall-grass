package io.nicolaszurbuchen.tallgrass.core.move.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One Pokemon that learns a move.
 *
 * [level] is set only where [method] is [LearnMethod.LEVEL_UP], and not even for all of those: 160
 * level-up moves are known without being taught, on evolution or from the start. Upstream writes 0
 * in every one of those cases and 0 is not a level.
 *
 * The name, the artwork and the types are joined from the variant rather than stored beside the
 * learner, so there is one statement of what a Pokemon looks like rather than 62,777 copies of it.
 * They are here at all because tapping one of these opens its detail screen, whose hero draws all
 * three before it has read anything.
 */
data class MoveLearner(
    val variantSlug: String,
    val dexNumber: Int,
    val name: String,
    val formLabel: String?,
    val artworkUrl: String,
    val primaryType: PokemonType,
    val secondaryType: PokemonType?,
    val method: LearnMethod,
    val level: Int?,
)
