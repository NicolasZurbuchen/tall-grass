package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.FormKind
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.FormPillUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_form_default

/**
 * The forms worth switching between, which is not all of them.
 *
 * Cosmetic forms are left out. Pikachu has fourteen — Rock Star, Pop Star, Ph.D., Libre and eight
 * hats — and every one of them has Pikachu's types, stats and abilities exactly. A row of eighteen
 * pills that all show the same numbers is a worse screen than no row at all, and it buries the two
 * that do change something.
 *
 * Empty when one form survives, because there is then nothing to switch between. That is most
 * species, and also Koraidon and Miraidon, whose four ride builds are all cosmetic.
 */
fun List<PokemonVariant>.toFormPillsUiModel(speciesName: String): List<FormPillUiModel> {
    val switchable = filterNot { it.formKind == FormKind.COSMETIC }

    return if (switchable.size > 1) switchable.map { it.toFormPillUiModel(speciesName) } else emptyList()
}

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
