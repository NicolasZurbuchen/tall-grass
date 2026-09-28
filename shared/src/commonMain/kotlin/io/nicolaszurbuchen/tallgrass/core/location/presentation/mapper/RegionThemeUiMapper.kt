package io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.RegionThemeUiModel

/**
 * The crossing from the dataset's spelling, for callers holding a slug rather than a region.
 *
 * A slug rather than a domain type, because a region is a row and not an enum: there are eleven of
 * them only until there are twelve, and the dataset would carry the twelfth before this file did.
 */
fun String.toRegionThemeUiModel(): RegionThemeUiModel? = RegionThemeUiModel.fromSlug(this)
