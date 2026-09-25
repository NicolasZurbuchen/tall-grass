package io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local

import io.nicolaszurbuchen.tallgrass.core.ability.data.datasource.local.mapper.toDomain
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.VariantAbility
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AbilityLocalDataSourceImpl(
    private val queries: Lazy<AbilityQueries>,
    private val dispatcher: CoroutineDispatcher,
) : AbilityLocalDataSource {
    override suspend fun abilities(): List<Ability> =
        withContext(dispatcher) {
            queries.value
                .selectAbilities()
                .executeAsList()
                .map { it.toDomain() }
        }

    override suspend fun detail(slug: String): AbilityDetail? =
        withContext(dispatcher) {
            queries.value
                .selectAbility(slug)
                .executeAsOneOrNull()
                ?.toDomain()
        }

    // Its own read rather than part of the detail's, on the same grounds as a move's learners: it is
    // its own tab, and a reader who never opens it never pays for the rows. Levitate's 96 holders
    // are the largest here.
    override suspend fun holders(slug: String): List<AbilityHolder> =
        withContext(dispatcher) {
            queries.value
                .selectAbilityHolders(slug)
                .executeAsList()
                .mapNotNull { it.toDomain() }
        }

    // Ordered by SQL rather than in Kotlin, unlike a variant's moves: slot order is a column and
    // says what it means, where a move's order lives in LearnMethod's declaration.
    override suspend fun abilitiesFor(variantSlug: String): List<VariantAbility> =
        withContext(dispatcher) {
            queries.value
                .selectAbilitiesForVariant(variantSlug)
                .executeAsList()
                .map { it.toDomain() }
        }
}
