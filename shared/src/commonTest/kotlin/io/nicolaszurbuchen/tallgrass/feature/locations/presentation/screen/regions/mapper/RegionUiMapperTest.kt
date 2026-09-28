package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.fake.LocationFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RegionUiMapperTest {
    @Test
    fun card_carriesTheRegionsOwnColour() {
        assertNotNull(LocationFixtures.kanto.toUiModel().color)
    }

    @Test
    fun card_survivesARegionThisBuildHasNoColourFor() {
        // Null rather than dropped: there is still a whole region to draw, and the card falls back
        // to the theme's surface.
        val twelfth = LocationFixtures.kanto.copy(slug = "somewhere-new")

        assertNull(twelfth.toUiModel().color)
        assertEquals("Kanto", twelfth.toUiModel().name)
    }

    @Test
    fun card_keepsTheMissingJapaneseNameMissing() {
        // Orre, and only Orre. The card leaves the line out rather than substituting the English
        // name, which would read as a translation that had not been done.
        assertNull(LocationFixtures.orre.toUiModel().nativeName)
        assertEquals("カントー", LocationFixtures.kanto.toUiModel().nativeName)
    }

    @Test
    fun card_carriesTheBoxArtInTheOrderItWasGiven() {
        // The pair overlaps, so which one is in front is the order rather than a choice the card
        // makes.
        assertEquals(listOf("charizard.png", "blastoise.png"), LocationFixtures.kanto.toUiModel().boxArt)
    }
}
