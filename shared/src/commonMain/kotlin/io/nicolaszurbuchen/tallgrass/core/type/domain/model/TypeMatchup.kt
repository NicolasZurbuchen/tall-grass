package io.nicolaszurbuchen.tallgrass.core.type.domain.model

/**
 * How much damage one defender takes from [attackingType], with both of its types accounted for.
 *
 * [multiplier] is one of 0, 0.25, 0.5, 2 and 4. A neutral matchup is not a matchup and is absent.
 */
data class TypeMatchup(
    val attackingType: PokemonType,
    val multiplier: Float,
)
