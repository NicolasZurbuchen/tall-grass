package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model

import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * One battle-distinct form, and everything about a Pokemon that the form changes.
 *
 * Types are here rather than on the species because they genuinely move: Arceus has eighteen
 * variants and each one is a different type.
 *
 * [formLabel] is upstream's wording and is null for most default forms, but not all — Partner
 * Pikachu and every Totem form are non-default with no label. [isDefault] is the reliable test.
 */
data class PokemonVariant(
    val slug: String,
    val name: String,
    val formLabel: String?,
    val isDefault: Boolean,
    val artworkUrl: String,
    /** Decimetres and hectograms, upstream's units. Converted at the presentation edge. */
    val height: Int,
    val weight: Int,
    val primaryType: PokemonType,
    val secondaryType: PokemonType?,
    val stats: PokemonStats,
)
