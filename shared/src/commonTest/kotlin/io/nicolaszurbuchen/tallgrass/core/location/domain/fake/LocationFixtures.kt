package io.nicolaszurbuchen.tallgrass.core.location.domain.fake

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.CaptureMethod
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Console
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Encounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.EncounterCondition
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.GameVersion
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationCategory
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationSummary
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Region
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDetail
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantAvailability
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantEncounter
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * The world, small enough to hold in one head and chosen for the cases rather than for the names.
 *
 * Kanto and Orre, because between them they cover every absence the screens have to survive: Orre has
 * no Japanese name, no regional Pokedex, and games upstream does not file under any region. Route 1
 * carries a conditioned walking table, which is the shape the correction on #8 is about.
 */
object LocationFixtures {
    val red =
        GameVersion(
            slug = "red",
            name = "Red",
            code = "R",
            console = Console.GB_GBC,
            generation = 1,
        )

    val heartgold =
        GameVersion(
            slug = "heartgold",
            name = "HeartGold",
            code = "HG",
            console = Console.DS,
            generation = 4,
        )

    // Shares its code with Yellow and is told apart by its row, which is the whole reason the grid
    // groups by console. See #9.
    val pokemonY =
        GameVersion(
            slug = "y",
            name = "Y",
            code = "Y",
            console = Console.THREE_DS,
            generation = 6,
        )

    val colosseum =
        GameVersion(
            slug = "colosseum",
            name = "Colosseum",
            code = "Co",
            console = Console.GAME_CUBE,
            generation = 3,
        )

    val kanto =
        Region(
            slug = "kanto",
            name = "Kanto",
            nativeName = "カントー",
            generation = 1,
            locationCount = 96,
            boxArt = listOf("charizard.png", "blastoise.png"),
        )

    val orre =
        Region(
            slug = "orre",
            name = "Orre",
            // Upstream has no Japanese name for this one, and only this one.
            nativeName = null,
            generation = 3,
            locationCount = 18,
            boxArt = listOf("espeon.png", "umbreon.png"),
        )

    val kantoDetail =
        RegionDetail(
            slug = "kanto",
            name = "Kanto",
            nativeName = "カントー",
            generation = 1,
            blurb = "The region the series began in.",
            locationCount = 96,
            pokedexSize = 151,
            versions = listOf(heartgold, red),
        )

    val orreDetail =
        RegionDetail(
            slug = "orre",
            name = "Orre",
            nativeName = null,
            generation = 3,
            blurb = "A desert region off the main sequence.",
            locationCount = 18,
            // Upstream has no Orre dex, and the tab says so rather than showing the national one.
            pokedexSize = 0,
            versions = listOf(colosseum),
        )

    val route1Summary =
        LocationSummary(
            slug = "kanto-route-1",
            name = "Route 1",
            category = LocationCategory.ROUTE,
            versionCount = 12,
        )

    // Listed although nothing can be met in most games, which is #24's point: the absence is the
    // answer rather than a reason to leave the row out.
    val berryForestSummary =
        LocationSummary(
            slug = "berry-forest",
            name = "Berry Forest",
            category = LocationCategory.FOREST,
            versionCount = 2,
        )

    val route1 =
        LocationDetail(
            slug = "kanto-route-1",
            name = "Route 1",
            category = LocationCategory.ROUTE,
            regionSlug = "kanto",
            regionName = "Kanto",
            // Red is drawn grey: the cell is there to be tapped so a negative can be confirmed.
            versions = listOf(heartgold, red),
            encounteredVersions = setOf("heartgold"),
        )

    /**
     * Two rows of one twelve-slot table, each tagged with the state that switches it on.
     *
     * Together they read as 75% of a walking table and neither is wrong: pinned to day they are
     * Pidgey alone, pinned to night they are Rattata alone. This is the shape the correction on #8
     * describes, in the smallest form that still shows it.
     */
    val pidgeyByDay =
        Encounter(
            id = 1,
            variantSlug = "pidgey",
            cardSlug = "pidgey",
            dexNumber = 16,
            name = "Pidgey",
            artworkUrl = "pidgey.png",
            primaryType = PokemonType.NORMAL,
            secondaryType = PokemonType.FLYING,
            areaSlug = "kanto-route-1",
            areaName = null,
            method = "walk",
            minLevel = 2,
            maxLevel = 4,
            chance = 45,
            conditions = listOf("time-day"),
        )

    val rattataByNight =
        pidgeyByDay.copy(
            id = 2,
            variantSlug = "rattata",
            cardSlug = "rattata",
            dexNumber = 19,
            name = "Rattata",
            artworkUrl = "rattata.png",
            secondaryType = null,
            chance = 30,
            conditions = listOf("time-night"),
        )

    val pidgeyOnRoute1 =
        VariantEncounter(
            id = 1,
            locationSlug = "kanto-route-1",
            locationName = "Route 1",
            areaSlug = "kanto-route-1",
            areaName = null,
            method = "walk",
            minLevel = 2,
            maxLevel = 4,
            chance = 45,
            conditions = listOf("time-day"),
        )

    val pidgeyAvailability =
        VariantAvailability(
            variantSlug = "pidgey",
            captureMethods = listOf(CaptureMethod.WILD_CATCH),
            versions = listOf(heartgold, red, pokemonY),
            encounteredVersions = setOf("heartgold", "red"),
        )

    val timeOfDay =
        listOf(
            EncounterCondition(slug = "time-day", axis = "time", isDefault = true),
            EncounterCondition(slug = "time-night", axis = "time", isDefault = false),
        )
}
