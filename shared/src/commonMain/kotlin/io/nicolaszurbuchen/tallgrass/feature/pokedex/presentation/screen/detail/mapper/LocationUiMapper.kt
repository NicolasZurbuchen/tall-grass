package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.CaptureMethod
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantAvailability
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.VariantEncounter
import io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper.toAvailabilityGridUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper.toEncounterMethodUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityGridUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.CaptureMethodUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.CatchDifficultyUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.LocationUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.VariantPlaceUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.spellOutSlug
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.location_detail_level_one
import tallgrass.shared.generated.resources.location_detail_level_range
import tallgrass.shared.generated.resources.location_detail_rate_up_to
import tallgrass.shared.generated.resources.pokedex_detail_catch_easy
import tallgrass.shared.generated.resources.pokedex_detail_catch_hard
import tallgrass.shared.generated.resources.pokedex_detail_catch_moderate
import tallgrass.shared.generated.resources.pokedex_detail_catch_rate_value
import tallgrass.shared.generated.resources.pokedex_detail_catch_very_easy
import tallgrass.shared.generated.resources.pokedex_detail_catch_very_hard
import tallgrass.shared.generated.resources.pokedex_detail_never_wild
import tallgrass.shared.generated.resources.pokedex_detail_not_in_game
import tallgrass.shared.generated.resources.pokedex_detail_one_place
import tallgrass.shared.generated.resources.pokedex_detail_places

/**
 * The Location tab.
 *
 * The receiver is nullable because the tab reads when it is first opened: null is "not read yet"
 * rather than "nothing found", and the two look different on screen.
 *
 * **The grid is not scoped to a region here**, unlike the route side's. The question is which of my
 * games has this Pokemon, and the answer crosses all of them.
 *
 * [emptyText] is the pair #21 insists on keeping apart. A chosen game with no rows says transfer it
 * in; a Pokemon with no rows in any game says it is never found in the wild, which for Arceus and the
 * starters -- 17.5% of species -- is a fact rather than a gap.
 */
fun VariantAvailability?.toLocationUiModel(
    captureRate: Int,
    selectedVersion: String?,
    isLoadingPlaces: Boolean,
    places: List<VariantEncounter>,
): LocationUiModel {
    val grid =
        this?.let { it.versions.toAvailabilityGridUiModel(it.encounteredVersions) }
            ?: AvailabilityGridUiModel(emptyList())

    val selected = grid.rows.flatMap { it.cells }.firstOrNull { it.slug == selectedVersion }
    val rows = places.toVariantPlacesUiModel()

    return LocationUiModel(
        isLoading = this == null,
        captureMethods = this?.captureMethods.orEmpty().map { it.toUiModel() },
        catchRateText = UiText.Resource(Res.string.pokedex_detail_catch_rate_value, listOf(captureRate)),
        catchDifficulty = captureRate.toCatchDifficultyUiModel(),
        catchFraction = (captureRate / CATCH_RATE_MAX).coerceIn(0f, 1f),
        grid = grid,
        selected = selected,
        breadcrumbText =
            selected?.let {
                when {
                    isLoadingPlaces -> null
                    rows.isEmpty() -> null
                    rows.size == 1 -> UiText.Resource(Res.string.pokedex_detail_one_place)
                    else -> UiText.Resource(Res.string.pokedex_detail_places, listOf(rows.size))
                }
            },
        places = rows,
        emptyText =
            when {
                this == null || isLoadingPlaces -> null

                // Never wild anywhere, which is a fact about the Pokemon rather than about a game,
                // so it is said before any cell is tapped.
                encounteredVersions.isEmpty() -> UiText.Resource(Res.string.pokedex_detail_never_wild)

                selected == null || rows.isNotEmpty() -> null

                else -> UiText.Resource(Res.string.pokedex_detail_not_in_game, listOf(selected.name))
            },
    )
}

/**
 * Exhaustive by construction, so a fifth capture method breaks this at compile time rather than
 * quietly failing to draw a pill.
 */
fun CaptureMethod.toUiModel(): CaptureMethodUiModel =
    when (this) {
        CaptureMethod.WILD_CATCH -> CaptureMethodUiModel.WILD_CATCH
        CaptureMethod.TRADE -> CaptureMethodUiModel.TRADE
        CaptureMethod.GIFT_OR_EVENT -> CaptureMethodUiModel.GIFT_OR_EVENT
        CaptureMethod.TRANSFER_ONLY -> CaptureMethodUiModel.TRANSFER_ONLY
    }

/**
 * A word for the catch rate, because the figure alone says nothing.
 *
 * #43's second criterion: legible without knowing that 255 is the maximum. Both halves ship -- the
 * figure over 255 for anyone who knows the scale, and the word for everyone else -- because the
 * figure is the fact and the word is the reading of it.
 *
 * The bands are cut where the games' own arithmetic changes character rather than at round numbers:
 * 255 is everything a Poke Ball catches on sight, 45 is the starters and most fully-evolved lines,
 * and 3 is the legendaries a Master Ball exists for.
 */
fun Int.toCatchDifficultyUiModel(): CatchDifficultyUiModel =
    when {
        this >= VERY_EASY -> CatchDifficultyUiModel.VERY_EASY
        this >= EASY -> CatchDifficultyUiModel.EASY
        this >= MODERATE -> CatchDifficultyUiModel.MODERATE
        this >= HARD -> CatchDifficultyUiModel.HARD
        else -> CatchDifficultyUiModel.VERY_HARD
    }

/**
 * Where this Pokemon turns up in one game, one row per place.
 *
 * **A rate here is always a best case.** There is no condition selector on this side -- one Pokemon
 * in one game is one to three rows, so a selector would be chrome over nothing -- which means a
 * conditioned row can never be pinned to a state and is qualified accordingly. A row with several
 * condition states folds to its highest, for the same reason the route side does it: the states are
 * alternatives, and summing them would invent a rate.
 *
 * [conditionText] names the state a row belongs to when it has one, so a reader can see *why* the
 * figure is a best case without a control to change it.
 */
fun List<VariantEncounter>.toVariantPlacesUiModel(): List<VariantPlaceUiModel> =
    groupBy { Triple(it.locationSlug, it.areaSlug, it.method) }
        .map { (key, rows) ->
            val best = rows.maxBy { it.chance }
            val minLevel = rows.minOf { it.minLevel }
            val maxLevel = rows.maxOf { it.maxLevel }

            VariantPlaceUiModel(
                locationSlug = key.first,
                locationName = best.locationName,
                areaName = best.areaName,
                method = key.third.toEncounterMethodUiModel(),
                levelText =
                    if (minLevel == maxLevel) {
                        UiText.Resource(Res.string.location_detail_level_one, listOf(minLevel))
                    } else {
                        UiText.Resource(Res.string.location_detail_level_range, listOf(minLevel, maxLevel))
                    },
                // Nothing here can pin a state, so every figure is "up to" -- and a method with no
                // meaningful rate prints nothing rather than a fabricated percentage.
                rateText =
                    best.chance
                        .takeIf { it > 0 }
                        ?.let { UiText.Resource(Res.string.location_detail_rate_up_to, listOf(it)) },
                conditionText =
                    best.conditions
                        .takeIf { it.isNotEmpty() }
                        ?.let { UiText.Raw(it.joinToString(CONDITION_SEPARATOR) { tag -> tag.spellOutSlug() }) },
            )
        }.sortedWith(compareBy({ it.locationName }, { it.method.slug }))

private const val CONDITION_SEPARATOR = " · "

// Caterpie and Magikarp sit at 255; a Poke Ball holds them almost on sight.
private const val VERY_EASY = 200

private const val EASY = 120

// 45 is where the starters and most fully-evolved lines land.
private const val MODERATE = 60

private const val HARD = 20

// Upstream's scale, where 255 is a Caterpie and 3 is a legendary.
private const val CATCH_RATE_MAX = 255f
