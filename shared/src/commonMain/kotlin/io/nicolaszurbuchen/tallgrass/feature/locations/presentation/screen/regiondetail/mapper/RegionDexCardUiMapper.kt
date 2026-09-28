package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDexEntry
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.uimodel.RegionDexCardUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText

/**
 * The card draws the **National** Dex number, padded to three, which is what every other card in the
 * app draws. The regional entry number decides the order and is not shown: two numbers on one card
 * would leave a reader working out which is which, and the national one is the one they know.
 */
fun RegionDexEntry.toUiModel(): RegionDexCardUiModel =
    RegionDexCardUiModel(
        slug = slug,
        cardSlug = cardSlug,
        numberText = UiText.Raw("#" + dexNumber.toString().padStart(DEX_DIGITS, '0')),
        name = name,
        artworkUrl = artworkUrl,
        primaryType = primaryType.toUiModel(),
        secondaryType = secondaryType?.toUiModel(),
    )

// 1,025 species, so three digits is the widest a number gets.
private const val DEX_DIGITS = 3
