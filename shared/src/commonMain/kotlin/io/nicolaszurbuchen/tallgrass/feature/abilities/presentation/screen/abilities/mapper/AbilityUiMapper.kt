package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.AbilityUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.abilities_generation

/**
 * The initial is taken from the name rather than the slug: they agree for all 314, and the name is
 * what the reader is scanning. Uppercased because a card's tile is a display of the letter rather
 * than the first character of a word -- upstream's names are already capitalised, so this changes
 * nothing today and stops a lowercase one looking like a typo if that ever stops being true.
 */
fun Ability.toUiModel(): AbilityUiModel =
    AbilityUiModel(
        slug = slug,
        name = name,
        initial = name.take(1).uppercase(),
        generationText = UiText.Resource(Res.string.abilities_generation, listOf(generation)),
        shortEffect = shortEffect,
    )
