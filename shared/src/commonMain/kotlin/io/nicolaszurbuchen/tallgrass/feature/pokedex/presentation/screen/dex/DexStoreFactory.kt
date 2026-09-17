package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetDexEntriesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface DexStore : Store<DexIntent, DexState, DexLabel>

class DexStoreFactory(
    private val storeFactory: StoreFactory,
    private val getDexEntries: GetDexEntriesUseCase,
) {
    fun create(): DexStore =
        object :
            DexStore,
            Store<DexIntent, DexState, DexLabel> by storeFactory.create(
                name = "DexStore",
                initialState = DexState(isLoading = true),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl() },
                reducer = ReducerImpl,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<DexAction>() {
        override fun invoke() {
            dispatch(DexAction.LoadEntries)
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<DexIntent, DexAction, DexState, DexMessage, DexLabel>() {
        override fun executeAction(action: DexAction) {
            when (action) {
                DexAction.LoadEntries -> load()
            }
        }

        override fun executeIntent(intent: DexIntent) {
            when (intent) {
                is DexIntent.EntryClicked -> navigateToDetail(intent.slug)
                DexIntent.RetryClicked -> load()
            }
        }

        // The entry is read back out of state rather than carried on the Intent: what the hero opens
        // with is a fact about the Pokemon that was tapped, and the Store is where that is known.
        private fun navigateToDetail(slug: String) {
            val entry = state().entries.firstOrNull { it.slug == slug } ?: return

            publish(
                DexLabel.NavigateToDetail(
                    slug = entry.slug,
                    artworkUrl = entry.artworkUrl,
                    primaryTypeSlug = entry.primaryType.slug,
                ),
            )
        }

        private fun load() {
            dispatch(DexMessage.LoadStarted)
            scope.launch {
                try {
                    dispatch(DexMessage.EntriesLoaded(getDexEntries()))
                } catch (e: AppException) {
                    dispatch(DexMessage.LoadFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(DexMessage.LoadFailed(AppError.Unexpected(e)))
                }
            }
        }
    }

    internal object ReducerImpl : Reducer<DexState, DexMessage> {
        override fun DexState.reduce(msg: DexMessage): DexState =
            when (msg) {
                DexMessage.LoadStarted -> copy(isLoading = true, error = null)
                is DexMessage.EntriesLoaded -> copy(isLoading = false, entries = msg.entries, error = null)
                is DexMessage.LoadFailed -> copy(isLoading = false, error = msg.error)
            }
    }
}
