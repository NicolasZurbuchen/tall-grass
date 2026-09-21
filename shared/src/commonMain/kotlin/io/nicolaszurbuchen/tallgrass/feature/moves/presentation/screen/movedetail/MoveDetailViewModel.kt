package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

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

class MoveDetailViewModel(
    factory: MoveDetailStoreFactory,
    slug: String,
) : ViewModel() {
    private val store = factory.create(slug)

    // Mapped whole on every state, unlike the two list screens. The largest learner list is Rest's
    // 1,213 and it only changes when the move does, so the identity cache those screens need to
    // avoid remapping a thousand cards per tab tap would be machinery for one list that never moves.
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MoveDetailUiModel> =
        store.stateFlow
            .map { it.toUiModel() }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MoveDetailState(isLoading = true).toUiModel())

    val labels: Flow<MoveDetailLabel> = store.labels

    fun onIntent(intent: MoveDetailIntent) {
        store.accept(intent)
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
