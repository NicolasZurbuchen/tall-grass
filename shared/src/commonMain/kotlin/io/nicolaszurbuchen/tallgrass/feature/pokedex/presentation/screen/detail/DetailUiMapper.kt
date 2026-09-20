package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import io.nicolaszurbuchen.tallgrass.core.error.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toTypeUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.mapper.toUiModel
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper.toAboutUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper.toFormPillsUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper.toHeroUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper.toStatsUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailContentUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailHeroUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailTabUiModel

fun DetailState.toUiModel(hero: HeroHandoff): DetailUiModel {
    val variant = detail?.variants?.firstOrNull { it.slug == activeVariantSlug }
    val entry = entries.firstOrNull { it.slug == activeEntrySlug }

    // The form on screen if it has been read, the card the carousel is on if it has not, and the
    // handoff before even that. The handoff's slug fails to parse only if a saved destination
    // outlived the build that wrote it, and a grey-blue hero for a few milliseconds is the whole
    // cost of being wrong.
    val types =
        when {
            variant != null -> {
                listOfNotNull(variant.primaryType, variant.secondaryType).map { it.toUiModel() }
            }

            entry != null -> {
                listOfNotNull(entry.primaryType, entry.secondaryType).map { it.toUiModel() }
            }

            else -> {
                listOfNotNull(
                    hero.primaryTypeSlug.toTypeUiModel() ?: TypeUiModel.NORMAL,
                    hero.secondaryTypeSlug?.toTypeUiModel(),
                )
            }
        }

    val tint = types.first().color
    val artworkUrl = variant?.artworkUrl ?: entry?.artworkUrl ?: hero.artworkUrl
    val dexNumber = entry?.dexNumber ?: detail?.species?.dexNumber

    // The tapped card's key, and only while the carousel and the switcher are both still on it.
    // Anywhere else it would name a card in a grid the reader has already swiped away from.
    val heroKey =
        hero.sharedElementKey
            .takeIf { activeEntrySlug == entryVariantSlug }
            ?.copy(id = activeVariantSlug)

    return DetailUiModel(
        isLoading = isLoading,
        error = error?.toUiModel(),
        name = variant?.name ?: entry?.name ?: hero.name,
        // Blank rather than wrong in the frame before the carousel's list has been read.
        numberText = dexNumber?.let { "#" + it.toString().padStart(DEX_NUMBER_DIGITS, '0') }.orEmpty(),
        types = types,
        tint = tint,
        // One card until the list lands, so the carousel is never empty and the hero never waits.
        heroes =
            if (entries.isEmpty()) {
                listOf(DetailHeroUiModel(activeEntrySlug, artworkUrl, tint, heroKey))
            } else {
                entries.map { candidate ->
                    if (candidate.slug == activeEntrySlug) {
                        candidate.toHeroUiModel(artworkUrl = artworkUrl, tint = tint, artworkKey = heroKey)
                    } else {
                        candidate.toHeroUiModel()
                    }
                }
            },
        activeIndex = entries.indexOfFirst { it.slug == activeEntrySlug }.coerceAtLeast(0),
        content =
            if (detail == null || variant == null) {
                null
            } else {
                DetailContentUiModel(
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
