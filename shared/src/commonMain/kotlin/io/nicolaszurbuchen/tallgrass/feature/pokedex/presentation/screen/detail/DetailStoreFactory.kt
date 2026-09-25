package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMovesForVariantUseCase
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.model.PokemonDetail
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetDexEntriesUseCase
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetPokemonDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.type.domain.model.TypeMatchup
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
    private val getMovesForVariant: GetMovesForVariantUseCase,
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
        formSlug: String?,
    ): DetailStore =
        object :
            DetailStore,
            Store<DetailIntent, DetailState, DetailLabel> by storeFactory.create(
                name = "DetailStore",
                initialState =
                    DetailState(
                        entryVariantSlug = variantSlug,
                        query = query,
                        // The form to open on, which is the card itself unless something handed over
                        // a variant. See DetailDestination.
                        activeVariantSlug = formSlug ?: variantSlug,
                    ),
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
        // A swipe can outrun a read. Holding the jobs means a card that is no longer on screen stops
        // being loaded rather than landing on top of the one that is.
        private var detailJob: Job? = null
        private var readAheadJob: Job? = null
        private var movesJob: Job? = null

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
                    loadMoves(intent.variantSlug)
                }

                is DetailIntent.EntrySelected -> {
                    if (intent.entrySlug != state().activeEntrySlug) {
                        dispatch(DetailMessage.EntrySwitched(intent.entrySlug))
                        loadDetail(intent.entrySlug)
                    }
                }

                is DetailIntent.TabSelected -> {
                    dispatch(DetailMessage.TabSwitched(intent.tab))
                    if (intent.tab == DetailState.Tab.MOVES) loadMoves(state().activeVariantSlug)
                }

                is DetailIntent.MoveClicked -> {
                    publish(DetailLabel.NavigateToMove(intent.slug))
                }

                DetailIntent.BackClicked -> {
                    publish(DetailLabel.NavigateBack)
                }

                // Past the cache: a button that says "try again" and quietly does not is worse than no
                // button, even if the only way to see it is to have nothing cached anyway.
                DetailIntent.RetryClicked -> {
                    loadDetail(state().activeEntrySlug, force = true)
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
                readAhead()
            }
        }

        /**
         * Nothing to do when the card is already held: a swipe onto a neighbour that was read ahead
         * shows it on the same frame, with no skeleton in between.
         */
        private fun loadDetail(
            entrySlug: String,
            force: Boolean = false,
        ) {
            if (!force && state().details.containsKey(entrySlug)) {
                readAhead()
                return
            }

            detailJob?.cancel()
            dispatch(DetailMessage.LoadStarted)

            detailJob =
                scope.launch {
                    try {
                        val record = read(entrySlug)

                        if (record == null) {
                            dispatch(DetailMessage.LoadFailed(AppError.Database.NotFound))
                            return@launch
                        }

                        dispatch(DetailMessage.DetailLoaded(entrySlug, record.first, record.second))
                        readAhead()
                    } catch (e: AppException) {
                        dispatch(DetailMessage.LoadFailed(e.error))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        dispatch(DetailMessage.LoadFailed(AppError.Unexpected(e)))
                    }
                }
        }

        /**
         * The moves of one form, read the first time its tab is opened for that form.
         *
         * **Not read with the detail**, which is what keeps a reader who never opens this tab from
         * paying for a hundred rows on every swipe. Held per variant once read and never re-read:
         * the learnset is baked into the binary and cannot change under a running app.
         *
         * Keyed by variant rather than by card because a form learns its own moves — Alolan
         * Exeggutor is not Exeggutor in a different colour — so switching forms lands here too, and
         * the guard is on the map rather than on which tab is open.
         *
         * Failures are swallowed, like the carousel's: a tab that cannot be read is worth less than
         * an error message covering the Pokemon they came to see.
         */
        private fun loadMoves(variantSlug: String) {
            if (state().moves.containsKey(variantSlug)) return

            movesJob?.cancel()
            movesJob =
                scope.launch {
                    try {
                        dispatch(DetailMessage.MovesLoaded(variantSlug, getMovesForVariant(variantSlug)))
                    } catch (e: AppException) {
                        return@launch
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        return@launch
                    }
                }
        }

        /**
         * Reads the cards either side of the one on screen, so a swipe finds them already there.
         *
         * Failures are swallowed. A card the reader has not asked for cannot produce an error
         * message, and the read runs again if they swipe onto it.
         *
         * DECISIONS.md § The carousel reads ahead, so a swipe lands on content
         */
        private fun readAhead() {
            readAheadJob?.cancel()

            val current = state()
            val index = current.entries.indexOfFirst { it.slug == current.activeEntrySlug }
            if (index < 0) return

            val wanted =
                listOfNotNull(current.entries.getOrNull(index - 1), current.entries.getOrNull(index + 1))
                    .map { it.slug }
                    .filterNot { current.details.containsKey(it) }

            if (wanted.isEmpty()) return

            readAheadJob =
                scope.launch {
                    wanted.forEach { slug ->
                        try {
                            read(slug)?.let { dispatch(DetailMessage.DetailLoaded(slug, it.first, it.second)) }
                        } catch (e: AppException) {
                            return@forEach
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            return@forEach
                        }
                    }
                }
        }

        private suspend fun read(entrySlug: String): Pair<PokemonDetail, Map<String, List<TypeMatchup>>>? {
            val detail = getPokemonDetail(entrySlug) ?: return null

            // Every form's matchups, not only the one about to be on screen: Arceus has eighteen, and
            // the switcher has to move between them without a query.
            val matchups =
                detail.variants.associate { variant ->
                    variant.slug to getTypeMatchups(variant.primaryType, variant.secondaryType)
                }

            return detail to matchups
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
                    val isOnScreen = msg.entrySlug == activeEntrySlug
                    val held = windowed(details + (msg.entrySlug to msg.detail))

                    copy(
                        isLoading = if (isOnScreen) false else isLoading,
                        details = held,
                        matchups = (matchups + msg.matchups).filterKeys { it in variantSlugs(held) },
                        // Whichever form is already selected, if this species has it. That is the
                        // card itself on the ordinary path, and the variant that was handed over
                        // when something opened a form directly -- resolving to the card here would
                        // throw that away and open Exeggutor on a move that only Alolan Exeggutor
                        // learns.
                        //
                        // Falling back to the first form rather than to nothing: if the dataset has
                        // stopped carrying the one asked for, a species is a better screen than an
                        // empty one. A read that answers for a card nobody is looking at changes
                        // nothing about the one they are.
                        activeVariantSlug =
                            if (isOnScreen) {
                                msg.detail.variants
                                    .map { it.slug }
                                    .firstOrNull { it == activeVariantSlug }
                                    ?: msg.detail.variants.first().slug
                            } else {
                                activeVariantSlug
                            },
                        error = if (isOnScreen) null else error,
                    )
                }

                is DetailMessage.LoadFailed -> {
                    copy(isLoading = false, error = msg.error)
                }

                is DetailMessage.EntrySwitched -> {
                    copy(
                        activeEntrySlug = msg.entrySlug,
                        activeVariantSlug = msg.entrySlug,
                        error = null,
                    )
                }

                is DetailMessage.FormSwitched -> {
                    copy(activeVariantSlug = msg.variantSlug)
                }

                is DetailMessage.MovesLoaded -> {
                    // Kept for every form that has been looked at rather than only the one on
                    // screen: a reader comparing two forms switches back and forth, and the second
                    // look should not read again.
                    copy(moves = moves + (msg.variantSlug to msg.moves))
                }

                is DetailMessage.TabSwitched -> {
                    copy(tab = msg.tab)
                }
            }

        /**
         * The card on screen and the two either side of it, which is exactly what the read-ahead
         * fills and exactly what a swipe can reach without another read.
         *
         * Bounded by construction rather than by a count: swiping the length of the dex holds three
         * records whatever route it took to get there.
         */
        private fun DetailState.windowed(candidates: Map<String, PokemonDetail>): Map<String, PokemonDetail> {
            val index = entries.indexOfFirst { it.slug == activeEntrySlug }

            val window =
                if (index < 0) {
                    setOf(activeEntrySlug)
                } else {
                    listOfNotNull(
                        entries.getOrNull(index - 1),
                        entries.getOrNull(index),
                        entries.getOrNull(index + 1),
                    ).map { it.slug }.toSet() + activeEntrySlug
                }

            return candidates.filterKeys { it in window }
        }

        private fun variantSlugs(details: Map<String, PokemonDetail>): Set<String> =
            details.values.flatMap { detail -> detail.variants.map { it.slug } }.toSet()
    }
}
