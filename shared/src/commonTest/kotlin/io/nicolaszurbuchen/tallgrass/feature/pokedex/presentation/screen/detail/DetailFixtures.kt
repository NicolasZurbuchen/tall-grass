package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EggGroup
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.FormKind
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.GrowthRate
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonSpecies
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonStats
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey

/**
 * Charizard, because it is the case that exercises the split: three forms, a second type, and a Mega
 * that weighs more than the ordinary one while sharing its egg groups.
 */
internal val charizardSpecies =
    PokemonSpecies(
        dexNumber = 6,
        name = "Charizard",
        genus = "Flame Pokémon",
        genderRate = 1,
        captureRate = 45,
        hatchCounter = 20,
        growthRate = GrowthRate.MEDIUM_SLOW,
        eggGroups = listOf(EggGroup.MONSTER, EggGroup.DRAGON),
    )

internal val charizard =
    PokemonVariant(
        slug = "charizard",
        name = "Charizard",
        formLabel = null,
        formKind = FormKind.NONE,
        isDefault = true,
        artworkUrl = "https://example.invalid/6.png",
        height = 17,
        weight = 905,
        primaryType = PokemonType.FIRE,
        secondaryType = PokemonType.FLYING,
        stats = PokemonStats(hp = 78, attack = 84, defense = 78, specialAttack = 109, specialDefense = 85, speed = 100),
    )

internal val charizardMegaX =
    PokemonVariant(
        slug = "charizard-mega-x",
        name = "Mega Charizard X",
        formLabel = "Mega Charizard X",
        formKind = FormKind.MEGA,
        isDefault = false,
        artworkUrl = "https://example.invalid/10034.png",
        height = 17,
        weight = 1105,
        primaryType = PokemonType.FIRE,
        secondaryType = PokemonType.DRAGON,
        stats = PokemonStats(hp = 78, attack = 130, defense = 111, specialAttack = 130, specialDefense = 85, speed = 100),
    )

internal val charizardDetail = PokemonDetail(species = charizardSpecies, variants = listOf(charizard, charizardMegaX))

internal val charizardHandoff =
    HeroHandoff(
        artworkUrl = charizard.artworkUrl,
        primaryTypeSlug = PokemonType.FIRE.slug,
        sharedElementKey = dexArtworkKey(charizard.slug),
    )

/** A costume: the switcher must not list it. Charizard has none, so this one is invented. */
internal val charizardCostume =
    charizard.copy(
        slug = "charizard-party-hat",
        name = "Charizard",
        formLabel = "Party Hat",
        formKind = FormKind.COSMETIC,
        isDefault = false,
    )
