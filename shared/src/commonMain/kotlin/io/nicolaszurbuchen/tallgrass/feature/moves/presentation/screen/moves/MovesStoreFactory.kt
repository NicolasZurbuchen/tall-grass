package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.moves

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMovesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface MovesStore : Store<MovesIntent, MovesState, MovesLabel>

class MovesStoreFactory(
    private val storeFactory: StoreFactory,
    private val getMoves: GetMovesUseCase,
) {
    fun create(): MovesStore =
        object :
            MovesStore,
            Store<MovesIntent, MovesState, MovesLabel> by storeFactory.create(
                name = "MovesStore",
                initialState = MovesState(isLoading = true),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl() },
                reducer = ReducerImpl,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<MovesAction>() {
        override fun invoke() {
            dispatch(MovesAction.LoadMoves)
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<MovesIntent, MovesAction, MovesState, MovesMessage, MovesLabel>() {
        override fun executeAction(action: MovesAction) {
            when (action) {
                MovesAction.LoadMoves -> load()
            }
        }

        override fun executeIntent(intent: MovesIntent) {
            when (intent) {
                is MovesIntent.MoveClicked -> publish(MovesLabel.NavigateToDetail(intent.slug))
                MovesIntent.RetryClicked -> load()
            }
        }

        private fun load() {
            dispatch(MovesMessage.LoadStarted)
            scope.launch {
                try {
                    dispatch(MovesMessage.MovesLoaded(getMoves()))
                } catch (e: AppException) {
                    dispatch(MovesMessage.LoadFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(MovesMessage.LoadFailed(AppError.Unexpected(e)))
                }
            }
        }
    }

    internal object ReducerImpl : Reducer<MovesState, MovesMessage> {
        override fun MovesState.reduce(msg: MovesMessage): MovesState =
            when (msg) {
                MovesMessage.LoadStarted -> copy(isLoading = true, error = null)
                is MovesMessage.MovesLoaded -> copy(isLoading = false, moves = msg.moves, error = null)
                is MovesMessage.LoadFailed -> copy(isLoading = false, error = msg.error)
            }
    }
}
