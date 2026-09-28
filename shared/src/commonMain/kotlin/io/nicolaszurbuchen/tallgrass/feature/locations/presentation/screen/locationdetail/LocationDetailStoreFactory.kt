package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.locationdetail

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetEncounterConditionsUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetLocationDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetLocationEncountersUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface LocationDetailStore : Store<LocationDetailIntent, LocationDetailState, LocationDetailLabel>

class LocationDetailStoreFactory(
    private val storeFactory: StoreFactory,
    private val getLocationDetail: GetLocationDetailUseCase,
    private val getEncounters: GetLocationEncountersUseCase,
    private val getConditions: GetEncounterConditionsUseCase,
) {
    fun create(slug: String): LocationDetailStore =
        object :
            LocationDetailStore,
            Store<LocationDetailIntent, LocationDetailState, LocationDetailLabel> by storeFactory.create(
                name = "LocationDetailStore",
                initialState = LocationDetailState(isLoading = true),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl(slug) },
                reducer = ReducerImpl,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<LocationDetailAction>() {
        override fun invoke() {
            dispatch(LocationDetailAction.LoadLocation)
        }
    }

    private inner class ExecutorImpl(
        private val slug: String,
    ) : CoroutineExecutor<
            LocationDetailIntent,
            LocationDetailAction,
            LocationDetailState,
            LocationDetailMessage,
            LocationDetailLabel,
        >() {
        override fun executeAction(action: LocationDetailAction) {
            when (action) {
                LocationDetailAction.LoadLocation -> load()
            }
        }

        override fun executeIntent(intent: LocationDetailIntent) {
            when (intent) {
                is LocationDetailIntent.VersionSelected -> {
                    selectVersion(intent.slug)
                }

                LocationDetailIntent.VersionCleared -> {
                    dispatch(LocationDetailMessage.VersionCleared)
                }

                is LocationDetailIntent.MethodSelected -> {
                    dispatch(LocationDetailMessage.MethodSelected(intent.method))
                }

                is LocationDetailIntent.ConditionSelected -> {
                    dispatch(LocationDetailMessage.ConditionSelected(intent.axis, intent.value))
                }

                is LocationDetailIntent.PokemonClicked -> {
                    openPokemon(intent.variantSlug)
                }

                LocationDetailIntent.RetryClicked -> {
                    load()
                }
            }
        }

        // The tapped row is looked up rather than carried through the Intent, so the Intent stays a
        // slug and the six fields the next screen's hero needs cannot drift out of step with it.
        private fun openPokemon(variantSlug: String) {
            val encounter = state().encounters.firstOrNull { it.variantSlug == variantSlug } ?: return

            publish(
                LocationDetailLabel.NavigateToPokemon(
                    cardSlug = encounter.cardSlug,
                    formSlug = encounter.variantSlug,
                    name = encounter.name,
                    artworkUrl = encounter.artworkUrl,
                    primaryTypeSlug = encounter.primaryType.slug,
                    secondaryTypeSlug = encounter.secondaryType?.slug,
                ),
            )
        }

        /**
         * A grey cell is selected like any other, and the read that follows comes back empty.
         *
         * That is deliberate: the two empty states are told apart by what the *screen* knows about
         * the game, not by refusing to look. See #21 and the empty states in `LocationDetailUiMapper`.
         */
        private fun selectVersion(version: String) {
            dispatch(LocationDetailMessage.VersionSelected(version))
            scope.launch {
                try {
                    dispatch(LocationDetailMessage.EncountersLoaded(getEncounters(slug, version)))
                } catch (e: AppException) {
                    dispatch(LocationDetailMessage.LoadFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(LocationDetailMessage.LoadFailed(AppError.Unexpected(e)))
                }
            }
        }

        private fun load() {
            dispatch(LocationDetailMessage.LoadStarted)
            scope.launch {
                try {
                    val location = getLocationDetail(slug)

                    if (location == null) {
                        dispatch(LocationDetailMessage.LoadFailed(AppError.Database.NotFound))
                        return@launch
                    }

                    dispatch(LocationDetailMessage.LocationLoaded(location, getConditions()))
                } catch (e: AppException) {
                    dispatch(LocationDetailMessage.LoadFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(LocationDetailMessage.LoadFailed(AppError.Unexpected(e)))
                }
            }
        }
    }

    internal object ReducerImpl : Reducer<LocationDetailState, LocationDetailMessage> {
        override fun LocationDetailState.reduce(msg: LocationDetailMessage): LocationDetailState =
            when (msg) {
                LocationDetailMessage.LoadStarted -> {
                    copy(isLoading = true, error = null)
                }

                is LocationDetailMessage.LocationLoaded -> {
                    copy(isLoading = false, location = msg.location, conditions = msg.conditions, error = null)
                }

                is LocationDetailMessage.VersionSelected -> {
                    // The method and the pinned conditions are cleared with the game, because both
                    // belong to the table that is about to be replaced: Route 1 in HeartGold varies
                    // on three axes and in Red on none, and a pin left over from the first would
                    // silently filter the second.
                    copy(
                        version = msg.slug,
                        isLoadingEncounters = true,
                        encounters = emptyList(),
                        method = null,
                        pinned = emptyMap(),
                        error = null,
                    )
                }

                LocationDetailMessage.VersionCleared -> {
                    copy(version = null, encounters = emptyList(), method = null, pinned = emptyMap())
                }

                is LocationDetailMessage.EncountersLoaded -> {
                    copy(
                        isLoadingEncounters = false,
                        encounters = msg.encounters,
                        // The first method present, because a route has no fixed set of them and
                        // there is nothing to default to until the rows are in.
                        method = msg.encounters.firstOrNull()?.method,
                    )
                }

                is LocationDetailMessage.MethodSelected -> {
                    // The pins go with the tab. Each method is its own table with its own axes, and
                    // a time pinned on the walking table means nothing to the fishing one.
                    copy(method = msg.method, pinned = emptyMap())
                }

                is LocationDetailMessage.ConditionSelected -> {
                    copy(
                        pinned = if (msg.value == null) pinned - msg.axis else pinned + (msg.axis to msg.value),
                    )
                }

                is LocationDetailMessage.LoadFailed -> {
                    copy(isLoading = false, isLoadingEncounters = false, error = msg.error)
                }
            }
    }
}
