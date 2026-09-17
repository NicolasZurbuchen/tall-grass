package io.nicolaszurbuchen.tallgrass.core.type.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.type.domain.fake.FakeTypeRepository
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeEfficacy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The chart here is Charizard's, cut down to the cells each case needs. Only non-neutral cells are
 * ever stored, so an attacking type absent from a list deals normal damage to that half.
 */
class GetTypeMatchupsUseCaseTest {
    private val againstFire =
        listOf(
            TypeEfficacy(PokemonType.ROCK, 200),
            TypeEfficacy(PokemonType.GROUND, 200),
            TypeEfficacy(PokemonType.WATER, 200),
            TypeEfficacy(PokemonType.GRASS, 50),
        )

    private val againstFlying =
        listOf(
            TypeEfficacy(PokemonType.ROCK, 200),
            TypeEfficacy(PokemonType.GROUND, 0),
            TypeEfficacy(PokemonType.GRASS, 50),
            TypeEfficacy(PokemonType.FIGHTING, 50),
        )

    private val useCase =
        GetTypeMatchupsUseCase(
            FakeTypeRepository(
                mapOf(
                    PokemonType.FIRE to againstFire,
                    PokemonType.FLYING to againstFlying,
                ),
            ),
        )

    private suspend fun charizard() = useCase(PokemonType.FIRE, PokemonType.FLYING).associate { it.attackingType to it.multiplier }

    @Test
    fun invoke_multipliesTheTwoHalvesTogether() =
        runTest {
            val matchups = charizard()

            assertEquals(4f, matchups[PokemonType.ROCK])
            assertEquals(0.25f, matchups[PokemonType.GRASS])
        }

    @Test
    fun invoke_letsAnImmunityWinOverAWeakness() =
        runTest {
            // Ground is doubly effective on Fire and cannot touch Flying at all. Charizard takes
            // nothing -- the one case where multiplying in the wrong order would read as x2.
            assertEquals(0f, charizard()[PokemonType.GROUND])
        }

    @Test
    fun invoke_carriesAMatchupThatOnlyOneHalfHas() =
        runTest {
            val matchups = charizard()

            assertEquals(2f, matchups[PokemonType.WATER])
            assertEquals(0.5f, matchups[PokemonType.FIGHTING])
        }

    @Test
    fun invoke_leavesOutTheTypesThatDoNormalDamage() =
        runTest {
            val matchups = charizard()

            assertNull(matchups[PokemonType.PSYCHIC])
            assertNull(matchups[PokemonType.NORMAL])
        }

    @Test
    fun invoke_leavesOutAWeaknessAResistanceCancels() =
        runTest {
            // Volcanion's case: Water is doubly effective on Fire and halved by Water, which is
            // exactly normal damage. Comparing rounded floats is how this one becomes a x1.0 row.
            val repository =
                FakeTypeRepository(
                    mapOf(
                        PokemonType.FIRE to listOf(TypeEfficacy(PokemonType.WATER, 200)),
                        PokemonType.WATER to listOf(TypeEfficacy(PokemonType.WATER, 50)),
                    ),
                )

            val matchups = GetTypeMatchupsUseCase(repository)(PokemonType.FIRE, PokemonType.WATER)

            assertTrue(matchups.none { it.attackingType == PokemonType.WATER })
        }

    @Test
    fun invoke_readsOnlyThePrimaryTypeWhenThereIsNoSecond() =
        runTest {
            val matchups = useCase(PokemonType.FIRE, null).associate { it.attackingType to it.multiplier }

            assertEquals(2f, matchups[PokemonType.GROUND])
            assertEquals(0.5f, matchups[PokemonType.GRASS])
        }

    @Test
    fun invoke_ordersTheWorstMatchupsFirst() =
        runTest {
            val multipliers = useCase(PokemonType.FIRE, PokemonType.FLYING).map { it.multiplier }

            assertEquals(multipliers.sortedDescending(), multipliers)
        }
}
