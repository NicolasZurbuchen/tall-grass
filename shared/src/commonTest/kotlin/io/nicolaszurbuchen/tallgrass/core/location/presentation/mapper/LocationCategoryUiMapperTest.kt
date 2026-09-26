package io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.LocationCategory
import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.LocationCategoryUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class LocationCategoryUiMapperTest {
    @Test
    fun everyCategory_hasAMarker() {
        // Exhaustive by construction, so this cannot fail without one of the enums having gained a
        // member the other has not -- which is what it is here to catch.
        assertEquals(LocationCategory.entries.size, LocationCategoryUiModel.entries.size)
        LocationCategory.entries.forEach { category ->
            assertEquals(category.name, category.toUiModel().name)
        }
    }

    @Test
    fun noTwoCategories_shareAColour() {
        val colors = LocationCategoryUiModel.entries.map { it.color }

        assertEquals(colors.size, colors.distinct().size)
    }
}
