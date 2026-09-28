package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Encounter
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.EncounterCondition
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.ConditionAxisUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.ConditionOptionUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.EncounterAreaUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail.uimodel.EncounterRowUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import io.nicolaszurbuchen.tallgrass.infra.text.spellOutSlug
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.location_detail_level_one
import tallgrass.shared.generated.resources.location_detail_level_range
import tallgrass.shared.generated.resources.location_detail_rate_exact
import tallgrass.shared.generated.resources.location_detail_rate_up_to

/**
 * Which axes the table in front of the reader actually varies on.
 *
 * Derived from the rows rather than from the full list of conditions, and an axis with fewer than two
 * values is left out: a control offering one answer is asking a question nobody has.
 */
fun List<Encounter>.toConditionAxesUiModel(
    conditions: List<EncounterCondition>,
    pinned: Map<String, String>,
): List<ConditionAxisUiModel> {
    val bySlug = conditions.associateBy { it.slug }

    return flatMap { it.conditions }
        .distinct()
        .mapNotNull { bySlug[it] }
        .groupBy { it.axis }
        .filterValues { it.size > 1 }
        .map { (axis, values) ->
            // A value read without its axis in front of it: `time-day` under the Time control is
            // "Day" rather than "Time day". Upstream does not prefix consistently -- the Generation
            // VIII weather values are filed under a `max-den-rating` axis and still spell themselves
            // `weather-sandstorm` -- so the prefix comes off only when it is there.
            val label = { slug: String -> UiText.Raw(slug.removePrefix("$axis-").spellOutSlug()) }

            ConditionAxisUiModel(
                axis = axis,
                label = UiText.Raw(axis.spellOutSlug()),
                options = values.map { ConditionOptionUiModel(it.slug, label(it.slug)) }.sortedBy { it.slug },
                selected = pinned[axis],
            )
        }.sortedBy { it.axis }
}

/**
 * The rows of one method, grouped into the tables they are shares of.
 *
 * **The filter is the subset rule, and it is the whole of the correction on #8.** A row's condition
 * tags are AND-filters on that row: it is possible only when every tag it carries is satisfied. An
 * axis the reader has left unpinned satisfies all of its values at once, which is what "Any" means --
 * so a row survives when each of its tags is either the pinned value for its axis, or on an axis
 * nobody has pinned.
 *
 * That is why HeartGold's Route 1 reads as 360% of a walking table until a state is chosen, and as
 * exactly 100% once one is. Nothing here renormalises: where upstream has left variance untagged the
 * figures still exceed 100, and #24's refusal to show a running total is what keeps that honest.
 *
 * **Best case, not total, while anything is unpinned.** A variant appearing under several states is
 * one line at its highest rate rather than the sum of states that cannot happen together -- Pidgey is
 * "up to 45%", not 45% by day plus 20% at night. Within a single state its rows *are* summed, because
 * those are slots of one table.
 */
fun List<Encounter>.toEncounterAreasUiModel(
    conditions: List<EncounterCondition>,
    pinned: Map<String, String>,
): List<EncounterAreaUiModel> {
    val axisOf = conditions.associate { it.slug to it.axis }

    // The axes these rows actually vary on, which is what "every axis pinned" has to mean for the
    // figures to be exact. An axis with one value is not a choice and does not count.
    val varying =
        flatMap { it.conditions }
            .distinct()
            .mapNotNull { tag -> axisOf[tag]?.let { tag to it } }
            .groupBy({ it.second }, { it.first })
            .filterValues { it.size > 1 }
            .keys

    val isBestCase = varying.any { it !in pinned.keys }

    val matching =
        filter { encounter ->
            encounter.conditions.all { tag ->
                val axis = axisOf[tag] ?: return@all true
                pinned[axis]?.let { it == tag } ?: true
            }
        }

    // Labelled only when there is more than one, which is what makes #24's fold invisible in the
    // 85.5% of cases where a location draws the method from a single area.
    val showAreaNames = matching.map { it.areaSlug }.distinct().size > 1

    return matching
        .groupBy { it.areaSlug }
        .map { (areaSlug, rows) ->
            EncounterAreaUiModel(
                slug = areaSlug,
                name = rows.firstOrNull()?.areaName?.takeIf { showAreaNames },
                rows = rows.toRowsUiModel(isBestCase),
            )
        }.sortedBy { it.slug }
}

private fun List<Encounter>.toRowsUiModel(isBestCase: Boolean): List<EncounterRowUiModel> =
    groupBy { it.variantSlug }
        .map { (_, rows) ->
            // Summed within a state, compared across them: rows sharing a condition set are slots of
            // one table and add up, rows in different states are alternatives and the best wins.
            val chance = rows.groupBy { it.conditions.sorted() }.values.maxOf { state -> state.sumOf { it.chance } }
            val first = rows.first()
            val minLevel = rows.minOf { it.minLevel }
            val maxLevel = rows.maxOf { it.maxLevel }

            EncounterRowUiModel(
                variantSlug = first.variantSlug,
                name = first.name,
                artworkUrl = first.artworkUrl,
                primaryType = first.primaryType.toUiModel(),
                levelText =
                    if (minLevel == maxLevel) {
                        UiText.Resource(Res.string.location_detail_level_one, listOf(minLevel))
                    } else {
                        UiText.Resource(Res.string.location_detail_level_range, listOf(minLevel, maxLevel))
                    },
                // Zero is the methods with no meaningful rate -- raids, gifts, trades, SOS calls --
                // and the row prints nothing rather than a fabricated percentage.
                rateText =
                    chance.takeIf { it > 0 }?.let {
                        if (isBestCase) {
                            UiText.Resource(Res.string.location_detail_rate_up_to, listOf(it))
                        } else {
                            UiText.Resource(Res.string.location_detail_rate_exact, listOf(it))
                        }
                    },
                // A share of a hundred rather than of the largest row. Stretching the bar to fill
                // would say the commonest Pokemon on a route is a certainty.
                rateFraction = (chance / FULL_TABLE).coerceIn(0f, 1f),
                isBestCase = isBestCase,
            )
        }.sortedWith(compareByDescending<EncounterRowUiModel> { it.rateFraction }.thenBy { it.name })

private const val FULL_TABLE = 100f
