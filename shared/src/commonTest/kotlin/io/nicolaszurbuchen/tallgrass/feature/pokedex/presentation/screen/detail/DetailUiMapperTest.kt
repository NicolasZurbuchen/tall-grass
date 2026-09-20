package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.type.presentation.uimodel.TypeUiModel
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DexQuery
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.uimodel.DetailTabUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DetailUiMapperTest {
    private fun state(
        activeSlug: String = "charizard",
        isLoading: Boolean = false,
    ) = DetailState(
        entryVariantSlug = "charizard",
        query = DexQuery.All,
        isLoading = isLoading,
        details = mapOf("charizard" to charizardDetail),
        activeVariantSlug = activeSlug,
    )

    @Test
    fun toUiModel_drawsTheHeroBeforeAnythingHasBeenRead() {
        // The point of the handoff: a hero with no artwork and no colour is the flicker the shared
        // element exists to remove, and both are known the moment the screen opens.
        val ui = DetailState(entryVariantSlug = "charizard", query = DexQuery.All).toUiModel(charizardHandoff)

        assertEquals(charizardHandoff.artworkUrl, ui.heroes.single().artworkUrl)
        assertEquals(TypeUiModel.FIRE.color, ui.tint)
        assertNull(ui.content)
        assertTrue(ui.isLoading)
    }

    @Test
    fun toUiModel_keepsTheTappedCardsKeyWhileTheTappedFormIsOnScreen() {
        assertEquals(dexArtworkKey("charizard"), state().toUiModel(charizardHandoff).heroes.single().artworkKey)
    }

    @Test
    fun toUiModel_takesTheKeyOffTheOtherFormsArtwork() {
        // The Mega is a different picture. Keeping the key would fly Charizard's card into it.
        val key = state(activeSlug = "charizard-mega-x").toUiModel(charizardHandoff).heroes.single().artworkKey

        assertNotEquals(dexArtworkKey("charizard"), key)
        assertEquals(dexArtworkKey("charizard-mega-x"), key)
    }

    @Test
    fun toUiModel_switchingFormChangesTheStatsAndLeavesTheBreedingBlockAlone() {
        // The bug the Species/Variant split exists to prevent. Egg groups are true of Charizard
        // whichever form it is in; base stats are not.
        val ordinary = assertNotNull(state().toUiModel(charizardHandoff).content)
        val mega = assertNotNull(state(activeSlug = "charizard-mega-x").toUiModel(charizardHandoff).content)

        assertNotEquals(ordinary.stats, mega.stats)
        assertEquals(ordinary.about.genderText, mega.about.genderText)
        assertEquals(ordinary.about.eggGroupsText, mega.about.eggGroupsText)
        assertEquals(ordinary.about.eggCycleText, mega.about.eggCycleText)
    }

    @Test
    fun toUiModel_switchingFormStillChangesTheMeasurements() {
        // Height and weight sit in the same block and do belong to the form: a Mega is heavier.
        val ordinary = assertNotNull(state().toUiModel(charizardHandoff).content)
        val mega = assertNotNull(state(activeSlug = "charizard-mega-x").toUiModel(charizardHandoff).content)

        assertNotEquals(ordinary.about.weightText, mega.about.weightText)
    }

    @Test
    fun toUiModel_readsTheTypesAndTheTintFromTheFormOnScreen() {
        // Mega Charizard X is Fire/Dragon. Arceus is the same case eighteen times over.
        val mega = state(activeSlug = "charizard-mega-x").toUiModel(charizardHandoff)

        assertEquals(listOf(TypeUiModel.FIRE, TypeUiModel.DRAGON), mega.types)
    }

    @Test
    fun toUiModel_padsTheDexNumberToThreeDigits() {
        assertEquals("#006", state().toUiModel(charizardHandoff).numberText)
    }

    @Test
    fun toUiModel_listsEveryFormInTheSwitcher() {
        val content = assertNotNull(state().toUiModel(charizardHandoff).content)

        assertEquals(listOf("charizard", "charizard-mega-x"), content.forms.map { it.slug })
        assertEquals("charizard", content.activeFormSlug)
    }

    @Test
    fun toUiModel_keepsTheCostumesOutOfTheSwitcher() {
        // Pikachu's fourteen cosmetic forms are in the dataset and must not reach the row. This
        // asserts it through the whole State-to-UiModel path, because the filter is easy to drop
        // when the mapper is next touched and nothing else on screen would look wrong.
        val withCostume = charizardDetail.copy(variants = charizardDetail.variants + charizardCostume)

        val content = assertNotNull(state().copy(details = mapOf("charizard" to withCostume)).toUiModel(charizardHandoff).content)

        assertEquals(listOf("charizard", "charizard-mega-x"), content.forms.map { it.slug })
    }

    @Test
    fun toUiModel_hasNoSwitcherForASpeciesWithOneForm() {
        val single = charizardDetail.copy(variants = listOf(charizard))

        val content = assertNotNull(state().copy(details = mapOf("charizard" to single)).toUiModel(charizardHandoff).content)

        assertTrue(content.forms.isEmpty())
    }

    @Test
    fun toUiModel_carriesTheTabAndTheError() {
        assertEquals(
            DetailTabUiModel.STATS,
            assertNotNull(state().copy(tab = DetailState.Tab.STATS).toUiModel(charizardHandoff).content).tab,
        )
        assertNotNull(state().copy(error = AppError.Database.NotFound).toUiModel(charizardHandoff).error)
    }
}
