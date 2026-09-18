package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeMatchup
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatBarUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.StatsUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.TypeMatchupUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_stat_attack
import tallgrass.shared.generated.resources.pokedex_detail_stat_defense
import tallgrass.shared.generated.resources.pokedex_detail_stat_hp
import tallgrass.shared.generated.resources.pokedex_detail_stat_special_attack
import tallgrass.shared.generated.resources.pokedex_detail_stat_special_defense
import tallgrass.shared.generated.resources.pokedex_detail_stat_speed

fun PokemonVariant.toStatsUiModel(matchups: List<TypeMatchup>): StatsUiModel {
    val bar = { label: UiText, value: Int ->
        StatBarUiModel(
            label = label,
            valueText = value.toString(),
            fraction = (value.toFloat() / FULL_BAR).coerceAtMost(1f),
        )
    }

    return StatsUiModel(
        bars =
            listOf(
                bar(UiText.Resource(Res.string.pokedex_detail_stat_hp), stats.hp),
                bar(UiText.Resource(Res.string.pokedex_detail_stat_attack), stats.attack),
                bar(UiText.Resource(Res.string.pokedex_detail_stat_defense), stats.defense),
                bar(UiText.Resource(Res.string.pokedex_detail_stat_special_attack), stats.specialAttack),
                bar(UiText.Resource(Res.string.pokedex_detail_stat_special_defense), stats.specialDefense),
                bar(UiText.Resource(Res.string.pokedex_detail_stat_speed), stats.speed),
            ),
        totalText = stats.total.toString(),
        matchups =
            matchups.map { matchup ->
                val type = matchup.attackingType.toUiModel()

                TypeMatchupUiModel(
                    typeLabel = type.label,
                    typeColor = type.color,
                    factorText = FACTOR_LABELS[matchup.factorPercent] ?: "×${matchup.factorPercent / NEUTRAL_PERCENT}",
                )
            },
    )
}

// A full bar at 160 rather than at 255, the real maximum: only Blissey's HP comes near 255, and
// scaling every bar to it leaves the ordinary range squashed into the left third where the
// differences the tab exists to show stop being visible. Anything above it is drawn full.
private const val FULL_BAR = 160f

private const val NEUTRAL_PERCENT = 100

// The five factors the chart can produce. Written as fractions rather than as "x0.25", which is how
// the games write them and is shorter in a row of eighteen chips.
private val FACTOR_LABELS =
    mapOf(
        0 to "0",
        25 to "¼",
        50 to "½",
        200 to "×2",
        400 to "×4",
    )
