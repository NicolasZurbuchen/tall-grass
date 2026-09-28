package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail

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

class LocationDetailViewModel(
    factory: LocationDetailStoreFactory,
    slug: String,
    versionSlug: String?,
) : ViewModel() {
    private val store = factory.create(slug, versionSlug)

    // viewModelScope is the main dispatcher, and the mapper is pure, so it runs off it. No card cache
    // here: pinning a condition changes the rows on every tap, so there is no run of states that
    // share one.
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<LocationDetailUiModel> =
        store.stateFlow
            .map { it.toUiModel() }
            .flowOn(Dispatchers.Default)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                LocationDetailState(isLoading = true).toUiModel(),
            )

    val labels: Flow<LocationDetailLabel> = store.labels

    fun onIntent(intent: LocationDetailIntent) {
        store.accept(intent)
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
