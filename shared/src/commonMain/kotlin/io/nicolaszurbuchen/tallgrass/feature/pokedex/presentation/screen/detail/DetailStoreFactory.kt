package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetDexEntriesUseCase
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetPokemonDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.type.domain.usecase.GetTypeMatchupsUseCase
import io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.navigation.DexQuery
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface DetailStore : Store<DetailIntent, DetailState, DetailLabel>

class DetailStoreFactory(
    private val storeFactory: StoreFactory,
    private val getDexEntries: GetDexEntriesUseCase,
    private val getPokemonDetail: GetPokemonDetailUseCase,
    private val getTypeMatchups: GetTypeMatchupsUseCase,
) {
    /**
     * Which Pokemon the screen is about, and which list it was reached through, are properties of
     * the Store being made rather than of the thing making it, so they arrive here. That is what
     * keeps the factory an ordinary Koin binding instead of something assembled by hand where the
     * graph verification cannot see it.
     */
    fun create(
        variantSlug: String,
        query: DexQuery,
    ): DetailStore =
        object :
            DetailStore,
            Store<DetailIntent, DetailState, DetailLabel> by storeFactory.create(
                name = "DetailStore",
                initialState = DetailState(entryVariantSlug = variantSlug, query = query),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl(query) },
                reducer = ReducerImpl,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<DetailAction>() {
        override fun invoke() {
            dispatch(DetailAction.LoadDetail)
            dispatch(DetailAction.LoadCarousel)
        }
    }

    private inner class ExecutorImpl(
        private val query: DexQuery,
    ) : CoroutineExecutor<DetailIntent, DetailAction, DetailState, DetailMessage, DetailLabel>() {
        // A swipe can outrun a read. Holding the job means a card that is no longer on screen stops
        // being loaded rather than landing on top of the one that is.
        private var detailJob: Job? = null

        override fun executeAction(action: DetailAction) {
            when (action) {
                DetailAction.LoadCarousel -> loadCarousel()
                DetailAction.LoadDetail -> loadDetail(state().activeEntrySlug)
            }
        }

        override fun executeIntent(intent: DetailIntent) {
            when (intent) {
                is DetailIntent.FormSelected -> {
                    dispatch(DetailMessage.FormSwitched(intent.variantSlug))
                }

                is DetailIntent.EntrySelected -> {
                    if (intent.entrySlug != state().activeEntrySlug) {
                        dispatch(DetailMessage.EntrySwitched(intent.entrySlug))
                        loadDetail(intent.entrySlug)
                    }
                }

                is DetailIntent.TabSelected -> {
                    dispatch(DetailMessage.TabSwitched(intent.tab))
                }

                DetailIntent.BackClicked -> {
                    publish(DetailLabel.NavigateBack)
                }

                DetailIntent.RetryClicked -> {
                    loadDetail(state().activeEntrySlug)
                }
            }
        }

        /**
         * A failed read leaves the carousel empty rather than failing the screen. The detail is what
         * the reader asked for; the cards either side of it are context, and context that cannot be
         * read is worth less than an error message covering the thing they came to see.
         */
        private fun loadCarousel() {
            scope.launch {
                val entries =
                    try {
                        when (query) {
                            DexQuery.All -> getDexEntries()
                        }
                    } catch (e: AppException) {
                        emptyList()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        emptyList()
                    }

                dispatch(DetailMessage.CarouselLoaded(entries))
            }
        }

        private fun loadDetail(variantSlug: String) {
            detailJob?.cancel()
            dispatch(DetailMessage.LoadStarted)

            detailJob =
                scope.launch {
                    try {
                        val detail = getPokemonDetail(variantSlug)

                        if (detail == null) {
                            dispatch(DetailMessage.LoadFailed(AppError.Database.NotFound))
                            return@launch
                        }

                        // Every form's matchups, not only the one about to be on screen: Arceus has
                        // eighteen, and the switcher has to move between them without a query.
                        val matchups =
                            detail.variants.associate { variant ->
                                variant.slug to getTypeMatchups(variant.primaryType, variant.secondaryType)
                            }

                        dispatch(DetailMessage.DetailLoaded(detail, matchups))
                    } catch (e: AppException) {
                        dispatch(DetailMessage.LoadFailed(e.error))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        dispatch(DetailMessage.LoadFailed(AppError.Unexpected(e)))
                    }
                }
        }
    }

    internal object ReducerImpl : Reducer<DetailState, DetailMessage> {
        override fun DetailState.reduce(msg: DetailMessage): DetailState =
            when (msg) {
                DetailMessage.LoadStarted -> {
                    copy(isLoading = true, error = null)
                }

                is DetailMessage.CarouselLoaded -> {
                    copy(entries = msg.entries)
                }

                is DetailMessage.DetailLoaded -> {
                    copy(
                        isLoading = false,
                        detail = msg.detail,
                        matchups = msg.matchups,
                        // The card the carousel is on, unless the dataset has stopped carrying it,
                        // in which case the first form of the species is a better screen than an
                        // empty one.
                        activeVariantSlug =
                            msg.detail.variants
                                .map { it.slug }
                                .firstOrNull { it == activeEntrySlug }
                                ?: msg.detail.variants.first().slug,
                        error = null,
                    )
                }

                is DetailMessage.LoadFailed -> {
                    copy(isLoading = false, error = msg.error)
                }

                // The sheet empties. Holding the previous Pokemon's forms, stats and matchups under
                // the new one's name for the length of a read is a wrong screen rather than a slow
                // one, and the read is one frame.
                is DetailMessage.EntrySwitched -> {
                    copy(
                        activeEntrySlug = msg.entrySlug,
                        activeVariantSlug = msg.entrySlug,
                        detail = null,
                        matchups = emptyMap(),
                        error = null,
                    )
                }

                is DetailMessage.FormSwitched -> {
                    copy(activeVariantSlug = msg.variantSlug)
                }

                is DetailMessage.TabSwitched -> {
                    copy(tab = msg.tab)
                }
            }
    }
}
