package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regiondetail

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionDexUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionLocationsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface RegionDetailStore : Store<RegionDetailIntent, RegionDetailState, RegionDetailLabel>

class RegionDetailStoreFactory(
    private val storeFactory: StoreFactory,
    private val getRegionDetail: GetRegionDetailUseCase,
    private val getRegionLocations: GetRegionLocationsUseCase,
    private val getRegionDex: GetRegionDexUseCase,
) {
    fun create(slug: String): RegionDetailStore =
        object :
            RegionDetailStore,
            Store<RegionDetailIntent, RegionDetailState, RegionDetailLabel> by storeFactory.create(
                name = "RegionDetailStore",
                initialState = RegionDetailState(isLoading = true),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl(slug) },
                reducer = ReducerImpl,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<RegionDetailAction>() {
        override fun invoke() {
            dispatch(RegionDetailAction.LoadRegion)
        }
    }

    private inner class ExecutorImpl(
        private val slug: String,
    ) : CoroutineExecutor<RegionDetailIntent, RegionDetailAction, RegionDetailState, RegionDetailMessage, RegionDetailLabel>() {
        override fun executeAction(action: RegionDetailAction) {
            when (action) {
                RegionDetailAction.LoadRegion -> load()
            }
        }

        override fun executeIntent(intent: RegionDetailIntent) {
            when (intent) {
                is RegionDetailIntent.TabSelected -> {
                    RegionDetailState.Tab.entries.getOrNull(intent.index)?.let {
                        dispatch(RegionDetailMessage.TabChanged(it))
                    }
                }

                is RegionDetailIntent.QueryChanged -> {
                    dispatch(RegionDetailMessage.QueryChanged(intent.query))
                }

                is RegionDetailIntent.PokemonClicked -> {
                    openPokemon(intent.slug)
                }

                RegionDetailIntent.RetryClicked -> {
                    load()
                }
            }
        }

        // The tapped card is looked up rather than carried through the Intent, so the Intent stays a
        // slug and the six fields the next screen's hero needs cannot drift out of step with it.
        private fun openPokemon(slug: String) {
            val entry = state().dex.firstOrNull { it.slug == slug } ?: return

            publish(
                RegionDetailLabel.NavigateToPokemon(
                    cardSlug = entry.cardSlug,
                    formSlug = entry.slug,
                    name = entry.name,
                    artworkUrl = entry.artworkUrl,
                    primaryTypeSlug = entry.primaryType.slug,
                    secondaryTypeSlug = entry.secondaryType?.slug,
                ),
            )
        }

        private fun load() {
            dispatch(RegionDetailMessage.LoadStarted)
            scope.launch {
                try {
                    val region = getRegionDetail(slug)

                    if (region == null) {
                        dispatch(RegionDetailMessage.LoadFailed(AppError.Database.NotFound))
                        return@launch
                    }

                    dispatch(
                        RegionDetailMessage.RegionLoaded(
                            region = region,
                            locations = getRegionLocations(slug),
                            dex = getRegionDex(slug),
                        ),
                    )
                } catch (e: AppException) {
                    dispatch(RegionDetailMessage.LoadFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(RegionDetailMessage.LoadFailed(AppError.Unexpected(e)))
                }
            }
        }
    }

    internal object ReducerImpl : Reducer<RegionDetailState, RegionDetailMessage> {
        override fun RegionDetailState.reduce(msg: RegionDetailMessage): RegionDetailState =
            when (msg) {
                RegionDetailMessage.LoadStarted -> {
                    copy(isLoading = true, error = null)
                }

                is RegionDetailMessage.RegionLoaded -> {
                    copy(
                        isLoading = false,
                        region = msg.region,
                        locations = msg.locations,
                        dex = msg.dex,
                        error = null,
                    )
                }

                is RegionDetailMessage.TabChanged -> {
                    copy(tab = msg.tab)
                }

                is RegionDetailMessage.QueryChanged -> {
                    copy(query = msg.query)
                }

                is RegionDetailMessage.LoadFailed -> {
                    copy(isLoading = false, error = msg.error)
                }
            }
    }
}
