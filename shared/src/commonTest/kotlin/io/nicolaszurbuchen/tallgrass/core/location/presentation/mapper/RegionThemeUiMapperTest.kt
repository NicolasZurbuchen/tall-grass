package io.nicolaszurbuchen.tallgrass.core.location.presentation.mapper

import io.nicolaszurbuchen.tallgrass.core.location.presentation.uimodel.RegionThemeUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RegionThemeUiMapperTest {
    @Test
    fun everyRegionInTheDataset_hasAColour() {
        // The ten the dataset ships. Orre is not among them: it exists only in Colosseum and XD,
        // which are spin-offs. An eleventh is the day Game Freak makes one, and the null below is
        // what happens then.
        val shipped =
            listOf("kanto", "johto", "hoenn", "sinnoh", "unova", "kalos", "alola", "galar", "hisui", "paldea")

        assertEquals(shipped.size, RegionThemeUiModel.entries.size)
        shipped.forEach { slug -> assertEquals(slug, slug.toRegionThemeUiModel()?.slug) }
    }

    @Test
    fun anUnknownRegion_hasNoColourRatherThanABorrowedOne() {
        // Null, so the card draws on the theme's own surface. Falling back to another region's
        // colour would make a twelfth region look like Kanto.
        assertNull("somewhere-new".toRegionThemeUiModel())
    }

    @Test
    fun noTwoRegions_shareAColour() {
        // The colours exist to be told apart down a list of eleven, so a repeat would be a bug
        // rather than a coincidence.
        val colors = RegionThemeUiModel.entries.map { it.color }

        assertEquals(colors.size, colors.distinct().size)
    }
}
