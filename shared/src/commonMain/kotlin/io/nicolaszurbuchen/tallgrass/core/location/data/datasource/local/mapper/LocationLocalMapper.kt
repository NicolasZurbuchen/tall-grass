package io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.EncounterConditionValue
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.SelectEncountersAtLocation
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.SelectEncountersForVariant
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.SelectGameVersions
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.SelectLocationsByRegion
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.SelectRegionDex
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.SelectRegionVersions
import io.nicolaszurbuchen.tallgrass.core.location.data.datasource.local.SelectRegions
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.CaptureMethod
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Console
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Encounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.EncounterCondition
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.GameVersion
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationCategory
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationSummary
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Region
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDexEntry
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantEncounter
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType

/**
 * The box art arrives as a separate read, because a card needs two rows and eleven cards would
 * otherwise be eleven follow-up queries. [boxArt] is the artwork for this region, already ordered.
 */
fun SelectRegions.toDomain(boxArt: List<String>): Region =
    Region(
        slug = slug,
        name = name,
        nativeName = nativeName,
        generation = generation.toInt(),
        locationCount = locationCount.toInt(),
        boxArt = boxArt,
    )

/**
 * Null when the row names a console this build does not know.
 *
 * A dropped cell is better than one drawn in a row that does not exist: the grid's rows *are* the
 * consoles, so a version with nowhere to sit has no cell to be.
 */
fun SelectGameVersions.toDomain(): GameVersion? {
    val machine = Console.fromName(console) ?: return null

    return GameVersion(
        slug = slug,
        name = name,
        code = code,
        console = machine,
        generation = generation.toInt(),
    )
}

/** The region-scoped read of the same rows, and the same rule about an unknown console. */
fun SelectRegionVersions.toDomain(): GameVersion? {
    val machine = Console.fromName(console) ?: return null

    return GameVersion(
        slug = slug,
        name = name,
        code = code,
        console = machine,
        generation = generation.toInt(),
    )
}

/**
 * Total, unlike the version rows': an unknown category is not a failure, because the category was
 * never a fact. It falls to [LocationCategory.OTHER] and the marker goes neutral.
 */
fun SelectLocationsByRegion.toDomain(): LocationSummary =
    LocationSummary(
        slug = slug,
        name = name,
        category = LocationCategory.fromName(category),
        versionCount = versionCount.toInt(),
    )

/**
 * Null when the row names a primary type this build does not know, or has no dex card to be reached
 * through. The same two rules every other join onto `variant` applies: a card with no colour, or one
 * that cannot be opened.
 *
 * The card matters more here than anywhere else, because a regional dex deliberately names forms
 * that are not cards -- Alola's 37 is `vulpix-alola`, which opens through `vulpix`.
 */
fun SelectRegionDex.toDomain(): RegionDexEntry? {
    val primary = primaryType?.let(PokemonType::fromSlug) ?: return null
    val card = cardSlug ?: return null

    return RegionDexEntry(
        slug = slug,
        cardSlug = card,
        number = number.toInt(),
        dexNumber = speciesDexNumber.toInt(),
        name = name,
        artworkUrl = artworkUrl,
        primaryType = primary,
        secondaryType = secondaryType?.let(PokemonType::fromSlug),
    )
}

/**
 * [conditions] arrives separately and is keyed by encounter id, because a busy route is 42 rows and
 * reading each one's tags after it would be 42 more queries.
 *
 * Null on an unknown primary type or a missing dex card, for the same two reasons every other join
 * onto `variant` drops a row: a card with no colour, or one that cannot be opened.
 */
fun SelectEncountersAtLocation.toDomain(conditions: List<String>): Encounter? {
    val primary = primaryType?.let(PokemonType::fromSlug) ?: return null
    val card = cardSlug ?: return null

    return Encounter(
        id = id,
        variantSlug = variantSlug,
        cardSlug = card,
        dexNumber = speciesDexNumber.toInt(),
        name = name,
        artworkUrl = artworkUrl,
        primaryType = primary,
        secondaryType = secondaryType?.let(PokemonType::fromSlug),
        areaSlug = areaSlug,
        areaName = areaName,
        method = method,
        minLevel = minLevel.toInt(),
        maxLevel = maxLevel.toInt(),
        chance = chance.toInt(),
        conditions = conditions,
    )
}

/** Total: the Pokemon side of the join carries no type and no card, so there is nothing to drop on. */
fun SelectEncountersForVariant.toDomain(conditions: List<String>): VariantEncounter =
    VariantEncounter(
        id = id,
        locationSlug = locationSlug,
        locationName = locationName,
        areaSlug = areaSlug,
        areaName = areaName,
        method = method,
        minLevel = minLevel.toInt(),
        maxLevel = maxLevel.toInt(),
        chance = chance.toInt(),
        conditions = conditions,
    )

fun EncounterConditionValue.toDomain(): EncounterCondition =
    EncounterCondition(
        slug = slug,
        axis = axis,
        isDefault = isDefault,
    )

/**
 * The capture pills, from the methods a Pokemon turns up under and the generations it turns up in.
 *
 * `gift` and `npc-trade` are encounter methods upstream but are not wild catches, so a starter does
 * not end up claiming to be findable in the grass. [newestGenerationWithData] is passed in rather
 * than written down as 8, because Generation IX is empty today and the answer moves on its own on
 * the day it is not.
 *
 * There is no Evolve pill; [CaptureMethod] says why.
 */
fun Set<String>.toCaptureMethodsDomain(
    generationsPresent: Set<Int>,
    newestGenerationWithData: Int,
): List<CaptureMethod> {
    // Named, because inside `buildList` the receiver is the list being built and a bare `any` would
    // ask the answer about the pills rather than about the methods.
    val methods = this

    return buildList {
        if (methods.any { it !in GIFT_METHODS && it !in TRADE_METHODS }) add(CaptureMethod.WILD_CATCH)
        if (methods.any { it in TRADE_METHODS }) add(CaptureMethod.TRADE)
        if (methods.any { it in GIFT_METHODS }) add(CaptureMethod.GIFT_OR_EVENT)
        // An absence rather than a row, which is the whole of what "transfer only" means -- so it
        // needs a generation to be absent *from*. Zero is a database with no encounters in it at
        // all, where the honest answer is to say nothing rather than to call every Pokemon a
        // transfer.
        if (newestGenerationWithData > 0 && newestGenerationWithData !in generationsPresent) {
            add(CaptureMethod.TRANSFER_ONLY)
        }
    }
}

// `gift-egg` is the Togepi case, and reads as a gift to anyone holding it.
private val GIFT_METHODS = setOf("gift", "gift-egg")

private val TRADE_METHODS = setOf("npc-trade")
