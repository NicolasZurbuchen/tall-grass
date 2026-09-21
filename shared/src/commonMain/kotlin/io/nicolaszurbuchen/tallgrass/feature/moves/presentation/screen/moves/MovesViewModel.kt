package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import io.nicolaszurbuchen.tallgrass.core.move.domain.model.Move
import io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves.mapper.toUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MovesViewModel(
    factory: MovesStoreFactory,
) : ViewModel() {
    private val store = factory.create()

    private var cardsFrom: List<Move>? = null
    private var cards: List<MoveUiModel> = emptyList()

    // viewModelScope is the main dispatcher, and the mapper is pure, so it runs off it.
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MovesUiModel> =
        store.stateFlow
            .map { it.toUiModel(cardsFor(it.moves)) }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MovesState(isLoading = true).toUiModel())

    val labels: Flow<MovesLabel> = store.labels

    fun onIntent(intent: MovesIntent) {
        store.accept(intent)
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }

    // Identity, not equality, on the same grounds as DexViewModel: the reducer copies the state and
    // leaves the list alone, so the same instance coming back means nothing about the cards changed.
    // DECISIONS.md § The dex is mapped once per list, not once per state
    private fun cardsFor(moves: List<Move>): List<MoveUiModel> {
        if (moves !== cardsFrom) {
            cardsFrom = moves
            cards = moves.map { it.toUiModel() }
        }

        return cards
    }
}
