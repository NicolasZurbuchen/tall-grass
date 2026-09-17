package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toTypeUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper.toAboutUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper.toFormPillUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper.toStatsUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailTabUiModel

fun DetailState.toUiModel(hero: HeroHandoff): DetailUiModel {
    val variant = detail?.variants?.firstOrNull { it.slug == activeVariantSlug }

    return DetailUiModel(
        isLoading = isLoading,
        error = error?.toUiModel(),
        artworkUrl = variant?.artworkUrl ?: hero.artworkUrl,
        // Keyed by the form on screen rather than by the one that was tapped. The two are the same
        // until the switcher is used, and after that the key matches no card -- which is the point.
        // See DetailUiModel.
        artworkKey = hero.sharedElementKey.copy(id = activeVariantSlug),
        // The handoff's type until the read lands, so the hero never changes colour under the
        // reader. Its slug fails to parse only if a saved destination outlived the build that wrote
        // it, and a grey-blue hero for a few milliseconds is the whole cost of being wrong.
        tint = (variant?.primaryType?.toUiModel() ?: hero.primaryTypeSlug.toTypeUiModel() ?: TypeUiModel.NORMAL).color,
        content =
            if (detail == null || variant == null) {
                null
            } else {
                DetailContentUiModel(
                    name = variant.name,
                    numberText = "#" + detail.species.dexNumber.toString().padStart(DEX_NUMBER_DIGITS, '0'),
                    genusText = detail.species.genus,
                    types = listOfNotNull(variant.primaryType, variant.secondaryType).map { it.toUiModel() },
                    // A species with one form has nothing to switch between.
                    forms =
                        if (detail.variants.size > 1) {
                            detail.variants.map { it.toFormPillUiModel(detail.species.name) }
                        } else {
                            emptyList()
                        },
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
