package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * [hero] never reaches the Store. It is what the tapped card was already drawing, so it is true
 * before anything is read and stays true whatever the read returns — putting it through the Store
 * would make the hero wait for an answer it already has.
 */
class DetailViewModel(
    factory: DetailStoreFactory,
    variantSlug: String,
    private val hero: HeroHandoff,
) : ViewModel() {
    private val store = factory.create(variantSlug)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DetailUiModel> =
        store.stateFlow
            .map { it.toUiModel(hero) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), store.state.toUiModel(hero))

    val labels: Flow<DetailLabel> = store.labels

    fun onIntent(intent: DetailIntent) {
        store.accept(intent)
    }

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
