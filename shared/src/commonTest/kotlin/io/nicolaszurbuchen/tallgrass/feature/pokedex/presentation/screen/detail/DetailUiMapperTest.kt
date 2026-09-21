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
        assertEquals(ordinary.about.gender, mega.about.gender)
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

    /**
     * **The bug this exists for opened every variant on Bulbasaur.**
     *
     * A detail can be reached from somewhere that is not a dex list — the Learned by tab on a move
     * hands over whichever Pokemon learns it, and only default forms are dex cards. Alolan Vulpix,
     * every Mega and every Gigantamax therefore arrive at a carousel that does not contain them.
     *
     * `indexOfFirst` returned -1, `coerceAtLeast(0)` made it 0, and the pager snapped to the first
     * card in the dex and then reported that page as a swipe. The screen became Bulbasaur.
     */
    @Test
    fun toUiModel_doesNotFallToTheFirstCardWhenTheActiveOneIsNotInTheList() {
        val entries = listOf(bulbasaurEntry, charizardEntry)
        val state =
            DetailState(
                entryVariantSlug = "charizard-mega-x",
                query = DexQuery.All,
                isLoading = false,
                entries = entries,
                details = mapOf("charizard-mega-x" to charizardDetail),
                activeVariantSlug = "charizard-mega-x",
            )

        val ui = state.toUiModel(charizardHandoff)

        assertEquals(0, ui.activeIndex)
        assertEquals(1, ui.heroes.size, "a card with no list around it has no neighbours")
        assertEquals("charizard-mega-x", ui.heroes.single().slug)
    }

    @Test
    fun toUiModel_stillWalksTheListWhenTheActiveCardIsInIt() {
        // The ordinary path, pinned beside the one above so the fix cannot be read as "never use the
        // carousel".
        val entries = listOf(bulbasaurEntry, charizardEntry)
        val state =
            DetailState(
                entryVariantSlug = "charizard",
                query = DexQuery.All,
                isLoading = false,
                entries = entries,
                details = mapOf("charizard" to charizardDetail),
                activeVariantSlug = "charizard",
            )

        val ui = state.toUiModel(charizardHandoff)

        assertEquals(1, ui.activeIndex)
        assertEquals(listOf("bulbasaur", "charizard"), ui.heroes.map { it.slug })
    }
}
