package io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.SelectAbilities
import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.SelectAbilitiesForVariant
import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.SelectAbilityHolders
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.VariantAbility
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.Ability as AbilityRow

/**
 * Total, unlike the move list's: an ability row has nothing in it this build could fail to recognise.
 * There is no type, no damage class and no category — only prose, a name and a number.
 */
fun SelectAbilities.toDomain(): Ability =
    Ability(
        slug = slug,
        name = name,
        generation = generation.toInt(),
        shortEffect = shortEffect,
    )

/**
 * The row type is the table's, because the query selects every column — so it is imported aliased to
 * keep the domain model's name. Same as `selectMove`.
 */
fun AbilityRow.toDomain(): AbilityDetail =
    AbilityDetail(
        slug = slug,
        name = name,
        generation = generation.toInt(),
        shortEffect = shortEffect,
        effect = effect,
    )

/**
 * Null when the row names a primary type this build does not know, or has no dex card to be reached
 * through: a dropped holder is better than a card with no colour or one that cannot be opened. A
 * missing second type is the ordinary case rather than a failure. The same three rules
 * `SelectMoveLearners.toDomain` applies, because it is the same join.
 */
fun SelectAbilityHolders.toDomain(): AbilityHolder? {
    val primary = primaryType?.let(PokemonType::fromSlug) ?: return null
    val card = cardSlug ?: return null

    return AbilityHolder(
        variantSlug = slug,
        cardSlug = card,
        dexNumber = speciesDexNumber.toInt(),
        name = name,
        artworkUrl = artworkUrl,
        primaryType = primary,
        secondaryType = secondaryType?.let(PokemonType::fromSlug),
        isHidden = isHidden,
    )
}

/**
 * Total, like the list row's: a variant-ability row carries a name, a line and a flag, and there is
 * nothing in any of them this build could fail to recognise.
 */
fun SelectAbilitiesForVariant.toDomain(): VariantAbility =
    VariantAbility(
        slug = slug,
        name = name,
        shortEffect = shortEffect,
        isHidden = isHidden,
    )
