package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One card in the dex grid.
 *
 * A variant that earns a place in the grid, flattened with the bits of its species the card shows.
 * Deliberately not a whole Variant: the grid draws roughly eleven hundred of these at once, and the
 * stats, abilities and breeding data none of them display would be a thousand-fold waste.
 *
 * [dexNumber] is shared rather than unique -- Vulpix and Alolan Vulpix are both #037. [slug] is what
 * identifies the entry, and what a route keys on.
 */
data class DexEntry(
    val slug: String,
    val dexNumber: Int,
    val name: String,
    val formLabel: String?,
    val artworkUrl: String,
    val primaryType: PokemonType,
    val secondaryType: PokemonType?,
)
