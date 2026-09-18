package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.FormPillUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_form_default

/**
 * A pill says what makes this form different, and nothing else.
 *
 * Upstream's label repeats the species: Charizard's forms are labelled "Mega Charizard X" and
 * "Gigantamax Form", which in a row under the name Charizard reads as three Charizards. Taking the
 * species name and the two filler suffixes out leaves "Mega X" and "Gigantamax" — and, for Arceus,
 * "Normal" through "Fairy", which is the whole content of its eighteen pills.
 *
 * A form with no label is the ordinary one, except that it is not always: Partner Pikachu and every
 * Totem form are unlabelled and not default, so they fall back to their own name rather than to
 * "Standard".
 */
fun PokemonVariant.toFormPillUiModel(speciesName: String): FormPillUiModel {
    val stripped =
        formLabel
            ?.removeSuffix(FORM_SUFFIX)
            ?.removeSuffix(TYPE_SUFFIX)
            ?.replace(speciesName, "")
            ?.replace(DOUBLE_SPACE, " ")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    return FormPillUiModel(
        slug = slug,
        label =
            when {
                stripped != null -> UiText.Raw(stripped)
                isDefault -> UiText.Resource(Res.string.pokedex_detail_form_default)
                else -> UiText.Raw(name)
            },
    )
}

private const val FORM_SUFFIX = " Form"
private const val TYPE_SUFFIX = " Type"
private const val DOUBLE_SPACE = "  "
