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

    /**
     * **Mapped off the main thread.** `viewModelScope` is the main dispatcher, so without the
     * `flowOn` below every state change builds all 1,082 dex cards on the thread drawing the frame
     * — measured at 22ms cold against the real dataset, and that lands exactly when the grid first
     * appears. The mapper is pure, so there is nothing about it that wants the main thread.
     */
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

    /**
     * The mapped dex, kept for as long as the Store keeps handing back the same entries.
     *
     * The prefetch reports twenty-five times while it fills the artwork cache, and every report is a
     * new `DexState` carrying the *same* entries list. Mapping is O(1,082), so without this the
     * screen pays for the whole dex twenty-five times over to redraw a percentage — around 27,000
     * throwaway UiModels during exactly the window the reader is scrolling.
     *
     * **Identity, not equality.** The reducer copies the state and leaves the list alone, so the same
     * instance coming back is precisely the signal that nothing about the entries changed. Comparing
     * by equality would walk all 1,082 to learn the same thing.
     *
     * Not synchronised, and does not need to be: it is only ever reached from the single coroutine
     * collecting the flow above.
     */
    private fun cardsFor(entries: List<DexEntry>): List<DexEntryUiModel> {
        if (entries !== cardsFrom) {
            cardsFrom = entries
            cards = entries.map { it.toUiModel() }
        }

        return cards
    }
}
