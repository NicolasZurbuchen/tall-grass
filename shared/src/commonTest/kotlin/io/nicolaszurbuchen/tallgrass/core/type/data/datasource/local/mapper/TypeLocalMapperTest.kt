package io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local.mapper

import io.nicolaszurbuchen.tallgrass.core.type.data.datasource.local.SelectEfficacyAgainst
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.PokemonType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TypeLocalMapperTest {
    @Test
    fun toDomain_carriesTheAttackingTypeAndItsPercentage() {
        val cell = SelectEfficacyAgainst(damageTypeSlug = "fire", factorPercent = 200L).toDomain()

        assertEquals(PokemonType.FIRE, cell?.damageType)
        assertEquals(200, cell?.factorPercent)
    }

    @Test
    fun toDomain_dropsACellWhoseAttackingTypeIsUnknown() {
        // Stellar is in upstream's type table but is not a type a Pokemon has, so it has no column
        // in this app's enum. Dropping the cell leaves one matchup unlisted rather than crashing.
        assertNull(SelectEfficacyAgainst(damageTypeSlug = "stellar", factorPercent = 200L).toDomain())
    }
}
