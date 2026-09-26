package io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationCategory
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.LocationCategoryUiModel

/**
 * Exhaustive by construction, the same way the type mapper is: both enums list the same nine members,
 * so a tenth category breaks this at compile time rather than leaving a row with no marker.
 */
fun LocationCategory.toUiModel(): LocationCategoryUiModel =
    when (this) {
        LocationCategory.ROUTE -> LocationCategoryUiModel.ROUTE
        LocationCategory.TOWN -> LocationCategoryUiModel.TOWN
        LocationCategory.CAVE -> LocationCategoryUiModel.CAVE
        LocationCategory.FOREST -> LocationCategoryUiModel.FOREST
        LocationCategory.WATER -> LocationCategoryUiModel.WATER
        LocationCategory.MOUNTAIN -> LocationCategoryUiModel.MOUNTAIN
        LocationCategory.BUILDING -> LocationCategoryUiModel.BUILDING
        LocationCategory.PARK -> LocationCategoryUiModel.PARK
        LocationCategory.OTHER -> LocationCategoryUiModel.OTHER
    }
