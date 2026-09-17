package io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase

import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.repository.PokedexRepository

class GetDexEntriesUseCase(
    private val repository: PokedexRepository,
) {
    suspend operator fun invoke(): List<DexEntry> = repository.dexEntries()
}
