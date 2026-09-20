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

    /**
     * **The check that catches an id from the wrong table.**
     *
     * Upstream numbers `pokemon` and `pokemon_forms` separately and both run into the ten-thousands,
     * so a form id pasted into an artwork URL resolves — to somebody else. Every Arceus Plate and
     * Silvally Memory shipped with another Pokemon's picture that way: Dragon Arceus was Mega Mewtwo
     * X, because 10043 is a real `pokemon` id belonging to it.
     *
     * Nothing else notices. The URL is well-formed, the image loads, and the only way to see it is
     * to open the form switcher and recognise the Pokemon looking back.
     */
    @Test
    fun noTwoVariants_shareOnePicture() {
        val shared =
            variants.groupBy { it.artworkUrl }
                .filterValues { it.size > 1 }
                .mapValues { (_, group) -> group.map { it.slug } }

        assertTrue(shared.isEmpty(), "One picture used by several variants: $shared")
    }

    @Test
    fun everySpecies_isListedInTheDex() {
        // A species with no listed variant would be a gap in the grid at that Dex number.
        val listedNumbers = variants.filter { it.listedInDex }.map { it.speciesDexNumber }.toSet()
        val missing = species.map { it.dexNumber }.filterNot { it in listedNumbers }
        assertTrue(missing.isEmpty(), "Dex numbers with no card: $missing")
    }

    @Test
    fun onlyTheDefaultForm_isListedInTheDex() {
        fun listed(slug: String) = variants.single { it.slug == slug }.listedInDex

        assertTrue(listed("vulpix"), "The species' own form is the card")

        assertTrue(!listed("vulpix-alola"), "A regional form is reached through the form switcher")
        assertTrue(!listed("wooper-paldea"))
        assertTrue(!listed("tauros-paldea-aqua-breed"))
        assertTrue(!listed("darmanitan-galar-standard"))
        assertTrue(!listed("charizard-mega-x"), "A Mega is a battle state, not a browse entry")
        assertTrue(!listed("darmanitan-galar-zen"))
        assertTrue(!listed("meowstic-female"), "Gender differences stay behind the form switcher")
        assertTrue(!listed("zygarde-complete"))
    }

    @Test
    fun theDex_holdsExactlyOneCardPerSpecies() {
        // The invariant the browse list is built on: scrolling it is walking the National Dex, and
        // every form of every kind is reached from the card of the species it belongs to.
        assertEquals(species.size, variants.count { it.listedInDex })
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

    @Test
    fun everyVariant_isClassified() {
        // The partition must be total. A variant with no kind is one the classifier fell through,
        // which is the failure the ordering in classifyForm exists to prevent.
        assertEquals(
            mapOf(
                FormKind.NONE to 1025,
                FormKind.MEGA to 97,
                FormKind.ALTERNATE to 97,
                FormKind.REGIONAL to 57,
                FormKind.COSMETIC to 36,
                FormKind.GIGANTAMAX to 34,
                FormKind.BATTLE_ONLY to 23,
                FormKind.TOTEM to 12,
                FormKind.GENDER to 4,
            ),
            variants.groupingBy { it.formKind }.eachCount().toList().sortedByDescending { it.second }.toMap(),
        )
    }

    @Test
    fun onlyDefaultVariants_areClassifiedNone() {
        assertEquals(
            variants.count { it.isDefault },
            variants.count { it.formKind == FormKind.NONE },
        )
    }

    @Test
    fun formsDifferingOnlyByAbility_areNotFiledAsCostumes() {
        // The regression this pins: a COSMETIC test comparing stats and types alone puts all eight
        // of these in COSMETIC, because an ability is the only thing that separates them from their
        // base form.
        listOf(
            "greninja-battle-bond",
            "rockruff-own-tempo",
            "toxtricity-low-key",
            "zygarde-50-power-construct",
            "basculin-blue-striped",
            "squawkabilly-yellow-plumage",
            "squawkabilly-white-plumage",
        ).forEach { slug ->
            assertEquals(FormKind.ALTERNATE, variants.single { it.slug == slug }.formKind, slug)
        }
    }

    @Test
    fun costumesAndRideForms_areCosmetic() {
        listOf("pikachu-rock-star", "pikachu-cosplay", "koraidon-sprinting-build", "miraidon-glide-mode")
            .forEach { slug ->
                assertEquals(FormKind.COSMETIC, variants.single { it.slug == slug }.formKind, slug)
            }
    }

    @Test
    fun everyVariant_namesItsSpeciesBySlug() {
        val slugs = species.map { it.slug }.toSet()
        val orphans = variants.filterNot { it.speciesSlug in slugs }
        assertTrue(orphans.isEmpty(), "Variants naming an unknown species: ${orphans.map { it.slug }}")
    }

    @Test
    fun abilities_carryNoName() {
        // The name belongs to the ability table that arrives with #27. Storing it here repeated it
        // across 2,943 rows holding 313 distinct abilities.
        assertTrue(variants.all { variant -> variant.abilities.all { it.slug.isNotEmpty() } })
    }

    @Test
    fun arceusAndSilvally_keepTheirTypeChangingForms() {
        // Upstream files these as forms rather than Pokemon because only the type moves. Left out,
        // the dataset says arceus [normal] and the Stats tab reports Fire Arceus as weak to
        // Fighting.
        assertEquals(18, variants.count { it.slug.startsWith("arceus") })
        assertEquals(18, variants.count { it.slug.startsWith("silvally") })

        val fire = variants.single { it.slug == "arceus-fire" }
        assertEquals(listOf("fire"), fire.types)
        assertEquals(FormKind.ALTERNATE, fire.formKind)
        assertTrue(!fire.listedInDex, "Eighteen Arceus would bury the rest of the grid")

        // Artwork comes from the form id, not the pokemon id, or all eighteen share one picture.
        assertTrue(fire.artworkUrl != variants.single { it.slug == "arceus" }.artworkUrl)
    }

    @Test
    fun promotedForms_inheritWhatUpstreamCannotVary() {
        // There is no per-form stat or ability table, so these carry the base form's. The type is
        // the only thing that genuinely differs.
        val base = variants.single { it.slug == "arceus" }
        val fire = variants.single { it.slug == "arceus-fire" }

        assertEquals(base.stats, fire.stats)
        assertEquals(base.abilities.map { it.slug }, fire.abilities.map { it.slug })
        assertEquals(base.speciesDexNumber, fire.speciesDexNumber)
    }
}
