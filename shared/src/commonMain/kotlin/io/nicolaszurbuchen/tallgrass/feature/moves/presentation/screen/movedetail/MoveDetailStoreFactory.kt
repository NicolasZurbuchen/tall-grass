package io.nicolaszurbuchen.tallgrass.feature.moves.presentation.screen.movedetail

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import io.nicolaszurbuchen.tallgrass.core.error.AppError
import io.nicolaszurbuchen.tallgrass.core.error.AppException
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMoveDetailUseCase
import io.nicolaszurbuchen.tallgrass.core.move.domain.usecase.GetMoveLearnersUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

interface MoveDetailStore : Store<MoveDetailIntent, MoveDetailState, MoveDetailLabel>

class MoveDetailStoreFactory(
    private val storeFactory: StoreFactory,
    private val getMoveDetail: GetMoveDetailUseCase,
    private val getMoveLearners: GetMoveLearnersUseCase,
) {
    fun create(slug: String): MoveDetailStore =
        object :
            MoveDetailStore,
            Store<MoveDetailIntent, MoveDetailState, MoveDetailLabel> by storeFactory.create(
                name = "MoveDetailStore",
                initialState = MoveDetailState(isLoading = true),
                bootstrapper = BootstrapperImpl(),
                executorFactory = { ExecutorImpl(slug) },
                reducer = ReducerImpl,
            ) {}

    private class BootstrapperImpl : CoroutineBootstrapper<MoveDetailAction>() {
        override fun invoke() {
            dispatch(MoveDetailAction.LoadMove)
        }
    }

    private inner class ExecutorImpl(
        private val slug: String,
    ) : CoroutineExecutor<MoveDetailIntent, MoveDetailAction, MoveDetailState, MoveDetailMessage, MoveDetailLabel>() {
        override fun executeAction(action: MoveDetailAction) {
            when (action) {
                MoveDetailAction.LoadMove -> load()
            }
        }

        override fun executeIntent(intent: MoveDetailIntent) {
            when (intent) {
                is MoveDetailIntent.TabSelected -> dispatch(MoveDetailMessage.TabChanged(intent.tab))
                is MoveDetailIntent.LearnerClicked -> navigateToPokemon(intent.variantSlug)
                MoveDetailIntent.RetryClicked -> load()
            }
        }

        // The learner is read back out of state rather than carried on the Intent: what the Pokemon's
        // hero opens with is a fact about the row that was tapped, and the Store is where that is
        // known. The same shape the dex uses for its cards.
        private fun navigateToPokemon(variantSlug: String) {
            val learner = state().learners.firstOrNull { it.variantSlug == variantSlug } ?: return

            publish(
                MoveDetailLabel.NavigateToPokemon(
                    slug = learner.variantSlug,
                    name = learner.name,
                    artworkUrl = learner.artworkUrl,
                    primaryTypeSlug = learner.primaryType.slug,
                    secondaryTypeSlug = learner.secondaryType?.slug,
                ),
            )
        }

        private fun load() {
            dispatch(MoveDetailMessage.LoadStarted)
            scope.launch {
                try {
                    // A slug with no row is a disagreement between this build and the bundled
                    // dataset rather than a lookup that can legitimately miss, so it reads as the
                    // not-found error rather than as an empty screen. An empty learner list is the
                    // opposite -- 106 moves have one and it is a fact about the move.
                    val move = getMoveDetail(slug)
                    if (move == null) {
                        dispatch(MoveDetailMessage.LoadFailed(AppError.Database.NotFound))
                    } else {
                        dispatch(MoveDetailMessage.MoveLoaded(move, getMoveLearners(slug)))
                    }
                } catch (e: AppException) {
                    dispatch(MoveDetailMessage.LoadFailed(e.error))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    dispatch(MoveDetailMessage.LoadFailed(AppError.Unexpected(e)))
                }
            }
        }
    }

    internal object ReducerImpl : Reducer<MoveDetailState, MoveDetailMessage> {
        override fun MoveDetailState.reduce(msg: MoveDetailMessage): MoveDetailState =
            when (msg) {
                MoveDetailMessage.LoadStarted -> {
                    copy(isLoading = true, error = null)
                }

                is MoveDetailMessage.MoveLoaded -> {
                    copy(isLoading = false, move = msg.move, learners = msg.learners, error = null)
                }

                is MoveDetailMessage.LoadFailed -> {
                    copy(isLoading = false, error = msg.error)
                }

                // Deliberately not reset by a reload: a retry from the Learned by tab should come
                // back to it rather than to the one the screen happened to open on.
                is MoveDetailMessage.TabChanged -> {
                    copy(tab = msg.tab)
                }
            }
    }
}
