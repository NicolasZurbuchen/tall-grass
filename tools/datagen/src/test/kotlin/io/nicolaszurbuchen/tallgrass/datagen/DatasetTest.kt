package io.nicolaszurbuchen.tallgrass.datagen

import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the committed dataset rather than the code that wrote it.
 *
 * The failure this exists for is a generation that half-worked: a truncated read, a filter that
 * matched nothing, a regional rule that quietly stopped firing. All of those produce a dataset that
 * parses, builds a database and ships a dex with holes in it.
 *
 * The counts are deliberately exact. A range would pass through exactly the drift worth catching,
 * and when the pinned SHA moves these are *meant* to need updating -- that edit is the moment
 * somebody looks at what changed.
 */
class DatasetTest {
    private val json = Json { ignoreUnknownKeys = false }
    private val dataDir = File("../../data")

    private val manifest by lazy { json.decodeFromString<Manifest>(dataDir.resolve("manifest.json").readText()) }
    private val species by lazy { json.decodeFromString<List<SpeciesJson>>(dataDir.resolve("species.json").readText()) }
    private val variants by lazy { json.decodeFromString<List<VariantJson>>(dataDir.resolve("variants.json").readText()) }
    private val types by lazy { json.decodeFromString<TypeChartJson>(dataDir.resolve("types.json").readText()) }

    @Test
    fun manifest_matchesTheFilesItDescribes() {
        assertEquals(manifest.speciesCount, species.size)
        assertEquals(manifest.variantCount, variants.size)
        assertEquals(manifest.listedVariantCount, variants.count { it.listedInDex })
        assertEquals(manifest.typeCount, types.types.size)
    }

    @Test
    fun dataset_holdsEveryNationalDexNumberExactlyOnce() {
        assertEquals(1025, species.size)
        assertEquals((1..1025).toList(), species.map { it.dexNumber })
    }

    @Test
    fun types_areTheEighteenRealOnes() {
        assertEquals(18, types.types.size)
        assertTrue(types.types.none { it.slug == "stellar" || it.slug == "unknown" || it.slug == "shadow" })
    }

    @Test
    fun everyVariant_belongsToAKnownSpecies() {
        val dexNumbers = species.map { it.dexNumber }.toSet()
        val orphans = variants.filterNot { it.speciesDexNumber in dexNumbers }
        assertTrue(orphans.isEmpty(), "Variants with no species: ${orphans.map { it.slug }}")
    }

    @Test
    fun everyVariant_hasArtworkAndAtLeastOneType() {
        val untyped = variants.filter { it.types.isEmpty() }
        assertTrue(untyped.isEmpty(), "Variants with no type: ${untyped.map { it.slug }}")
        assertTrue(variants.all { it.artworkUrl.endsWith(".png") })
    }

    @Test
    fun everySpecies_isListedInTheDex() {
        // A species with no listed variant would be a gap in the grid at that Dex number.
        val listedNumbers = variants.filter { it.listedInDex }.map { it.speciesDexNumber }.toSet()
        val missing = species.map { it.dexNumber }.filterNot { it in listedNumbers }
        assertTrue(missing.isEmpty(), "Dex numbers with no card: $missing")
    }

    @Test
    fun regionalForms_areListedAndOtherFormsAreNot() {
        fun listed(slug: String) = variants.single { it.slug == slug }.listedInDex

        assertTrue(listed("vulpix-alola"), "A regional form gets its own card")
        assertTrue(listed("wooper-paldea"))
        assertTrue(listed("tauros-paldea-aqua-breed"), "Each Paldean Tauros breed is its own form")
        assertTrue(listed("darmanitan-galar-standard"), "The Galarian form itself is regional")

        assertTrue(!listed("charizard-mega-x"), "A Mega is a battle state, not a regional form")
        assertTrue(!listed("darmanitan-galar-zen"), "Zen Mode is a transformation of a regional form")
        assertTrue(!listed("meowstic-female"), "Gender differences stay behind the form switcher")
        assertTrue(!listed("zygarde-complete"))
    }

    @Test
    fun pikachusCostumes_doNotEachGetACard() {
        // `alola-cap` is a hat from Alola, not an Alolan Pikachu: prefix-matching the region name
        // would put a second Pikachu in the grid differing only in headwear.
        val pikachus = variants.filter { it.slug.startsWith("pikachu") }
        assertTrue(pikachus.size > 1, "Sanity: the costumes should be in the dataset")
        assertEquals(1, pikachus.count { it.listedInDex })
    }

    @Test
    fun cosmeticForms_neverBecameVariants() {
        // Alcremie has 63 cosmetic combinations upstream and exactly one battle-distinct form.
        assertTrue(variants.count { it.slug.startsWith("alcremie") } <= 2)
    }

    @Test
    fun species_carryTheBreedingDataTheAboutTabNeeds() {
        val bulbasaur = species.single { it.dexNumber == 1 }
        assertEquals("bulbasaur", bulbasaur.slug)
        assertEquals("Bulbasaur", bulbasaur.name)
        assertTrue(bulbasaur.genus.isNotEmpty())
        assertTrue(bulbasaur.eggGroups.isNotEmpty())
        assertEquals("medium-slow", bulbasaur.growthRate)
    }

    @Test
    fun genderlessSpecies_keepUpstreamsMinusOne() {
        // -1 is a third case rather than a missing percentage, and flattening it to 0 would render
        // Magnemite as female-only.
        assertEquals(-1, species.single { it.slug == "magnemite" }.genderRate)
    }

    @Test
    fun typeChart_storesOnlyTheNonNeutralPairs() {
        assertTrue(types.efficacies.none { it.factorPercent == 100 })
        assertEquals(
            200,
            types.efficacies.single { it.damage == "water" && it.target == "fire" }.factorPercent,
        )
        assertEquals(
            0,
            types.efficacies.single { it.damage == "normal" && it.target == "ghost" }.factorPercent,
        )
    }

    @Test
    fun json_isSortedSoTheDiffIsReadable() {
        assertEquals(species.map { it.dexNumber }.sorted(), species.map { it.dexNumber })
        assertEquals(types.types.map { it.slug }.sorted(), types.types.map { it.slug })
    }
}
