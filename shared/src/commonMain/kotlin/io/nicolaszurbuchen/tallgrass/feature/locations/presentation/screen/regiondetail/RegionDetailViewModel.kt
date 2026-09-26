package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RegionDetailViewModel(
    factory: RegionDetailStoreFactory,
    slug: String,
) : ViewModel() {
    private val store = factory.create(slug)

    // viewModelScope is the main dispatcher, and the mapper is pure, so it runs off it. No card
    // cache here, unlike the two list screens: the query changes the filtered list on almost every
    // keystroke, so there is no run of states that share one.
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<RegionDetailUiModel> =
        store.stateFlow
            .map { it.toUiModel() }
            .flowOn(Dispatchers.Default)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                RegionDetailState(isLoading = true).toUiModel(),
            )

    val labels: Flow<RegionDetailLabel> = store.labels

    fun onIntent(intent: RegionDetailIntent) {
        store.accept(intent)
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
