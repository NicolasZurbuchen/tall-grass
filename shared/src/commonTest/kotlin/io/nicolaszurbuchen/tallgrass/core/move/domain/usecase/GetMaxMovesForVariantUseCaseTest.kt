package io.nicolaszurbuchen.tallgrass.core.move.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.move.domain.fake.FakeMoveRepository
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.DamageClass
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.LearnMethod
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.MaxMove
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.VariantMove
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetMaxMovesForVariantUseCaseTest {
    private fun move(
        slug: String,
        type: PokemonType,
        damageClass: DamageClass = DamageClass.SPECIAL,
        power: Int? = 90,
        maxPower: Int? = 130,
    ) = VariantMove(
        slug = slug,
        name = slug.replaceFirstChar { it.uppercase() },
        type = type,
        damageClass = damageClass,
        power = power,
        maxPower = maxPower,
        method = LearnMethod.LEVEL_UP,
        level = 1,
    )

    // Enough of the catalogue to convert the moves below, in the shape the real one has: the ordinary
    // Max Moves keyed by type, Max Guard sharing Normal with Max Strike, and one signature.
    private val catalogue =
        listOf(
            MaxMove("max-flare", "Max Flare", PokemonType.FIRE, power = null),
            MaxMove("max-airstream", "Max Airstream", PokemonType.FLYING, power = null),
            MaxMove("max-strike", "Max Strike", PokemonType.NORMAL, power = null),
            MaxMove("max-guard", "Max Guard", PokemonType.NORMAL, power = null),
            MaxMove("g-max-wildfire", "G-Max Wildfire", PokemonType.FIRE, power = null),
            MaxMove("g-max-fireball", "G-Max Fireball", PokemonType.FIRE, power = 160),
        )

    private fun useCase(moves: List<VariantMove>) =
        GetMaxMovesForVariantUseCase(
            FakeMoveRepository(variantMoves = mapOf("charizard-gmax" to moves), maxMoves = catalogue),
        )

    @Test
    fun invoke_turnsTheMovesOfTheSignatureTypeIntoTheFormsOwnMove() =
        runTest {
            // The whole of what the Gigantamax factor changes: the Fire moves become G-Max Wildfire
            // rather than Max Flare, and nothing else moves.
            val moves =
                listOf(
                    move("flamethrower", PokemonType.FIRE),
                    move("fire-blast", PokemonType.FIRE, power = 110, maxPower = 140),
                    move("fly", PokemonType.FLYING, damageClass = DamageClass.PHYSICAL),
                )

            val converted = useCase(moves)("charizard-gmax", "g-max-wildfire")

            assertEquals(listOf("g-max-wildfire", "max-airstream"), converted.map { it.slug })
            assertTrue(converted.first().isSignature)
            assertTrue(converted.last().isSignature.not())
        }

    @Test
    fun invoke_withoutASignature_leavesTheFireMovesAsTheOrdinaryMaxMove() =
        runTest {
            // Every Pokemon can Dynamax; only 34 forms can Gigantamax. Null is the difference.
            val converted = useCase(listOf(move("flamethrower", PokemonType.FIRE)))("charizard-gmax", null)

            assertEquals(listOf("max-flare"), converted.map { it.slug })
            assertTrue(converted.single().isSignature.not())
        }

    @Test
    fun invoke_collapsesEveryMoveOfATypeIntoOneRowAndKeepsWhatFedIt() =
        runTest {
            // The reason `sources` exists. Four Fire moves are one row, and without the names under
            // it the list reads as a Pokemon that has forgotten three of them.
            val moves =
                listOf(
                    move("ember", PokemonType.FIRE, power = 40, maxPower = 90),
                    move("flamethrower", PokemonType.FIRE),
                    move("fire-blast", PokemonType.FIRE, power = 110, maxPower = 140),
                    move("heat-wave", PokemonType.FIRE, power = 95, maxPower = 130),
                )

            val wildfire = useCase(moves)("charizard-gmax", "g-max-wildfire").single()

            assertEquals(4, wildfire.sources.size)
            assertEquals(listOf("Fire-blast", "Flamethrower", "Heat-wave", "Ember"), wildfire.sources.map { it.name })
        }

    @Test
    fun invoke_takesThePowerOfTheStrongestMoveThatFeedsIt() =
        runTest {
            // Max Flare is 130 from Flamethrower and 90 from Ember. A Pokemon with both reaches 130,
            // which is what it can actually do.
            val moves = listOf(move("ember", PokemonType.FIRE, power = 40, maxPower = 90), move("flamethrower", PokemonType.FIRE))

            assertEquals(130, useCase(moves)("charizard-gmax", null).single().power)
        }

    @Test
    fun invoke_takesTheCategoryOfThatSameMove() =
        runTest {
            // A Max Move's category comes from whatever it replaced, so the strongest source decides
            // it rather than a vote or the first row read.
            val moves =
                listOf(
                    move("fire-punch", PokemonType.FIRE, damageClass = DamageClass.PHYSICAL, power = 75, maxPower = 130),
                    move("fire-blast", PokemonType.FIRE, power = 110, maxPower = 140),
                )

            assertEquals(DamageClass.SPECIAL, useCase(moves)("charizard-gmax", null).single().damageClass)
        }

    @Test
    fun invoke_alwaysTakesTheFixedPowerWhereTheMoveHasOne() =
        runTest {
            // G-Max Fireball is 160 whatever it replaced, so the strongest source does not get a say.
            val moves = listOf(move("ember", PokemonType.FIRE, power = 40, maxPower = 90))

            assertEquals(160, useCase(moves)("charizard-gmax", "g-max-fireball").single().power)
        }

    @Test
    fun invoke_turnsEveryStatusMoveIntoMaxGuardRatherThanIntoItsOwnType() =
        runTest {
            // Max Guard is Normal-type and so is Max Strike. A status Normal move must become the
            // first and a damaging one the second, which is the collision the lookup has to survive.
            val moves =
                listOf(
                    move("swords-dance", PokemonType.NORMAL, damageClass = DamageClass.STATUS, power = null, maxPower = null),
                    move("roost", PokemonType.FLYING, damageClass = DamageClass.STATUS, power = null, maxPower = null),
                    move("body-slam", PokemonType.NORMAL, damageClass = DamageClass.PHYSICAL),
                )

            val converted = useCase(moves)("charizard-gmax", null)

            assertEquals(listOf("max-strike", "max-guard"), converted.map { it.slug })
            assertEquals(2, converted.single { it.slug == "max-guard" }.sources.size)
            assertEquals(null, converted.single { it.slug == "max-guard" }.power)
        }

    @Test
    fun invoke_putsTheStrongestFirstAndMaxGuardLast() =
        runTest {
            // Max Guard is the one row with no power and the one row every Pokemon has, so it is the
            // least worth reading and sits at the bottom.
            val moves =
                listOf(
                    move("swords-dance", PokemonType.NORMAL, damageClass = DamageClass.STATUS, power = null, maxPower = null),
                    move("ember", PokemonType.FIRE, power = 40, maxPower = 90),
                    move("fly", PokemonType.FLYING, damageClass = DamageClass.PHYSICAL, power = 90, maxPower = 130),
                )

            assertEquals(
                listOf("max-airstream", "max-flare", "max-guard"),
                useCase(moves)("charizard-gmax", null).map { it.slug },
            )
        }

    @Test
    fun invoke_readsNothingWhenTheFormLearnsNothing() =
        runTest {
            // The Arceus and Silvally case, reached through a form that cannot Gigantamax anyway. The
            // catalogue read is skipped rather than made and thrown away.
            val repository = FakeMoveRepository(maxMoves = catalogue)

            assertEquals(emptyList(), GetMaxMovesForVariantUseCase(repository)("arceus-fire", null))
            assertEquals(0, repository.maxMovesCallCount)
        }
}
