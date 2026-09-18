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
 *
 * [formKind] is what a screen filters on. Every form of a species is here, cosmetic ones included,
 * because whether a costume is worth showing is a question about the screen rather than about the
 * Pokemon — the detail switcher hides them and a count of them would need them present.
 */
data class PokemonVariant(
    val slug: String,
    val name: String,
    val formLabel: String?,
    val formKind: FormKind,
    val isDefault: Boolean,
    val artworkUrl: String,
    /** Decimetres and hectograms, upstream's units. Converted at the presentation edge. */
    val height: Int,
    val weight: Int,
    val primaryType: PokemonType,
    val secondaryType: PokemonType?,
    val stats: PokemonStats,
)
