package io.nicolaszurbuchen.tallgrass.core.ability.domain.fake

import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityDetail
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.AbilityHolder
import io.nicolaszurbuchen.tallgrass.core.ability.domain.repository.AbilityRepository

/**
 * Counted per query rather than in total, on the same grounds as `FakeMoveRepository`: the ability
 * detail reads twice -- the ability and who has it -- so one counter could not tell a retry from the
 * second half of one load.
 */
class FakeAbilityRepository(
    private var abilities: List<Ability> = emptyList(),
    private var details: Map<String, AbilityDetail> = emptyMap(),
    private var holders: Map<String, List<AbilityHolder>> = emptyMap(),
    private var failure: Throwable? = null,
) : AbilityRepository {
    var abilitiesCallCount: Int = 0
        private set

    var detailCallCount: Int = 0
        private set

    var holdersCallCount: Int = 0
        private set

    override suspend fun abilities(): List<Ability> {
        abilitiesCallCount++
        failure?.let { throw it }
        return abilities
    }

    override suspend fun abilityDetail(slug: String): AbilityDetail? {
        detailCallCount++
        failure?.let { throw it }
        return details[slug]
    }

    override suspend fun abilityHolders(slug: String): List<AbilityHolder> {
        holdersCallCount++
        failure?.let { throw it }
        return holders[slug].orEmpty()
    }
}
