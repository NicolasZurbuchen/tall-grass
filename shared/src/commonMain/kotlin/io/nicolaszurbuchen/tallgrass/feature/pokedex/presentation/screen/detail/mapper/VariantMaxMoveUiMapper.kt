package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMaxMove
import io.nicolaszurbuchen.tallgrass.core.move.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.VariantMaxMoveUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * One converted row.
 *
 * The sources are spelled out in full rather than counted. "From 6 moves" answers a question nobody
 * asked; the names are what let a reader see that the Fire moves they were looking for are still
 * there, under another name.
 */
fun VariantMaxMove.toUiModel(): VariantMaxMoveUiModel =
    VariantMaxMoveUiModel(
        slug = slug,
        name = name,
        type = type.toUiModel(),
        damageClass = damageClass.toUiModel(),
        powerText = power?.toString() ?: NO_POWER,
        sourcesText = UiText.Raw(sources.joinToString(SOURCE_SEPARATOR) { it.name }),
        isSignature = isSignature,
    )

// An em dash rather than a zero, as everywhere else a move's power is drawn. Max Guard is the one row
// that has it: it inflicts nothing, which is the point of it.
private const val NO_POWER = "—"

private const val SOURCE_SEPARATOR = " · "
