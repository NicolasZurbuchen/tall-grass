package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveDetail
import io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveBarUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveFactUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import org.jetbrains.compose.resources.StringResource
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.move_detail_accuracy
import tallgrass.shared.generated.resources.move_detail_certain_suffix
import tallgrass.shared.generated.resources.move_detail_chance
import tallgrass.shared.generated.resources.move_detail_chance_suffix
import tallgrass.shared.generated.resources.move_detail_condition
import tallgrass.shared.generated.resources.move_detail_crit_rate
import tallgrass.shared.generated.resources.move_detail_crit_stages
import tallgrass.shared.generated.resources.move_detail_crit_stages_plural
import tallgrass.shared.generated.resources.move_detail_drain
import tallgrass.shared.generated.resources.move_detail_flinch
import tallgrass.shared.generated.resources.move_detail_healing
import tallgrass.shared.generated.resources.move_detail_hits
import tallgrass.shared.generated.resources.move_detail_hp_cost
import tallgrass.shared.generated.resources.move_detail_kind
import tallgrass.shared.generated.resources.move_detail_power
import tallgrass.shared.generated.resources.move_detail_pp
import tallgrass.shared.generated.resources.move_detail_pp_value
import tallgrass.shared.generated.resources.move_detail_recoil
import tallgrass.shared.generated.resources.move_detail_share_of_damage
import tallgrass.shared.generated.resources.move_detail_share_of_max_hp
import tallgrass.shared.generated.resources.move_detail_stat_stage
import tallgrass.shared.generated.resources.move_detail_stat_stages
import tallgrass.shared.generated.resources.move_detail_times
import tallgrass.shared.generated.resources.move_detail_times_range
import tallgrass.shared.generated.resources.move_detail_turns
import tallgrass.shared.generated.resources.move_detail_turns_range
import tallgrass.shared.generated.resources.move_detail_turns_value
import kotlin.math.abs

/**
 * One move, ready to draw.
 *
 * The helpers below are local rather than private top-level functions, on the precedent of
 * `StatsUiMapper`: a mapper file holds the crossing and nothing else, so anything that is not itself
 * a mapping lives inside the function that uses it.
 */
fun MoveDetail.toUiModel(): MoveContentUiModel {
    val signed = { value: Int -> if (value > 0) "+$value" else value.toString() }
    val fraction = { value: Int?, full: Int -> ((value ?: 0).toFloat() / full).coerceIn(0f, 1f) }

    return MoveContentUiModel(
        name = name,
        type = type.toUiModel(),
        damageClass = damageClass.toUiModel(),
        ppText = pp?.let { UiText.Resource(Res.string.move_detail_pp_value, listOf(it)) },
        bars =
            listOf(
                MoveBarUiModel(
                    label = UiText.Resource(Res.string.move_detail_power),
                    valueText = power?.toString() ?: ABSENT,
                    fraction = fraction(power, POWER_FULL),
                ),
                MoveBarUiModel(
                    label = UiText.Resource(Res.string.move_detail_accuracy),
                    valueText = accuracy?.let { "$it$PERCENT" } ?: ABSENT,
                    fraction = fraction(accuracy, ACCURACY_FULL),
                ),
                MoveBarUiModel(
                    label = UiText.Resource(Res.string.move_detail_pp),
                    valueText = pp?.toString() ?: ABSENT,
                    fraction = fraction(pp, PP_FULL),
                ),
            ),
        effect = effect,
        targetText = UiText.Raw(target.toUiModel().label),
        priorityText = signed(priority),
        facts = toFactsUiModel(signed),
    )
}

/**
 * The Mechanics block, built from whatever upstream actually recorded.
 *
 * Every entry is conditional, and that is the whole design: a move with no drain has no drain row
 * rather than a row saying zero, and the 92 moves with no meta produce an empty list that leaves the
 * section out. See `DECISIONS.md § A move's mechanical detail is null where there is nothing to say`.
 */
private fun MoveDetail.toFactsUiModel(signed: (Int) -> String): List<MoveFactUiModel> {
    val meta = this.meta ?: return emptyList()

    val fact = { label: StringResource, value: UiText -> MoveFactUiModel(UiText.Resource(label), value) }

    // **The one place the zero-means-always rule is spent.** A null chance beside a real effect means
    // the effect is certain rather than impossible, so it reads as "always" and never as 0%.
    //
    // A Composite rather than a wrapping format: substituting one format into another needs the inner
    // one resolved first, and resolving a string resource is a Composable call.
    val withChance = { value: UiText, chance: Int? ->
        val suffix =
            if (chance == null) {
                UiText.Resource(Res.string.move_detail_certain_suffix)
            } else {
                UiText.Resource(Res.string.move_detail_chance_suffix, listOf(chance))
            }

        UiText.Composite(listOf(value, suffix))
    }

    return buildList {
        add(fact(Res.string.move_detail_kind, UiText.Raw(meta.category.toUiModel().label)))

        meta.ailment?.let { ailment ->
            val condition = UiText.Raw(ailment.toUiModel().label)
            add(fact(Res.string.move_detail_condition, withChance(condition, meta.ailmentChance)))
        }

        meta.minHits?.let { min ->
            val max = meta.maxHits ?: min
            val value =
                if (min == max) {
                    UiText.Resource(Res.string.move_detail_times, listOf(min))
                } else {
                    UiText.Resource(Res.string.move_detail_times_range, listOf(min, max))
                }

            add(fact(Res.string.move_detail_hits, value))
        }

        meta.minTurns?.let { min ->
            val max = meta.maxTurns ?: min
            val value =
                if (min == max) {
                    UiText.Resource(Res.string.move_detail_turns_value, listOf(min))
                } else {
                    UiText.Resource(Res.string.move_detail_turns_range, listOf(min, max))
                }

            add(fact(Res.string.move_detail_turns, value))
        }

        // The sign picks the label rather than showing up in the figure. "Recoil 33% of the damage
        // dealt" says what "Drain -33%" means, and a reader should not have to work that out.
        meta.drain?.let { drain ->
            val label = if (drain > 0) Res.string.move_detail_drain else Res.string.move_detail_recoil
            add(fact(label, UiText.Resource(Res.string.move_detail_share_of_damage, listOf(abs(drain)))))
        }

        meta.healing?.let { healing ->
            val label = if (healing > 0) Res.string.move_detail_healing else Res.string.move_detail_hp_cost
            add(fact(label, UiText.Resource(Res.string.move_detail_share_of_max_hp, listOf(abs(healing)))))
        }

        meta.critRate?.let { stages ->
            val format =
                if (stages == 1) Res.string.move_detail_crit_stages else Res.string.move_detail_crit_stages_plural
            add(fact(Res.string.move_detail_crit_rate, UiText.Resource(format, listOf(signed(stages)))))
        }

        meta.flinchChance?.let { chance ->
            add(fact(Res.string.move_detail_flinch, UiText.Resource(Res.string.move_detail_chance, listOf(chance))))
        }

        // One row per stat rather than one row listing them all, so a stat reads down this block the
        // way it reads down a Pokemon's. Ancient Power moves five and each is a fact of its own.
        statChanges.forEach { change ->
            val format =
                if (abs(change.stages) == 1) Res.string.move_detail_stat_stage else Res.string.move_detail_stat_stages
            val stages = UiText.Resource(format, listOf(signed(change.stages)))

            add(MoveFactUiModel(UiText.Raw(change.stat.toUiModel().label), withChance(stages, meta.statChance)))
        }
    }
}

// An em dash rather than a zero, with an empty lane beside it.
private const val ABSENT = "—"

private const val PERCENT = "%"

// Full at 150 rather than at the 250 Explosion reaches. 882 of the 919 moves are at or below it, so
// scaling to the outlier would leave every move anyone actually compares in the bottom third of the
// lane. The stat bars on a Pokemon clamp at 160 for the same reason.
private const val POWER_FULL = 150

private const val ACCURACY_FULL = 100

// The most any move has.
private const val PP_FULL = 40
