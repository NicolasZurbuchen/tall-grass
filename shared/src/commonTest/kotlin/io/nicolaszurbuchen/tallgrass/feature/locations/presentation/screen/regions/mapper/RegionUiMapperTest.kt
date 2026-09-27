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
    fun card_carriesOnlyWhatFitsAtHalfWidth() {
        // Two columns, so the card is a name and two facts. The Japanese name is not among them any
        // more -- there is no room for a third line beside artwork this size, and the About tab has it.
        val card = LocationFixtures.kanto.toUiModel()

        assertEquals("Kanto", card.name)
        assertNotNull(card.locationsText)
    }

    @Test
    fun card_carriesTheBoxArtInTheOrderItWasGiven() {
        // The pair overlaps, so which one is in front is the order rather than a choice the card
        // makes.
        assertEquals(listOf("charizard.png", "blastoise.png"), LocationFixtures.kanto.toUiModel().boxArt)
    }
}
