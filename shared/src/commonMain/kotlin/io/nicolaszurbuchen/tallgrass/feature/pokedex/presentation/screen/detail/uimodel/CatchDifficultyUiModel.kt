package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import org.jetbrains.compose.resources.StringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_catch_easy
import tallgrass.shared.generated.resources.pokedex_detail_catch_hard
import tallgrass.shared.generated.resources.pokedex_detail_catch_moderate
import tallgrass.shared.generated.resources.pokedex_detail_catch_very_easy
import tallgrass.shared.generated.resources.pokedex_detail_catch_very_hard

/**
 * A word for the catch rate, because the figure alone says nothing.
 *
 * #43's second criterion is that the value be legible without knowing 255 is the maximum. Both halves
 * ship -- the figure for anyone who knows the scale and the word for everyone else -- because the
 * figure is the fact and the word is only a reading of it.
 *
 * Five bands rather than a percentage, because the scale is not linear in the thing a reader cares
 * about: the games' catch formula makes 255 and 200 feel identical and 3 and 30 feel nothing alike.
 */
enum class CatchDifficultyUiModel(
    val label: StringResource,
) {
    VERY_EASY(Res.string.pokedex_detail_catch_very_easy),
    EASY(Res.string.pokedex_detail_catch_easy),
    MODERATE(Res.string.pokedex_detail_catch_moderate),
    HARD(Res.string.pokedex_detail_catch_hard),
    VERY_HARD(Res.string.pokedex_detail_catch_very_hard),
}
