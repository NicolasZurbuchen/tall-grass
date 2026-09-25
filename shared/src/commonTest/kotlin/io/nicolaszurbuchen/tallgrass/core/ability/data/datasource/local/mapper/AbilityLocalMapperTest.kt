package io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.SelectAbilities
import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.SelectAbilityHolders
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.Ability as AbilityRow

class AbilityLocalMapperTest {
    private fun listRow(generation: Long = 3) =
        SelectAbilities(
            slug = "levitate",
            name = "Levitate",
            generation = generation,
            shortEffect = "Evades Ground moves.",
        )

    private fun detailRow(effect: String = "Immune to Ground-type moves, Spikes, Toxic Spikes and Arena Trap.") =
        AbilityRow(
            slug = "levitate",
            name = "Levitate",
            generation = 3,
            shortEffect = "Evades Ground moves.",
            effect = effect,
        )

    private fun holderRow(
        primaryType: String? = "ghost",
        secondaryType: String? = "poison",
        cardSlug: String? = "gastly",
        isHidden: Boolean = false,
    ) = SelectAbilityHolders(
        slug = "gastly",
        cardSlug = cardSlug,
        speciesDexNumber = 92,
        name = "Gastly",
        artworkUrl = "https://example.invalid/gastly.png",
        primaryType = primaryType,
        secondaryType = secondaryType,
        isHidden = isHidden,
    )

    @Test
    fun listRow_narrowsTheGenerationToAnInt() {
        // SQLite has one integer width and Kotlin has several. Nine generations fit anywhere, so
        // this pins the conversion rather than a bound.
        assertEquals(3, listRow().toDomain().generation)
    }

    @Test
    fun listRow_carriesTheShortEffectAndNothingLonger() {
        val ability = listRow().toDomain()

        assertEquals("levitate", ability.slug)
        assertEquals("Levitate", ability.name)
        assertEquals("Evades Ground moves.", ability.shortEffect)
    }

    @Test
    fun detailRow_carriesBothHalvesOfTheEffect() {
        val ability = detailRow().toDomain()

        assertEquals("Evades Ground moves.", ability.shortEffect)
        assertEquals("Immune to Ground-type moves, Spikes, Toxic Spikes and Arena Trap.", ability.effect)
    }

    @Test
    fun detailRow_keepsALongEffectThatRepeatsTheShortOne() {
        // 46 of the 314 are written this way upstream, and dropping the repeat here would put a
        // rendering decision in the data layer. The screen is where that is decided --
        // see AbilityContentUiMapper.
        val ability = detailRow(effect = "Evades Ground moves.").toDomain()

        assertEquals(ability.shortEffect, ability.effect)
    }

    @Test
    fun holderRow_readsTheTypesAndTheHiddenFlag() {
        val holder = holderRow(isHidden = true).toDomain()

        assertTrue(holder != null)
        assertEquals(PokemonType.GHOST, holder.primaryType)
        assertEquals(PokemonType.POISON, holder.secondaryType)
        assertTrue(holder.isHidden)
    }

    @Test
    fun holderRow_isNullWhenThePrimaryTypeIsNotOneThisBuildKnows() {
        // The bundled dataset and this build disagreeing. A dropped holder is better than a card
        // with no colour.
        assertNull(holderRow(primaryType = "sound").toDomain())
    }

    @Test
    fun holderRow_isNullWhenThereIsNoDexCardToOpen() {
        assertNull(holderRow(cardSlug = null).toDomain())
    }

    @Test
    fun holderRow_treatsAMissingSecondTypeAsTheOrdinaryCase() {
        val holder = holderRow(secondaryType = null).toDomain()

        assertTrue(holder != null)
        assertNull(holder.secondaryType)
    }
}
