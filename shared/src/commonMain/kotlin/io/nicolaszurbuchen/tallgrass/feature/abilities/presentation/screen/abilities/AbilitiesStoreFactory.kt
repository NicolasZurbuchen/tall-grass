package io.nicolaszurbuchen.tallgrass.feature.abilities.presentation.screen.abilities

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.ability.domain.usecase.GetAbilitiesUseCase
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface AbilitiesStore : Store<AbilitiesIntent, AbilitiesState, AbilitiesLabel>

class AbilitiesStoreFactory(
    private val storeFactory: StoreFactory,
    private val getAbilities: GetAbilitiesUseCase,
) {
    fun create(): AbilitiesStore =
        object :
            AbilitiesStore,
            Store<AbilitiesIntent, AbilitiesState, AbilitiesLabel> by storeFactory.create(
                name = "AbilitiesStore",
                initialState = AbilitiesState(isLoading = true),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl() },
                reducer = ReducerImpl,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<AbilitiesAction>() {
        override fun invoke() {
            dispatch(AbilitiesAction.LoadAbilities)
        }
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<AbilitiesIntent, AbilitiesAction, AbilitiesState, AbilitiesMessage, AbilitiesLabel>() {
        override fun executeAction(action: AbilitiesAction) {
            when (action) {
                AbilitiesAction.LoadAbilities -> load()
            }
        }

        override fun executeIntent(intent: AbilitiesIntent) {
            when (intent) {
                is AbilitiesIntent.AbilityClicked -> publish(AbilitiesLabel.NavigateToDetail(intent.slug))
                AbilitiesIntent.RetryClicked -> load()
            }
        }

        private fun load() {
            dispatch(AbilitiesMessage.LoadStarted)
            scope.launch {
                try {
                    dispatch(AbilitiesMessage.AbilitiesLoaded(getAbilities()))
                } catch (e: AppException) {
                    dispatch(AbilitiesMessage.LoadFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(AbilitiesMessage.LoadFailed(AppError.Unexpected(e)))
                }
            }
        }
    }

    internal object ReducerImpl : Reducer<AbilitiesState, AbilitiesMessage> {
        override fun AbilitiesState.reduce(msg: AbilitiesMessage): AbilitiesState =
            when (msg) {
                AbilitiesMessage.LoadStarted -> copy(isLoading = true, error = null)
                is AbilitiesMessage.AbilitiesLoaded -> copy(isLoading = false, abilities = msg.abilities, error = null)
                is AbilitiesMessage.LoadFailed -> copy(isLoading = false, error = msg.error)
            }
    }
}
