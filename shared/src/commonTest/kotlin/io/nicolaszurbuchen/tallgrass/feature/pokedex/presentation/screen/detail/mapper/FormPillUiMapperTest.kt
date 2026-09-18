package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonStats
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_form_default
import kotlin.test.Test
import kotlin.test.assertEquals

class FormPillUiMapperTest {
    private fun variant(
        slug: String = "charizard-mega-x",
        name: String = "Mega Charizard X",
        formLabel: String? = "Mega Charizard X",
        isDefault: Boolean = false,
    ) = PokemonVariant(
        slug = slug,
        name = name,
        formLabel = formLabel,
        isDefault = isDefault,
        artworkUrl = "",
        height = 17,
        weight = 1105,
        primaryType = PokemonType.FIRE,
        secondaryType = PokemonType.DRAGON,
        stats = PokemonStats(78, 130, 111, 130, 85, 100),
    )

    @Test
    fun toFormPillUiModel_takesTheSpeciesNameOutOfTheLabel() {
        // In a row under the name Charizard, "Mega Charizard X" reads as a second Charizard.
        assertEquals(UiText.Raw("Mega X"), variant().toFormPillUiModel("Charizard").label)
    }

    @Test
    fun toFormPillUiModel_dropsTheFillerSuffixes() {
        assertEquals(
            UiText.Raw("Gigantamax"),
            variant(formLabel = "Gigantamax Form").toFormPillUiModel("Charizard").label,
        )
        assertEquals(
            UiText.Raw("Alolan"),
            variant(formLabel = "Alolan Form").toFormPillUiModel("Vulpix").label,
        )
    }

    @Test
    fun toFormPillUiModel_leavesArceusWithTheTypeNameAlone() {
        // Eighteen pills reading "Normal" through "Fairy" is the whole content of that switcher.
        assertEquals(
            UiText.Raw("Bug"),
            variant(formLabel = "Bug Type").toFormPillUiModel("Arceus").label,
        )
    }

    @Test
    fun toFormPillUiModel_callsTheOrdinaryFormStandard() {
        val pill = variant(slug = "charizard", name = "Charizard", formLabel = null, isDefault = true)

        assertEquals(UiText.Resource(Res.string.pokedex_detail_form_default), pill.toFormPillUiModel("Charizard").label)
    }

    @Test
    fun toFormPillUiModel_fallsBackToTheNameForAnUnlabelledFormThatIsNotTheOrdinaryOne() {
        // Partner Pikachu and every Totem form are unlabelled and not default, so "Standard" would
        // put two identically named pills in the same row.
        val pill = variant(slug = "pikachu-starter", name = "Partner Pikachu", formLabel = null, isDefault = false)

        assertEquals(UiText.Raw("Partner Pikachu"), pill.toFormPillUiModel("Pikachu").label)
    }

    @Test
    fun toFormPillUiModel_keepsALabelThatIsNothingButTheSpeciesName() {
        // Stripping it would leave an empty pill, so the name stands in.
        val pill = variant(slug = "koraidon-limited-build", name = "Koraidon", formLabel = "Koraidon")

        assertEquals(UiText.Raw("Koraidon"), pill.toFormPillUiModel("Koraidon").label)
    }

    @Test
    fun toFormPillUiModel_carriesTheSlugTheTapSelects() {
        assertEquals("charizard-mega-x", variant().toFormPillUiModel("Charizard").slug)
    }
}
