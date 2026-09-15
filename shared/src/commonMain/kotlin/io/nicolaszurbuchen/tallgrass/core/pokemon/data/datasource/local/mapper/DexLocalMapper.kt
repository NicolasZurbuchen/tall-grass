package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.SelectDexEntries
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * Turns a grid row into a [DexEntry], or drops it.
 *
 * Null means the row has no recognisable primary type, which can only happen if the bundled dataset
 * and this build disagree about what the eighteen types are. Dropping the card is the least bad
 * answer: the dataset ships inside the binary, so this is not a transient failure that a retry
 * fixes, and one missing card beats a crash on the dex screen.
 *
 * The secondary type is different -- absent is the normal case, not a failure. A single-type Pokemon
 * simply has none.
 */
fun SelectDexEntries.toDomain(): DexEntry? {
    val primary = primaryType?.let(PokemonType::fromSlug) ?: return null

    return DexEntry(
        slug = slug,
        dexNumber = speciesDexNumber.toInt(),
        name = name,
        formLabel = formLabel,
        artworkUrl = artworkUrl,
        primaryType = primary,
        secondaryType = secondaryType?.let(PokemonType::fromSlug),
    )
}
