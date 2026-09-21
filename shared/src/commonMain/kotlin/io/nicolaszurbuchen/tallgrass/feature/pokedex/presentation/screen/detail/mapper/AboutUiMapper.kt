package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonSpecies
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.pokemon.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.AboutUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.GenderUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_egg_cycle_value
import tallgrass.shared.generated.resources.pokedex_detail_gender_share
import tallgrass.shared.generated.resources.pokedex_detail_height_value
import tallgrass.shared.generated.resources.pokedex_detail_weight_value

/**
 * The species is the receiver and the form is a parameter, because that is the shape of the tab:
 * only height and weight move when the switcher is used. Everything below them is breeding and
 * training, which are true of Vulpix whichever region it is from. See #5.
 */
fun PokemonSpecies.toAboutUiModel(variant: PokemonVariant): AboutUiModel {
    // Decimetres and hectograms are already tenths of the unit each one is shown in.
    val fromTenths = { value: Int -> "${value / 10}.${value % 10}" }

    // An eighth is 12.5%, which no integer percentage can say, so the share is counted in tenths of
    // a percent and split at the end.
    val share = { eighths: Int ->
        val tenths = eighths * TENTHS_OF_A_PERCENT_PER_EIGHTH
        if (tenths % 10 == 0) "${tenths / 10}" else "${tenths / 10}.${tenths % 10}"
    }

    return AboutUiModel(
        heightText = UiText.Resource(Res.string.pokedex_detail_height_value, listOf(fromTenths(variant.height))),
        weightText = UiText.Resource(Res.string.pokedex_detail_weight_value, listOf(fromTenths(variant.weight))),
        gender =
            if (genderRate < 0) {
                GenderUiModel.Genderless
            } else {
                GenderUiModel.Split(
                    maleText = UiText.Resource(Res.string.pokedex_detail_gender_share, listOf(share(EIGHTHS - genderRate))),
                    femaleText = UiText.Resource(Res.string.pokedex_detail_gender_share, listOf(share(genderRate))),
                )
            },
        eggGroupsText = UiText.Raw(eggGroups.joinToString { it.toUiModel().label }),
        eggCycleText = UiText.Resource(Res.string.pokedex_detail_egg_cycle_value, listOf(hatchCounter.toString())),
        growthText = UiText.Raw(growthRate.toUiModel().label),
    )
}

private const val EIGHTHS = 8
private const val TENTHS_OF_A_PERCENT_PER_EIGHTH = 125
