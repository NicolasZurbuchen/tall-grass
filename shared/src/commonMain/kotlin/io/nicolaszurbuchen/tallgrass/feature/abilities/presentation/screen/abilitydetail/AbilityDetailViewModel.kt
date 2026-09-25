package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilitydetail

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

class AbilityDetailViewModel(
    factory: AbilityDetailStoreFactory,
    slug: String,
) : ViewModel() {
    private val store = factory.create(slug)

    // Mapped whole on every state, unlike the list screen. The largest holder list is Levitate's 96
    // and it only changes when the ability does, so the identity cache that screen needs would be
    // machinery for one list that never moves.
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<AbilityDetailUiModel> =
        store.stateFlow
            .map { it.toUiModel() }
            .flowOn(Dispatchers.Default)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                AbilityDetailState(isLoading = true).toUiModel(),
            )

    val labels: Flow<AbilityDetailLabel> = store.labels

    fun onIntent(intent: AbilityDetailIntent) {
        store.accept(intent)
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
