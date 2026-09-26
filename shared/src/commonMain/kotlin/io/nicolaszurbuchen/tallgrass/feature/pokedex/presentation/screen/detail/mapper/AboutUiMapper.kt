package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EvYield
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
import tallgrass.shared.generated.resources.pokedex_detail_stat_attack
import tallgrass.shared.generated.resources.pokedex_detail_stat_defense
import tallgrass.shared.generated.resources.pokedex_detail_stat_hp
import tallgrass.shared.generated.resources.pokedex_detail_stat_special_attack
import tallgrass.shared.generated.resources.pokedex_detail_stat_special_defense
import tallgrass.shared.generated.resources.pokedex_detail_stat_speed
import tallgrass.shared.generated.resources.pokedex_detail_weight_value

/**
 * The species is the receiver and the form is a parameter, because that is the shape of the tab:
 * breeding is the species', and the measurements and the two training figures are the form's. See
 * #5, and `AboutUiModel` for why the split falls where it does.
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

    // "2 Sp. Atk, 1 Sp. Def" -- only the stats the form awards, in the order the Stats tab lists
    // them. Naming the four it awards nothing against would bury the one or two that are the answer.
    //
    // A Composite rather than a format: each stat's name is a resource of its own, and resolving one
    // to pass it as another's argument is a Composable call. The same reasoning as `withChance` in
    // MoveContentUiMapper.
    val awards = { evYield: EvYield ->
        UiText.Composite(
            listOf(
                Res.string.pokedex_detail_stat_hp to evYield.hp,
                Res.string.pokedex_detail_stat_attack to evYield.attack,
                Res.string.pokedex_detail_stat_defense to evYield.defense,
                Res.string.pokedex_detail_stat_special_attack to evYield.specialAttack,
                Res.string.pokedex_detail_stat_special_defense to evYield.specialDefense,
                Res.string.pokedex_detail_stat_speed to evYield.speed,
            ).filter { (_, amount) -> amount > 0 }
                .flatMapIndexed { index, (label, amount) ->
                    listOfNotNull(
                        UiText.Raw(SEPARATOR).takeIf { index > 0 },
                        UiText.Raw("$amount "),
                        UiText.Resource(label),
                    )
                },
        )
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
        evYieldText = variant.evYield?.let(awards),
        baseExperienceText = variant.baseExperience?.let { UiText.Raw(it.toString()) },
        growthText = UiText.Raw(growthRate.toUiModel().label),
    )
}

private const val EIGHTHS = 8
private const val TENTHS_OF_A_PERCENT_PER_EIGHTH = 125

// Between one award and the next. Two of them at most, since no Pokemon yields against more than
// three stats.
private const val SEPARATOR = ", "
