package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMove
import io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.VariantMoveUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_move_level

/**
 * One row of the Moves tab.
 *
 * The level wins over the method wherever there is one, because it is the more specific answer to the
 * same question: "Lv 46" already says the move is learned by levelling. The 160 level-up rows with no
 * level fall back to the method, which is what they mean -- the Pokemon knows it without being
 * taught.
 *
 * The same rule the Learned by tab on a move uses, deliberately: the two lists are the same rows read
 * from opposite ends, and a reader who has seen one should not have to learn the other.
 */
fun VariantMove.toUiModel(): VariantMoveUiModel =
    VariantMoveUiModel(
        slug = slug,
        name = name,
        type = type.toUiModel(),
        damageClass = damageClass.toUiModel(),
        powerText = power?.toString() ?: NO_POWER,
        howText =
            level?.let { UiText.Resource(Res.string.pokedex_detail_move_level, listOf(it)) }
                ?: UiText.Raw(method.toUiModel().label),
    )

// An em dash rather than a zero, as everywhere else a move's power is drawn.
private const val NO_POWER = "—"
