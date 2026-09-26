package io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel

import androidx.compose.ui.graphics.Color

/**
 * The coloured marker beside a place in a region's list.
 *
 * Nine colours down a list of up to 128 rows, which is what the marker is for: a reader looking for
 * a cave scans for brown rather than reading every name. The colours are borrowed from the type
 * palette's neighbourhood on purpose, so the list sits in the same world as the rest of the app
 * without claiming a place has a type.
 *
 * [OTHER] is grey and is not a failure. The category is read out of the slug at generation time and
 * about one place in six lands here, so the neutral marker is an ordinary outcome rather than a
 * missing one.
 */
enum class LocationCategoryUiModel(
    val color: Color,
) {
    ROUTE(Color(0xFF3FAE72)),
    TOWN(Color(0xFF5BA7F7)),
    CAVE(Color(0xFFBFA967)),
    FOREST(Color(0xFF5BC98F)),
    WATER(Color(0xFF6ECBD2)),
    MOUNTAIN(Color(0xFF9E8B72)),
    BUILDING(Color(0xFFB368C9)),
    PARK(Color(0xFF9DB945)),
    OTHER(Color(0xFF8FA0B5)),
}
