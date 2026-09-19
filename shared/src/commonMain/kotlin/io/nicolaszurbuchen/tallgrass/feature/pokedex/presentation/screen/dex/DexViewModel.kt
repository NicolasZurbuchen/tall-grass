package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.DexEntry
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex.mapper.toUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DexViewModel(
    factory: DexStoreFactory,
) : ViewModel() {
    private val store = factory.create()

    private var cardsFrom: List<DexEntry>? = null
    private var cards: List<DexEntryUiModel> = emptyList()

    // viewModelScope is the main dispatcher, and the mapper is pure, so it runs off it.
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DexUiModel> =
        store.stateFlow
            .map { it.toUiModel(cardsFor(it.entries)) }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DexState(isLoading = true).toUiModel())

    val labels: Flow<DexLabel> = store.labels

    fun onIntent(intent: DexIntent) {
        store.accept(intent)
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }

    // Identity, not equality: the reducer copies the state and leaves the entries list alone, so the
    // same instance coming back is the signal that nothing about them changed.
    // DECISIONS.md § The dex is mapped once per list, not once per state
    private fun cardsFor(entries: List<DexEntry>): List<DexEntryUiModel> {
        if (entries !== cardsFrom) {
            cardsFrom = entries
            cards = entries.map { it.toUiModel() }
        }

        return cards
    }
}
