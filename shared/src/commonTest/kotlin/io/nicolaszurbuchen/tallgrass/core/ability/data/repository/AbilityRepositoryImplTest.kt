package io.nicolaszurbuchen.tallgrass.core.ability.data.repository

import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.AbilityLocalDataSource
import io.nicolaszurbuchen.tallgrass.core.ability.domain.fake.AbilityFixtures
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.VariantAbility
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbilityRepositoryImplTest {
    private fun repository() =
        AbilityRepositoryImpl(
            StubAbilityLocalDataSource(
                abilities = listOf(AbilityFixtures.levitate),
                details = mapOf("levitate" to AbilityFixtures.levitateDetail),
                holders = mapOf("levitate" to listOf(AbilityFixtures.gastly)),
                variantAbilities = mapOf("charmander" to listOf(AbilityFixtures.blaze, AbilityFixtures.solarPower)),
            ),
        )

    @Test
    fun abilities_readFromTheLocalSourceAndAreNotReshaped() =
        runTest {
            assertEquals(listOf(AbilityFixtures.levitate), repository().abilities())
        }

    @Test
    fun abilityDetail_readsFromTheLocalSourceAndIsNotReshaped() =
        runTest {
            assertEquals(AbilityFixtures.levitateDetail, repository().abilityDetail("levitate"))
        }

    @Test
    fun abilityDetail_passesAMissingSlugStraightThroughAsNull() =
        runTest {
            assertNull(repository().abilityDetail("missingno"))
        }

    @Test
    fun abilityHolders_readFromTheLocalSourceAndAreNotReshaped() =
        runTest {
            assertEquals(listOf(AbilityFixtures.gastly), repository().abilityHolders("levitate"))
        }

    @Test
    fun abilityHolders_passAnEmptyResultStraightThrough() =
        runTest {
            assertTrue(repository().abilityHolders("stench").isEmpty())
        }

    fun abilitiesFor_readFromTheLocalSourceInTheOrderTheyCameBack() =
        runTest {
            // Slot order, hidden last, decided by SQL. The repository must not resort them.
            assertEquals(
                listOf(AbilityFixtures.blaze, AbilityFixtures.solarPower),
                repository().abilitiesFor("charmander"),
            )
        }

    private class StubAbilityLocalDataSource(
        private val abilities: List<Ability>,
        private val details: Map<String, AbilityDetail>,
        private val holders: Map<String, List<AbilityHolder>>,
        private val variantAbilities: Map<String, List<VariantAbility>>,
    ) : AbilityLocalDataSource {
        override suspend fun abilities(): List<Ability> = abilities

        override suspend fun detail(slug: String): AbilityDetail? = details[slug]

        override suspend fun holders(slug: String): List<AbilityHolder> = holders[slug].orEmpty()

        override suspend fun abilitiesFor(variantSlug: String): List<VariantAbility> = variantAbilities[variantSlug].orEmpty()
    }
}
