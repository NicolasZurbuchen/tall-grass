package io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.data.datasource.local.SelectDexEntries
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DexLocalMapperTest {
    private fun row(
        slug: String = "bulbasaur",
        dexNumber: Long = 1,
        name: String = "Bulbasaur",
        formLabel: String? = null,
        primaryType: String? = "grass",
        secondaryType: String? = "poison",
    ) = SelectDexEntries(
        slug = slug,
        speciesDexNumber = dexNumber,
        name = name,
        formLabel = formLabel,
        artworkUrl = "https://example.invalid/1.png",
        primaryType = primaryType,
        secondaryType = secondaryType,
    )

    @Test
    fun toDomain_carriesEveryFieldTheCardDraws() {
        val entry = row().toDomain()

        assertEquals("bulbasaur", entry?.slug)
        assertEquals(1, entry?.dexNumber)
        assertEquals("Bulbasaur", entry?.name)
        assertEquals(PokemonType.GRASS, entry?.primaryType)
        assertEquals(PokemonType.POISON, entry?.secondaryType)
    }

    @Test
    fun toDomain_treatsAMissingSecondTypeAsOrdinary() {
        // Most Pokemon have one type. Absent here is the normal case, not a failure.
        assertNull(row(secondaryType = null).toDomain()?.secondaryType)
    }

    @Test
    fun toDomain_dropsARowWhosePrimaryTypeIsUnknown() {
        // Only reachable if the bundled dataset and this build disagree about the eighteen types.
        // One missing card beats a crash on the dex screen.
        assertNull(row(primaryType = "stellar").toDomain())
        assertNull(row(primaryType = null).toDomain())
    }

    @Test
    fun toDomain_keepsTheFormLabelUntrimmed() {
        // "Alolan Form" is upstream's wording. Shortening it is the UI mapper's job, not this one's.
        assertEquals("Alolan Form", row(formLabel = "Alolan Form").toDomain()?.formLabel)
    }
}
