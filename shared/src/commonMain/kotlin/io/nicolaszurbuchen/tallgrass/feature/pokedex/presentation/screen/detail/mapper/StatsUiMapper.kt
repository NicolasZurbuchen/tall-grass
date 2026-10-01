package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.FormKind
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.StatRange
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeMatchup
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.MatchupGroupUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatBarUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatsUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.TypeMatchupUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_dynamax_hp
import tallgrass.shared.generated.resources.pokedex_detail_stat_attack
import tallgrass.shared.generated.resources.pokedex_detail_stat_defense
import tallgrass.shared.generated.resources.pokedex_detail_stat_hp
import tallgrass.shared.generated.resources.pokedex_detail_stat_special_attack
import tallgrass.shared.generated.resources.pokedex_detail_stat_special_defense
import tallgrass.shared.generated.resources.pokedex_detail_stat_speed

fun PokemonVariant.toStatsUiModel(matchups: List<TypeMatchup>): StatsUiModel {
    val bar = { label: UiText, value: Int, range: StatRange ->
        StatBarUiModel(
            label = label,
            valueText = value.toString(),
            fraction = (value.toFloat() / FULL_BAR).coerceAtMost(1f),
            minText = range.min.toString(),
            maxText = range.max.toString(),
        )
    }

    // Named rather than inlined into the bars, because the total row adds them up and parsing the
    // figures back out of the strings it just wrote would be the alternative.
    //
    // HP takes its own formula and no nature, which is why it is the one asked differently rather
    // than the one with a flag.
    val hp = StatRange.ofHp(stats.hp)
    val attack = StatRange.of(stats.attack)
    val defense = StatRange.of(stats.defense)
    val specialAttack = StatRange.of(stats.specialAttack)
    val specialDefense = StatRange.of(stats.specialDefense)
    val speed = StatRange.of(stats.speed)
    val ranges = listOf(hp, attack, defense, specialAttack, specialDefense, speed)

    return StatsUiModel(
        bars =
            listOf(
                bar(UiText.Resource(Res.string.pokedex_detail_stat_hp), stats.hp, hp),
                bar(UiText.Resource(Res.string.pokedex_detail_stat_attack), stats.attack, attack),
                bar(UiText.Resource(Res.string.pokedex_detail_stat_defense), stats.defense, defense),
                bar(UiText.Resource(Res.string.pokedex_detail_stat_special_attack), stats.specialAttack, specialAttack),
                bar(UiText.Resource(Res.string.pokedex_detail_stat_special_defense), stats.specialDefense, specialDefense),
                bar(UiText.Resource(Res.string.pokedex_detail_stat_speed), stats.speed, speed),
            ),
        totalText = stats.total.toString(),
        totalFraction = (stats.total.toFloat() / (FULL_BAR * STAT_COUNT)).coerceAtMost(1f),
        totalMinText = ranges.sumOf { it.min }.toString(),
        totalMaxText = ranges.sumOf { it.max }.toString(),
        // **The one battle figure on a screen of base stats.** Dynamaxing multiplies the HP the
        // Pokemon already has, so the pair is the level 100 band doubled rather than anything to do
        // with the base stat beside it -- which is also why it is a sentence under the table rather
        // than a column in it.
        //
        // Shown on the Gigantamax forms alone, where a reader is already asking what the form does.
        // Every Pokemon in Sword and Shield can Dynamax, so this is the narrower of two true things.
        //
        // The form kind rather than the form's G-Max Move, which is a learnset row and not something
        // a Stats mapper has any business reading.
        dynamaxHpText =
            if (formKind == FormKind.GIGANTAMAX) {
                UiText.Resource(
                    Res.string.pokedex_detail_dynamax_hp,
                    listOf(hp.min * MAX_DYNAMAX_MULTIPLIER, hp.max * MAX_DYNAMAX_MULTIPLIER),
                )
            } else {
                null
            },
        weaknesses = WEAKENING_FACTORS.toMatchupGroupsUiModel(matchups),
        resistances = RESISTING_FACTORS.toMatchupGroupsUiModel(matchups),
    )
}

/**
 * The factors in this order, each with the types that hit for it, and nothing for a factor no type
 * hits this defender for.
 *
 * The receiver is the order rather than the data, which is what makes the two calls above read as
 * the two halves of one question.
 */
private fun List<Int>.toMatchupGroupsUiModel(matchups: List<TypeMatchup>): List<MatchupGroupUiModel> {
    val byFactor = matchups.groupBy { it.factorPercent }

    return mapNotNull { factor ->
        byFactor[factor]?.let { rows ->
            MatchupGroupUiModel(
                factorText = FACTOR_LABELS.getValue(factor),
                types =
                    rows.map { matchup ->
                        val type = matchup.attackingType.toUiModel()

                        TypeMatchupUiModel(typeLabel = type.label, typeColor = type.color)
                    },
            )
        }
    }
}

// x1.5 at Dynamax Level 0, rising a twentieth per level to x2 at Level 10. Only the ceiling is a
// figure here; the floor is in the string, because it is a fact about the scale rather than about
// the Pokemon.
private const val MAX_DYNAMAX_MULTIPLIER = 2

// A full bar at 160 rather than at 255, the real maximum: only Blissey's HP comes near 255, and
// scaling every bar to it leaves the ordinary range squashed into the left third where the
// differences the tab exists to show stop being visible. Anything above it is drawn full.
private const val FULL_BAR = 160f

// The six a variant has. Named rather than read off the list, because the list is built right here
// and reading its size to scale itself is a circle a reader has to unwind.
private const val STAT_COUNT = 6

// The five factors the chart can produce, and the whole of them -- see `TypeMatchup`, where a
// neutral matchup is not a matchup and is absent. `getValue` rather than a fallback, because a sixth
// would be a bug in the chart rather than a label this file should invent a spelling for.
//
// Written as fractions rather than as "x0.25", which is how the games write them.
private val FACTOR_LABELS =
    mapOf(
        0 to "0",
        25 to "¼",
        50 to "½",
        200 to "×2",
        400 to "×4",
    )

// Lightest first in each, which is the order they were asked for.
private val WEAKENING_FACTORS = listOf(200, 400)
private val RESISTING_FACTORS = listOf(0, 25, 50)
