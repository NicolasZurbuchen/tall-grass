package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.dex

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetDexEntriesUseCase
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.HeroHandoff
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.dexArtworkKey
import io.nicolaszurbuchen.tallgrass.infra.image.ImagePrefetch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

interface DexStore : Store<DexIntent, DexState, DexLabel>

class DexStoreFactory(
    private val storeFactory: StoreFactory,
    private val getDexEntries: GetDexEntriesUseCase,
    private val prefetchImages: ImagePrefetch,
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
                    hero =
                        HeroHandoff(
                            name = entry.name,
                            artworkUrl = entry.artworkUrl,
                            primaryTypeSlug = entry.primaryType.slug,
                            secondaryTypeSlug = entry.secondaryType?.slug,
                            sharedElementKey = dexArtworkKey(entry.slug),
                        ),
                ),
            )
        }

        private fun load() {
            dispatch(DexMessage.LoadStarted)
            scope.launch {
                try {
                    val entries = getDexEntries()
                    dispatch(DexMessage.EntriesLoaded(entries))
                    prefetchArtwork(entries.map { it.artworkUrl })
                } catch (e: AppException) {
                    dispatch(DexMessage.LoadFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(DexMessage.LoadFailed(AppError.Unexpected(e)))
                }
            }
        }

        // Nothing is awaited and nothing can fail: the run reports itself and ends, and every card
        // on screen already works without it.
        // DECISIONS.md § The prefetch starts with the dex, not with the app
        private fun prefetchArtwork(urls: List<String>) {
            scope.launch {
                delay(PREFETCH_HEAD_START)

                prefetchImages.run(urls).collect { progress ->
                    dispatch(DexMessage.ArtworkPrefetchProgressed(progress))
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
                is DexMessage.ArtworkPrefetchProgressed -> copy(prefetch = msg.progress)
            }
    }
}

// One second, so the fifteen artworks actually on screen are not competing with a thousand queued
// behind them on the frame the grid appears. A guess, but the visible cards have a deadline and the
// prefetch does not.
private val PREFETCH_HEAD_START = 1.seconds
