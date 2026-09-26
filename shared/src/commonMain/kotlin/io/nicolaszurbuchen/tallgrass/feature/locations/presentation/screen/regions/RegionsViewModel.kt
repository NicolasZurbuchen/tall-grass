package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import io.nicolaszurbuchen.tallgrass.core.location.domain.model.Region
import io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions.mapper.toUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RegionsViewModel(
    factory: RegionsStoreFactory,
) : ViewModel() {
    private val store = factory.create()

    private var cardsFrom: List<Region>? = null
    private var cards: List<RegionUiModel> = emptyList()

    // viewModelScope is the main dispatcher, and the mapper is pure, so it runs off it.
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<RegionsUiModel> =
        store.stateFlow
            .map { it.toUiModel(cardsFor(it.regions)) }
            .flowOn(Dispatchers.Default)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                RegionsState(isLoading = true).toUiModel(),
            )

    val labels: Flow<RegionsLabel> = store.labels

    fun onIntent(intent: RegionsIntent) {
        store.accept(intent)
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }

    // Identity, not equality, on the same grounds as DexViewModel: the reducer copies the state and
    // leaves the list alone, so the same instance coming back means nothing about the cards changed.
    // DECISIONS.md § The dex is mapped once per list, not once per state
    private fun cardsFor(regions: List<Region>): List<RegionUiModel> {
        if (regions !== cardsFrom) {
            cardsFrom = regions
            cards = regions.map { it.toUiModel() }
        }

        return cards
    }
}
