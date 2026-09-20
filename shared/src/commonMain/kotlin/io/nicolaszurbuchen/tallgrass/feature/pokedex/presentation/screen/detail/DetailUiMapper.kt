package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toTypeUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper.toAboutUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper.toFormPillsUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper.toStatsUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailTabUiModel

fun DetailState.toUiModel(hero: HeroHandoff): DetailUiModel {
    val variant = detail?.variants?.firstOrNull { it.slug == activeVariantSlug }

    // The handoff's answer until the read lands, so nothing in the hero changes under the reader.
    // A slug fails to parse only if a saved destination outlived the build that wrote it, and a
    // grey-blue hero for a few milliseconds is the whole cost of being wrong.
    val types =
        if (variant == null) {
            listOfNotNull(
                hero.primaryTypeSlug.toTypeUiModel() ?: TypeUiModel.NORMAL,
                hero.secondaryTypeSlug?.toTypeUiModel(),
            )
        } else {
            listOfNotNull(variant.primaryType, variant.secondaryType).map { it.toUiModel() }
        }

    return DetailUiModel(
        isLoading = isLoading,
        error = error?.toUiModel(),
        name = variant?.name ?: hero.name,
        types = types,
        artworkUrl = variant?.artworkUrl ?: hero.artworkUrl,
        // Keyed by the form on screen rather than by the one that was tapped. The two are the same
        // until the switcher is used, and after that the key matches no card -- which is the point.
        // See DetailUiModel.
        artworkKey = hero.sharedElementKey.copy(id = activeVariantSlug),
        tint = types.first().color,
        content =
            if (detail == null || variant == null) {
                null
            } else {
                DetailContentUiModel(
                    numberText = "#" + detail.species.dexNumber.toString().padStart(DEX_NUMBER_DIGITS, '0'),
                    genusText = detail.species.genus,
                    forms = detail.variants.toFormPillsUiModel(detail.species.name),
                    activeFormSlug = variant.slug,
                    tab =
                        when (tab) {
                            DetailState.Tab.ABOUT -> DetailTabUiModel.ABOUT
                            DetailState.Tab.STATS -> DetailTabUiModel.STATS
                        },
                    about = detail.species.toAboutUiModel(variant),
                    stats = variant.toStatsUiModel(matchups[variant.slug].orEmpty()),
                )
            },
    )
}

// Three, as on the cards. See DexUiMapper.
private const val DEX_NUMBER_DIGITS = 3
