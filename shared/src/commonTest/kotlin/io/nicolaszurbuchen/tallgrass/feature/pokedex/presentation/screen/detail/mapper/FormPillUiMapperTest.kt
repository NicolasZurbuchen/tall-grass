package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail.mapper

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.EvYield
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.FormKind
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonStats
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonVariant
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.infra.text.UiText
import tallgrass.shared.generated.resources.Res
import tallgrass.shared.generated.resources.pokedex_detail_form_default
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormPillUiMapperTest {
    private fun variant(
        formKind: FormKind = FormKind.MEGA,
        slug: String = "charizard-mega-x",
        name: String = "Mega Charizard X",
        formLabel: String? = "Mega Charizard X",
        isDefault: Boolean = false,
    ) = PokemonVariant(
        slug = slug,
        name = name,
        formLabel = formLabel,
        formKind = formKind,
        isDefault = isDefault,
        artworkUrl = "",
        height = 17,
        weight = 1105,
        primaryType = PokemonType.FIRE,
        secondaryType = PokemonType.DRAGON,
        stats = PokemonStats(78, 130, 111, 130, 85, 100),
        baseExperience = 285,
        evYield = EvYield(hp = 0, attack = 0, defense = 0, specialAttack = 3, specialDefense = 0, speed = 0),
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

    @Test
    fun toFormPillsUiModel_leavesTheCostumesOut() {
        // Pikachu: Rock Star, Pop Star, Ph.D., Libre and eight hats, every one of them carrying
        // Pikachu's types, stats and abilities exactly. Only Partner Pikachu and Gigantamax change
        // anything, so the row is three pills rather than seventeen.
        val pikachu =
            listOf(
                variant(FormKind.NONE, "pikachu", "Pikachu", null, isDefault = true),
                variant(FormKind.COSMETIC, "pikachu-rock-star", "Pikachu Rock Star", "Pikachu Rock Star"),
                variant(FormKind.COSMETIC, "pikachu-libre", "Pikachu Libre", "Pikachu Libre"),
                variant(FormKind.COSMETIC, "pikachu-alola-cap", "Pikachu Alola Cap", "Alola Cap"),
                variant(FormKind.ALTERNATE, "pikachu-starter", "Partner Pikachu", null),
                variant(FormKind.GIGANTAMAX, "pikachu-gmax", "Gigantamax Pikachu", "Gigantamax Form"),
            )

        val pills = pikachu.toFormPillsUiModel("Pikachu")

        assertEquals(listOf("pikachu", "pikachu-starter", "pikachu-gmax"), pills.map { it.slug })
    }

    @Test
    fun toFormPillsUiModel_hasNoRowWhenOnlyOneFormSurvivesTheFilter() {
        // Koraidon's four ride builds are all cosmetic, which leaves nothing to switch between.
        val koraidon =
            listOf(
                variant(FormKind.NONE, "koraidon", "Koraidon", null, isDefault = true),
                variant(FormKind.COSMETIC, "koraidon-limited-build", "Koraidon", null),
                variant(FormKind.COSMETIC, "koraidon-sprinting-build", "Koraidon", null),
            )

        assertTrue(koraidon.toFormPillsUiModel("Koraidon").isEmpty())
    }

    @Test
    fun toFormPillsUiModel_hasNoRowForASpeciesWithOneForm() {
        assertTrue(listOf(variant(FormKind.NONE, "rattata", "Rattata", null, isDefault = true)).toFormPillsUiModel("Rattata").isEmpty())
    }
}
