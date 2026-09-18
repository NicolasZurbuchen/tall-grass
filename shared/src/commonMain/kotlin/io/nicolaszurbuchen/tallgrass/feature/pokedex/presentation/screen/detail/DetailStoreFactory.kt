package io.nicolaszurbuchen.tallgrass.feature.pokedex.presentation.screen.detail

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import io.nicolaszurbuchen.tallgrass.core.pokemon.domain.usecase.GetPokemonDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.type.domain.usecase.GetTypeMatchupsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface DetailStore : Store<DetailIntent, DetailState, DetailLabel>

class DetailStoreFactory(
    private val storeFactory: StoreFactory,
    private val getPokemonDetail: GetPokemonDetailUseCase,
    private val getTypeMatchups: GetTypeMatchupsUseCase,
) {
    /**
     * Which Pokemon the screen is about is a property of the Store being made rather than of the
     * thing making it, so it arrives here. That is what keeps the factory an ordinary Koin binding
     * instead of something assembled by hand where the graph verification cannot see it.
     */
    fun create(variantSlug: String): DetailStore =
        object :
            DetailStore,
            Store<DetailIntent, DetailState, DetailLabel> by storeFactory.create(
                name = "DetailStore",
                initialState = DetailState(entryVariantSlug = variantSlug),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl(variantSlug) },
                reducer = ReducerImpl,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<DetailAction>() {
        override fun invoke() {
            dispatch(DetailAction.LoadDetail)
        }
    }

    private inner class ExecutorImpl(
        private val variantSlug: String,
    ) : CoroutineExecutor<DetailIntent, DetailAction, DetailState, DetailMessage, DetailLabel>() {
        override fun executeAction(action: DetailAction) {
            when (action) {
                DetailAction.LoadDetail -> load()
            }
        }

        override fun executeIntent(intent: DetailIntent) {
            when (intent) {
                is DetailIntent.FormSelected -> dispatch(DetailMessage.FormSwitched(intent.variantSlug))
                is DetailIntent.TabSelected -> dispatch(DetailMessage.TabSwitched(intent.tab))
                DetailIntent.BackClicked -> publish(DetailLabel.NavigateBack)
                DetailIntent.RetryClicked -> load()
            }
        }

        private fun load() {
            dispatch(DetailMessage.LoadStarted)
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

                is DetailMessage.DetailLoaded -> {
                    copy(
                        isLoading = false,
                        detail = msg.detail,
                        matchups = msg.matchups,
                        // The tapped form, unless the dataset has stopped carrying it, in which case
                        // the first form of the species is a better screen than an empty one.
                        activeVariantSlug =
                            msg.detail.variants
                                .map { it.slug }
                                .firstOrNull { it == entryVariantSlug }
                                ?: msg.detail.variants.first().slug,
                        error = null,
                    )
                }

                is DetailMessage.LoadFailed -> {
                    copy(isLoading = false, error = msg.error)
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
