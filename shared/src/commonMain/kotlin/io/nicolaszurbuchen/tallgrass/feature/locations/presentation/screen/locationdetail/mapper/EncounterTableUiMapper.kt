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
    locationSlug: String,
    locationName: String,
): List<EncounterAreaUiModel> {
    val axisOf = conditions.associate { it.slug to it.axis }

    // The values each axis can take in this table. An axis with one value is not a choice: it applies
    // always, so it neither needs a control nor makes anything uncertain.
    val tagsByAxis =
        flatMap { it.conditions }
            .distinct()
            .mapNotNull { tag -> axisOf[tag]?.let { axis -> axis to tag } }
            .groupBy({ it.first }, { it.second })

    val matching =
        filter { encounter ->
            encounter.conditions.all { tag ->
                val axis = axisOf[tag] ?: return@all true
                pinned[axis]?.let { it == tag } ?: true
            }
        }

    // Every complete condition state the pins still allow, or null when there are too many to walk.
    //
    // **One more option per axis than it has tags**, which is the "none of these" case: upstream lists
    // only the states that have rows, so a table whose time tags are all `time-morning` still has a
    // rest of the day, and in it every morning-tagged row is absent. That extra option is what tells
    // the two kinds of row apart — a row tagged with nothing is counted in every state and is exact,
    // a morning-only row is counted in one and is a best case.
    //
    // Null past the cap is a real possibility rather than defensiveness: Galar's Wild Area tables
    // carry weather and story progress together, and the product is not bounded by anything here.
    val unpinned = tagsByAxis.filterKeys { it !in pinned.keys }
    val stateCount = unpinned.values.fold(1) { total, tags -> total * (tags.size + 1) }
    val states =
        if (stateCount > STATE_CAP) {
            null
        } else {
            unpinned.values.fold(listOf(pinned.values.toSet())) { built, tags ->
                built.flatMap { state -> tags.map { state + it } + listOf(state) }
            }
        }

    // Labelled when the method comes from more than one area, which is what keeps #24's fold invisible
    // in the 85.5% of cases where it comes from one.
    //
    // Upstream names only some of them: 179 areas belonging to multi-area locations have no prose name
    // at all, and Brooklet Hill's four are among them. Unlabelled they read as duplicates -- three
    // identical bubbling spots at 50% apiece, apparently adding to 150% -- when they are three separate
    // tables that each total 100. So the slug stands in, spelled out: "North", "South", "Totems den".
    //
    // The one area whose slug *is* the location's takes the location's own name, because it is the
    // place itself rather than a corner of it: "Postwick" beside "Leons room", not "Postwick" twice.
    val areas = matching.map { it.areaSlug }.distinct()
    val label = { areaSlug: String, areaName: String? ->
        areaName ?: locationName.takeIf { areaSlug == locationSlug } ?: areaSlug.spellOutSlug()
    }

    return matching
        .groupBy { it.areaSlug }
        .map { (areaSlug, rows) ->
            EncounterAreaUiModel(
                slug = areaSlug,
                name = if (areas.size > 1) label(areaSlug, rows.firstOrNull()?.areaName) else null,
                rows = rows.toRowsUiModel(states),
            )
        }.sortedBy { it.slug }
}

/**
 * **A rate is a best case only where it actually varies.**
 *
 * The first version of this asked the table: if any axis was unpinned, every row in it was written
 * "up to". That was wrong for most rows. Horsea on Kanto's Route 19 is 25% on the Super Rod in every
 * state there is, and writing "up to 25%" of a figure that never moves undersells it — while a
 * Pokemon that really is day-only sits in the same table and really is a best case.
 *
 * So each variant is asked separately: compute its share in every state the pins still allow, and if
 * the answer never changes, the figure is exact. Pidgey at 45% by day and nothing at night varies and
 * says "up to"; Horsea, tagged with nothing, does not.
 */
private fun List<Encounter>.toRowsUiModel(states: List<Set<String>>?): List<EncounterRowUiModel> =
    groupBy { it.variantSlug }
        .map { (_, rows) ->
            // Summed within a state, compared across them: rows sharing a condition set are slots of
            // one table and add up, rows in different states are alternatives and the best wins.
            val perState =
                states?.map { state -> rows.filter { row -> state.containsAll(row.conditions) }.sumOf { it.chance } }

            val chance =
                perState?.maxOrNull()
                    ?: rows.groupBy { it.conditions.sorted() }.values.maxOf { slots -> slots.sumOf { it.chance } }

            // Past the cap there is nothing to compare, so the question becomes whether the variant is
            // conditioned at all -- which is the same answer wherever it is not.
            val isBestCase =
                perState?.let { it.distinct().size > 1 } ?: rows.any { it.conditions.isNotEmpty() }

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

// Enough for every table in this dataset except a handful of Generation VIII ones, which combine
// weather with story progress. Walking a few hundred states to decide a label is cheap; walking
// thousands on every recomposition is not.
private const val STATE_CAP = 256
