package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One card in the dex grid.
 *
 * [dexNumber] is shared rather than unique: Vulpix and Alolan Vulpix are both #037. [slug] is what
 * identifies an entry and what a route keys on.
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
