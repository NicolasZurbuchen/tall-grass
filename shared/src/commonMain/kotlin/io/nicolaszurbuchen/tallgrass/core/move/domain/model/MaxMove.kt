package io.nicolaszurbuchen.tallgrass.core.move.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One move a Dynamaxed Pokemon's moves can turn into: a Max Move, or a Gigantamax form's own G-Max
 * Move.
 *
 * Only the three things the conversion needs to name one. Category and power are deliberately not
 * here, because a Max Move has neither of its own -- both come from whichever move it replaced.
 *
 * [power] is the exception and is set for three of the fifty-two: G-Max Drum Solo, Fireball and
 * Hydrosnipe are 160 whatever they replaced. Null everywhere else, which is what tells the
 * conversion to ask the base move instead.
 */
data class MaxMove(
    val slug: String,
    val name: String,
    val type: PokemonType,
    val power: Int?,
)
