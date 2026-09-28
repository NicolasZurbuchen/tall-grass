package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail.mapper

import io.nicolaszurbuchen.tallgrass.core.location.domain.model.RegionDexEntry
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RegionDexCardUiMapperTest {
    private val alolanVulpix =
        RegionDexEntry(
            slug = "vulpix-alola",
            cardSlug = "vulpix",
            number = 253,
            dexNumber = 37,
            name = "Vulpix",
            artworkUrl = "vulpix-alola.png",
            primaryType = PokemonType.ICE,
            secondaryType = null,
        )

    @Test
    fun card_drawsTheNationalNumberRatherThanTheRegionalOne() {
        // 37 and not 253. The regional number decides the order and is not shown: two numbers on one
        // card would leave a reader working out which is which.
        assertEquals(UiText.Raw("#037"), alolanVulpix.toUiModel().numberText)
    }

    @Test
    fun card_keepsTheFormAndTheDoorToItApart() {
        // A regional dex names forms that are not cards, so the two slugs differ and both matter:
        // one is what the region means, the other is what opens. See #5.
        val card = alolanVulpix.toUiModel()

        assertEquals("vulpix-alola", card.slug)
        assertEquals("vulpix", card.cardSlug)
    }

    @Test
    fun card_carriesItsTypes() {
        assertEquals(TypeUiModel.ICE, alolanVulpix.toUiModel().primaryType)
        assertNull(alolanVulpix.toUiModel().secondaryType)
    }
}
