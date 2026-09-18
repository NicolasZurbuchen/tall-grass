package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.SelectVariantDetails
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.Species
import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.VariantStat
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EggGroup
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.FormKind
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.GrowthRate
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonSpecies
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonStats
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * Null when the row's growth rate is not one of the six, which means the bundled dataset and this
 * build disagree. An egg group that does not parse is dropped on its own: a species with one
 * unreadable group still has a breeding block worth drawing.
 */
fun Species.toDomain(eggGroupSlugs: List<String>): PokemonSpecies? {
    val growth = GrowthRate.fromSlug(growthRate) ?: return null

    return PokemonSpecies(
        dexNumber = dexNumber.toInt(),
        name = name,
        genus = genus,
        genderRate = genderRate.toInt(),
        captureRate = captureRate.toInt(),
        hatchCounter = hatchCounter.toInt(),
        growthRate = growth,
        eggGroups = eggGroupSlugs.mapNotNull(EggGroup::fromSlug),
    )
}

/** Null when the row's primary type is not one of the eighteen. A missing second type is ordinary. */
fun SelectVariantDetails.toDomain(stats: PokemonStats): PokemonVariant? {
    val primary = primaryType?.let(PokemonType::fromSlug) ?: return null

    return PokemonVariant(
        slug = slug,
        name = name,
        formLabel = formLabel,
        formKind = FormKind.fromName(formKind),
        isDefault = isDefault,
        artworkUrl = artworkUrl,
        height = height.toInt(),
        weight = weight.toInt(),
        primaryType = primary,
        secondaryType = secondaryType?.let(PokemonType::fromSlug),
        stats = stats,
    )
}

/**
 * The six stat rows of every variant of one species, gathered into one record each.
 *
 * A variant missing any of the six is left out of the result entirely rather than defaulted to
 * zero, because a stat bar at zero is indistinguishable from a real answer.
 */
fun List<VariantStat>.toStatsByVariantDomain(): Map<String, PokemonStats> =
    groupBy { it.variantSlug }
        .mapNotNull { (variantSlug, rows) ->
            val byStat = rows.associate { it.statSlug to it.baseStat.toInt() }

            val stats =
                PokemonStats(
                    hp = byStat[STAT_HP] ?: return@mapNotNull null,
                    attack = byStat[STAT_ATTACK] ?: return@mapNotNull null,
                    defense = byStat[STAT_DEFENSE] ?: return@mapNotNull null,
                    specialAttack = byStat[STAT_SPECIAL_ATTACK] ?: return@mapNotNull null,
                    specialDefense = byStat[STAT_SPECIAL_DEFENSE] ?: return@mapNotNull null,
                    speed = byStat[STAT_SPEED] ?: return@mapNotNull null,
                )

            variantSlug to stats
        }
        .toMap()

private const val STAT_HP = "hp"
private const val STAT_ATTACK = "attack"
private const val STAT_DEFENSE = "defense"
private const val STAT_SPECIAL_ATTACK = "special-attack"
private const val STAT_SPECIAL_DEFENSE = "special-defense"
private const val STAT_SPEED = "speed"
