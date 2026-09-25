package io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.FakeAbilityRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetAbilitiesForVariantUseCaseTest {
    private val variantAbilities =
        mapOf("charmander" to listOf(AbilityFixtures.blaze, AbilityFixtures.solarPower))

    @Test
    fun invoke_returnsWhatTheRepositoryHoldsInTheOrderItHoldsIt() =
        runTest {
            // Slot order with the hidden one last, which is the game's own ordering and the
            // query's. Nothing between here and the screen is allowed to resort it.
            val useCase = GetAbilitiesForVariantUseCase(FakeAbilityRepository(variantAbilities = variantAbilities))

            assertEquals(listOf(AbilityFixtures.blaze, AbilityFixtures.solarPower), useCase("charmander"))
        }

    @Test
    fun invoke_returnsEmptyForAVariantWithNoRows() =
        runTest {
            // Nothing in the dataset is like this -- every Pokemon has at least one ability -- so an
            // empty list means the slug is not one, which the screen reads as not-yet-loaded.
            val useCase = GetAbilitiesForVariantUseCase(FakeAbilityRepository(variantAbilities = variantAbilities))

            assertTrue(useCase("missingno").isEmpty())
        }

    @Test
    fun invoke_propagatesFailureRatherThanReturningEmpty() =
        runTest {
            val useCase = GetAbilitiesForVariantUseCase(FakeAbilityRepository(failure = IllegalStateException("no db")))

            assertFailsWith<IllegalStateException> { useCase("charmander") }
        }
}
