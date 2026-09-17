package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.SelectDexEntries
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * Null when the row's primary type is not one of the eighteen, which means the bundled dataset and
 * this build disagree. A missing secondary type is the ordinary case, not a failure.
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
