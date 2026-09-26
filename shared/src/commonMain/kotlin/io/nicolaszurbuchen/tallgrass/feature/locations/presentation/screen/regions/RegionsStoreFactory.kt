package io.nicolaszurbuchen.tallgrass.feature.locations.presentation.screen.regions

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import io.nicolaszurbuchen.tallgrass.core.location.domain.usecase.GetRegionsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface RegionsStore : Store<RegionsIntent, RegionsState, RegionsLabel>

class RegionsStoreFactory(
    private val storeFactory: StoreFactory,
    private val getRegions: GetRegionsUseCase,
) {
    fun create(): RegionsStore =
        object :
            RegionsStore,
            Store<RegionsIntent, RegionsState, RegionsLabel> by storeFactory.create(
                name = "RegionsStore",
                initialState = RegionsState(isLoading = true),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl() },
                reducer = ReducerImpl,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<RegionsAction>() {
        override fun invoke() {
            dispatch(RegionsAction.LoadRegions)
        }
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<RegionsIntent, RegionsAction, RegionsState, RegionsMessage, RegionsLabel>() {
        override fun executeAction(action: RegionsAction) {
            when (action) {
                RegionsAction.LoadRegions -> load()
            }
        }

        override fun executeIntent(intent: RegionsIntent) {
            when (intent) {
                is RegionsIntent.RegionClicked -> publish(RegionsLabel.NavigateToDetail(intent.slug))
                RegionsIntent.RetryClicked -> load()
            }
        }

        private fun load() {
            dispatch(RegionsMessage.LoadStarted)
            scope.launch {
                try {
                    dispatch(RegionsMessage.RegionsLoaded(getRegions()))
                } catch (e: AppException) {
                    dispatch(RegionsMessage.LoadFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(RegionsMessage.LoadFailed(AppError.Unexpected(e)))
                }
            }
        }
    }

    internal object ReducerImpl : Reducer<RegionsState, RegionsMessage> {
        override fun RegionsState.reduce(msg: RegionsMessage): RegionsState =
            when (msg) {
                RegionsMessage.LoadStarted -> copy(isLoading = true, error = null)
                is RegionsMessage.RegionsLoaded -> copy(isLoading = false, regions = msg.regions, error = null)
                is RegionsMessage.LoadFailed -> copy(isLoading = false, error = msg.error)
            }
    }
}
