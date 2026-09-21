package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MoveLearner
import io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail.uimodel.MoveLearnerUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.move_detail_level

/**
 * One Pokemon in the Learned by tab.
 *
 * The level wins over the method wherever there is one, because it is the more specific answer to the
 * same question: "Lv 46" already says the move is learned by levelling. The 160 level-up rows with no
 * level fall back to the method, which is what they mean — the Pokemon knows it without being taught.
 */
fun MoveLearner.toUiModel(): MoveLearnerUiModel =
    MoveLearnerUiModel(
        slug = variantSlug,
        name = name,
        artworkUrl = artworkUrl,
        tint = primaryType.toUiModel(),
        howText =
            level?.let { UiText.Resource(Res.string.move_detail_level, listOf(it)) }
                ?: UiText.Raw(method.toUiModel().label),
    )
