package io.nicolaszurbuchen.tallgrass.core.ability.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One Pokemon that has an ability.
 *
 * [isHidden] is the ability in its third slot — the one a Pokemon cannot be caught with ordinarily,
 * which is why it is worth saying on the card rather than only counting.
 *
 * The name, the artwork and the types are joined from the variant rather than stored beside the
 * holder, and they are here at all because tapping one of these opens its detail screen, whose hero
 * draws all three before it has read anything. The same shape as
 * [io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner], including [cardSlug]: only
 * default forms are dex cards, so Alolan Sandshrew is reached through Sandshrew, and the detail needs
 * the card its carousel walks as well as the form on screen.
 */
data class AbilityHolder(
    val variantSlug: String,
    val cardSlug: String,
    val dexNumber: Int,
    val name: String,
    val artworkUrl: String,
    val primaryType: PokemonType,
    val secondaryType: PokemonType?,
    val isHidden: Boolean,
)
