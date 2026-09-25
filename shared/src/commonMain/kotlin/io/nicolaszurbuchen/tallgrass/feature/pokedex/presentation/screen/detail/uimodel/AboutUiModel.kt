package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel

import androidx.compose.runtime.Immutable
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * The About tab.
 *
 * **Breeding is the species' and training is not, which is why the two blocks are separate.** Egg
 * groups, gender and hatching are true of Vulpix whichever region it is from; the measurements and
 * the two training figures belong to the form on screen — Charizard is worth 267 experience and
 * Mega Charizard X 285, and where the ordinary form yields three Special Attack the Mega yields
 * three Attack. See #5 and #33.
 *
 * [evYieldText] and [baseExperienceText] are null for the 49 forms upstream has not costed, and the
 * rows are left out rather than drawn empty: a Pokémon worth nothing is not a fact this screen has.
 */
@Immutable
data class AboutUiModel(
    val heightText: UiText,
    val weightText: UiText,
    val gender: GenderUiModel,
    val eggGroupsText: UiText,
    val eggCycleText: UiText,
    val evYieldText: UiText?,
    val baseExperienceText: UiText?,
    val growthText: UiText,
)
