package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

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

class DexViewModel(
    factory: DexStoreFactory,
) : ViewModel() {
    private val store = factory.create()

    /**
     * **Mapped off the main thread.** `viewModelScope` is the main dispatcher, so without the
     * `flowOn` below every state change builds all 1,082 dex cards on the thread drawing the frame
     * — measured at 22ms cold against the real dataset, and that lands exactly when the grid first
     * appears. The mapper is pure, so there is nothing about it that wants the main thread.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DexUiModel> =
        store.stateFlow
            .map { it.toUiModel() }
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
}
