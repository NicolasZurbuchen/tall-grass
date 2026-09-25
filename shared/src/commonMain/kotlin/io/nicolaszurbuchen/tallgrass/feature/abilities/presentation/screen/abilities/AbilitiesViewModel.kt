package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import io.nicolaszurbuchen.tallgrass.core.ability.domain.model.Ability
import io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities.mapper.toUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AbilitiesViewModel(
    factory: AbilitiesStoreFactory,
) : ViewModel() {
    private val store = factory.create()

    private var cardsFrom: List<Ability>? = null
    private var cards: List<AbilityUiModel> = emptyList()

    // viewModelScope is the main dispatcher, and the mapper is pure, so it runs off it.
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<AbilitiesUiModel> =
        store.stateFlow
            .map { it.toUiModel(cardsFor(it.abilities)) }
            .flowOn(Dispatchers.Default)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                AbilitiesState(isLoading = true).toUiModel(),
            )

    val labels: Flow<AbilitiesLabel> = store.labels

    fun onIntent(intent: AbilitiesIntent) {
        store.accept(intent)
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }

    // Identity, not equality, on the same grounds as DexViewModel: the reducer copies the state and
    // leaves the list alone, so the same instance coming back means nothing about the cards changed.
    // DECISIONS.md § The dex is mapped once per list, not once per state
    private fun cardsFor(abilities: List<Ability>): List<AbilityUiModel> {
        if (abilities !== cardsFrom) {
            cardsFrom = abilities
            cards = abilities.map { it.toUiModel() }
        }

        return cards
    }
}
