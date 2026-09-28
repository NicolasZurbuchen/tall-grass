package io.nicolaszurbuchen.tallgrass.core.location.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One card in a region's Pokedex.
 *
 * Its own type rather than `DexEntry`, for two reasons the national dex does not have. It carries
 * [number], the **regional** entry number, which is not the National Dex number -- Chikorita is 1 in
 * Johto and 152 everywhere else. And it carries [cardSlug], because a regional dex names the
 * region-native form and that form is often not a card: Alola's 37 is `vulpix-alola`, which is
 * reached through `vulpix`.
 *
 * [slug] is the form the region means, and [cardSlug] is the door to it. See #5.
 */
data class RegionDexEntry(
    val slug: String,
    val cardSlug: String,
    val number: Int,
    val dexNumber: Int,
    val name: String,
    val artworkUrl: String,
    val primaryType: PokemonType,
    val secondaryType: PokemonType?,
)
