package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper.toAvailabilityGridUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper.toEncounterMethodUiModel
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.AvailabilityGridUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.mapper.toConditionAxesUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.mapper.toEncounterAreasUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.location_detail_no_data_yet
import tallgrass.shared.generated.resources.location_detail_none_here
import tallgrass.shared.generated.resources.location_detail_not_in_game
import tallgrass.shared.generated.resources.location_detail_one_species
import tallgrass.shared.generated.resources.location_detail_species

/**
 * The two empty states, which #21 insists must not be confused.
 *
 * A grey cell is tappable precisely so a negative can be confirmed, and what it says depends on
 * whether the silence is upstream's or the game's. Generation IX has no encounter data at all, so
 * Scarlet answers *"not available yet"* -- a gap. Every other game answers *"nothing recorded here"*,
 * which is a fact about that route in that game.
 *
 * Deciding it on the generation rather than on a hardcoded list of games means the first wording
 * stops appearing by itself on the day upstream fills Generation IX in.
 */
fun LocationDetailState.toUiModel(): LocationDetailUiModel {
    val grid =
        location
            ?.let { it.versions.toAvailabilityGridUiModel(it.encounteredVersions) }
            ?: AvailabilityGridUiModel(emptyList())

    val selectedCell = grid.rows.flatMap { it.cells }.firstOrNull { it.slug == version }
    val selectedVersion = location?.versions?.firstOrNull { it.slug == version }

    // A local lambda rather than a private function, because a UiMapper file may hold only the one
    // State-to-UiModel mapping. Same shape as AboutUiMapper's `awards`.
    val speciesCount = { count: Int ->
        if (count == 1) {
            UiText.Resource(Res.string.location_detail_one_species)
        } else {
            UiText.Resource(Res.string.location_detail_species, listOf(count))
        }
    }

    val methods = encounters.map { it.method }.distinct()
    val method = this.method ?: methods.firstOrNull()
    val rows = encounters.filter { it.method == method }

    return LocationDetailUiModel(
        isLoading = isLoading,
        name = location?.name.orEmpty(),
        regionName = location?.regionName.orEmpty(),
        grid = grid,
        selected = selectedCell,
        breadcrumbText =
            selectedCell?.let {
                when {
                    isLoadingEncounters -> null
                    encounters.isEmpty() -> UiText.Resource(Res.string.location_detail_none_here)
                    else -> speciesCount(encounters.map { row -> row.variantSlug }.distinct().size)
                }
            },
        isLoadingEncounters = isLoadingEncounters,
        methods = methods.map { it.toEncounterMethodUiModel() },
        selectedMethod = method,
        axes = rows.toConditionAxesUiModel(conditions, pinned),
        areas = rows.toEncounterAreasUiModel(conditions, pinned),
        emptyText =
            when {
                selectedCell == null || isLoadingEncounters || encounters.isNotEmpty() -> {
                    null
                }

                // The generation upstream has nothing for at all. Not a list of games, so this
                // answer disappears by itself the day that changes. See #21.
                selectedVersion != null && selectedVersion.generation >= FIRST_UNMAPPED_GENERATION -> {
                    UiText.Resource(Res.string.location_detail_no_data_yet)
                }

                else -> {
                    UiText.Resource(
                        Res.string.location_detail_not_in_game,
                        listOf(selectedCell.name),
                    )
                }
            },
        error = error?.toUiModel(),
    )
}

// Generation IX, which upstream has no encounter rows for anywhere. Hisui is the other gap and is a
// Generation VIII region, so it is not caught by this and reads as "nothing recorded here" -- which
// is the honest wording either way, because Legends: Arceus genuinely has no route tables upstream
// rather than a chapter nobody has typed in yet.
private const val FIRST_UNMAPPED_GENERATION = 9
